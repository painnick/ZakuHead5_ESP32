package com.painnick.zakuhead5;

import android.os.Process;

import com.painnick.zakuhead5.SingleThreadHandlerExecutor;
import com.painnick.zakuhead5.WebImageConsumer;
import com.painnick.zakuhead5.ZakuHeadApi;
import com.painnick.zakuhead5.ZakuHeadApiConsumer;

import org.json.JSONException;

public class ZakuHeadController {

    private static final String TAG = "ZakuHeadController";

    private final String ZakuHeadHost = "http://192.168.5.18";

    private final SingleThreadHandlerExecutor executor;

    private final ZakuHeadApi zakuHeadApi;

    private Boolean started = false;

    private Boolean stopping = true;

    private boolean isLedOn = false;

    private int lastAngle = 90;

    public int getLastAngle() {
        return lastAngle;
    }

    public ZakuHeadController(WebImageConsumer webImageListener, ZakuHeadApiConsumer apiListener) {
        executor = new SingleThreadHandlerExecutor("ZakuHeadController", Process.THREAD_PRIORITY_DEFAULT);
        zakuHeadApi = new ZakuHeadApi(ZakuHeadHost);
        zakuHeadApi.setNewBitmapListener(bitmap -> {
            webImageListener.onNewBitmap(bitmap);
            executor.execute(this::run);
        });
        zakuHeadApi.setApiListener(response -> {
            if (response.has("angle")) {
                try {
                    lastAngle = response.getInt("angle");
                } catch (JSONException e) {
                    // Something
                }
            }
            apiListener.onResponse(response);
        });
    }

    public void start() {
        stopping = false;
        started = true;
        zakuHeadApi.start();
    }

    public void run() {
        if (started && !stopping) {
            zakuHeadApi.reloadImage();
        }
    }

    public void pause() {
        stopping = true;
    }

    public void resume() {
        if (started) {
            stopping = false;
            executor.execute(this::run);
        }
    }

    public void moveLeft(int degree, boolean found) {
        zakuHeadApi.moveLeft(degree, found);
    }

    public void moveRight(int degree, boolean found) {
        zakuHeadApi.moveRight(degree, found);
    }

    public void ledOn(boolean forced) {
        if (forced || !isLedOn) {
            zakuHeadApi.led(10);
        }
        isLedOn = true;
    }

    public void ledOff(boolean forced) {
        if (forced || isLedOn) {
            zakuHeadApi.led(0);
        }
        isLedOn = false;
    }


}
