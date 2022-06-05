// Copyright 2021 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.painnick.zakuhead5;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mediapipe.formats.proto.DetectionProto;
import com.google.mediapipe.formats.proto.LocationDataProto;
import com.google.mediapipe.solutions.facedetection.FaceDetection;
import com.google.mediapipe.solutions.facedetection.FaceDetectionOptions;

import java.util.Date;

enum FACE_DIRECTION {
    NONE,
    LEFT,
    RIGHT
}

/**
 * Main activity of MediaPipe Face Detection app.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ZakuHeadController zakuHeadController;
    private FaceDetection faceDetection;

    // Image demo UI and image loader components.
    private FaceDetectionResultImageView imageView;

    private Date lastDetection, startFoundSeq, startNotFoundSeq;
    private FACE_DIRECTION faceDirection = FACE_DIRECTION.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastDetection = new Date();
        startFoundSeq = null;
        startNotFoundSeq = null;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setupStaticImageDemoUiComponents();
        Toast.makeText(MainActivity.this, "Wifi를 켜고, ZakuHead4에 연결해주세요.\n인터넷에 연결되지 않습니다.", Toast.LENGTH_SHORT).show();
        zakuHeadController.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (zakuHeadController != null) {
            zakuHeadController.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (zakuHeadController != null) {
            zakuHeadController.pause();
        }
    }

    /**
     * Sets up the UI components for the static image demo.
     */
    private void setupStaticImageDemoUiComponents() {
        imageView = new FaceDetectionResultImageView(this);
        setupStaticImageModePipeline();
        zakuHeadController = new ZakuHeadController(bitmap -> faceDetection.send(bitmap), response -> {
        });

        // 버튼과 화면과 위치가 반대임에 유의!
        findViewById(R.id.move_left).setOnClickListener(
                v -> {
                    faceDirection = FACE_DIRECTION.RIGHT;
                    zakuHeadController.moveRight(15, false);
                });
        findViewById(R.id.move_right).setOnClickListener(
                v -> {
                    faceDirection = FACE_DIRECTION.LEFT;
                    zakuHeadController.moveLeft(15, false);
                });
    }

    /**
     * Sets up core workflow for static image mode.
     */
    private void setupStaticImageModePipeline() {
        // Initializes a new MediaPipe Face Detection solution instance in the static image mode.
        faceDetection =
                new FaceDetection(
                        this,
                        FaceDetectionOptions.builder()
                                .setStaticImageMode(true)
                                .setModelSelection(0)
                                .setMinDetectionConfidence(0.7f)
                                .build());

        // Connects MediaPipe Face Detection solution to the user-defined FaceDetectionResultImageView.
        faceDetection.setResultListener(
                faceDetectionResult -> {
                    imageView.setFaceDetectionResult(faceDetectionResult);
                    runOnUiThread(() -> imageView.update());

                    float maxWidth = 0;
                    DetectionProto.Detection foundDetection = null;
                    for (DetectionProto.Detection detection : faceDetectionResult.multiFaceDetections()) {
                        LocationDataProto.LocationData.RelativeBoundingBox box = detection.getLocationData().getRelativeBoundingBox();
                        if (maxWidth < box.getWidth()) {
                            foundDetection = detection;
                            maxWidth = box.getWidth();
                        }
                    }

                    if (foundDetection != null) { // Found!!!
                        boolean isStartSeq = (startFoundSeq == null);
                        if (isStartSeq) {
                            startFoundSeq = new Date();
                            startNotFoundSeq = null;
                        }
                        zakuHeadController.ledOn(isStartSeq);
                        LocationDataProto.LocationData.RelativeBoundingBox box = foundDetection.getLocationData().getRelativeBoundingBox();
                        float center = box.getXmin() + (box.getWidth() / 2);

                        if (0.4f > center || center > 0.6f) {
                            if (center > 0.5) {
                                faceDirection = FACE_DIRECTION.RIGHT;
                                zakuHeadController.moveRight(5, true);
                            } else {
                                faceDirection = FACE_DIRECTION.LEFT;
                                zakuHeadController.moveLeft(5, true);
                            }
                        } else {
                            Date now = new Date();
                            // 일정 시간동안 얼굴을 찾고 있더라도 찾았다는 사실을 알림
                            if (((now.getTime() - startFoundSeq.getTime()) / 1000) % 3 == 1) {
                                zakuHeadController.moveLeft(0, true);
                            }
                        }
                        lastDetection = new Date();
                    } else {
                        Date now = new Date();
                        long lostSeconds = (now.getTime() - lastDetection.getTime()) / 1000;

                        // LOST!!!
                        if ((startNotFoundSeq == null) && (lostSeconds > 2)) {
                            startFoundSeq = null;
                            startNotFoundSeq = new Date();
                            zakuHeadController.ledOff(true);
                        }

                        // 기존 방향으로 카메라 이동
                        int lastAngle = zakuHeadController.getLastAngle();
                        if ((20 < lastAngle) && (lastAngle < 160)) {
                            if (faceDirection == FACE_DIRECTION.RIGHT) {
                                zakuHeadController.moveRight(30, false);
                            } else {
                                zakuHeadController.moveLeft(30, false);
                            }
                        }
                    }
                });
        faceDetection.setErrorListener(
                (message, e) -> Log.e(TAG, "MediaPipe Face Detection error:" + message));

        // Updates the preview layout.
        FrameLayout frameLayout = findViewById(R.id.preview_display_layout);
        frameLayout.removeAllViewsInLayout();
        imageView.setImageDrawable(null);

//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        imageView.setLayoutParams(layoutParams);

        frameLayout.addView(imageView);
        imageView.setVisibility(View.VISIBLE);
    }

    private void stopCurrentPipeline() {
        if (faceDetection != null) {
            faceDetection.close();
        }
    }

}
