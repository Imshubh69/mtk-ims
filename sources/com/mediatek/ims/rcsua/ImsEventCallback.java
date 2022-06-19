package com.mediatek.ims.rcsua;

import android.util.Log;
import com.mediatek.ims.rcsua.AppCallback;

public abstract class ImsEventCallback extends AppCallback {
    public void onRegistering(int mode) {
    }

    public void onRegistered(int mode) {
    }

    public void onDeregistering(int mode) {
    }

    public void onDeregistered(int mode) {
    }

    public void onDeregStart(int mode) {
    }

    public void onReregistered(int mode) {
    }

    public void onVopsIndication(int vops) {
    }

    class Runner extends AppCallback.BaseRunner<Integer> {
        Runner(Integer... params) {
            super(params);
        }

        /* access modifiers changed from: package-private */
        public void exec(Integer... params) {
            int regState = params[0].intValue();
            int regMode = params[1].intValue();
            Log.i("ImsEventCallback", "Runner->exec:" + regState + "," + regMode);
            switch (regState) {
                case 0:
                    ImsEventCallback.this.onDeregistered(regMode);
                    return;
                case 1:
                    ImsEventCallback.this.onRegistering(regMode);
                    return;
                case 2:
                    ImsEventCallback.this.onRegistered(regMode);
                    return;
                case 3:
                    ImsEventCallback.this.onDeregistering(regMode);
                    return;
                case 128:
                    ImsEventCallback.this.onDeregStart(regMode);
                    return;
                case 256:
                    ImsEventCallback.this.onReregistered(regMode);
                    return;
                case 512:
                    ImsEventCallback.this.onVopsIndication(params[2].intValue());
                    return;
                default:
                    return;
            }
        }
    }
}
