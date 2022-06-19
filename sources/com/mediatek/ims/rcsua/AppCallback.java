package com.mediatek.ims.rcsua;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class AppCallback {
    private boolean callbackOnMain = false;

    public void setCallbackOnMain(boolean isMain) {
        this.callbackOnMain = isMain;
    }

    /* access modifiers changed from: package-private */
    public void run(Runnable callbackRunner) {
        Log.i("AppCallback", "run:" + this.callbackOnMain);
        if (this.callbackOnMain) {
            new Handler(Looper.getMainLooper()).post(callbackRunner);
        } else {
            callbackRunner.run();
        }
    }

    class BaseRunner<Params> implements Runnable {
        private final Params[] params;

        BaseRunner(Params... params2) {
            this.params = params2;
        }

        /* access modifiers changed from: package-private */
        public void exec(Params... paramsArr) {
        }

        public void run() {
            exec(this.params);
        }
    }
}
