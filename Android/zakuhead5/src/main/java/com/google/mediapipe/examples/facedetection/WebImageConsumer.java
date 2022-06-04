package com.google.mediapipe.examples.facedetection;

import android.graphics.Bitmap;

public interface WebImageConsumer {
    void onNewBitmap(Bitmap bitmap);
}
