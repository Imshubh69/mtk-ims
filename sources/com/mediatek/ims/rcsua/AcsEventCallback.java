package com.mediatek.ims.rcsua;

import android.util.Log;
import com.mediatek.ims.rcsua.AppCallback;

public abstract class AcsEventCallback extends AppCallback {
    public void onConfigurationStatusChanged(boolean valid, int version) {
    }

    public void onAcsConnected() {
    }

    public void onAcsDisconnected() {
    }

    class Runner extends AppCallback.BaseRunner<Integer> {
        Runner(Integer... params) {
            super(params);
        }

        /* access modifiers changed from: package-private */
        public void exec(Integer... params) {
            boolean valid = false;
            int type = params[0].intValue();
            if (params[1].intValue() == 1) {
                valid = true;
            }
            int version = params[2].intValue();
            Log.d("AcsEventCallback", "type[" + type + "],valid[" + valid + "],version[" + version + "]");
            switch (type) {
                case 0:
                    AcsEventCallback.this.onConfigurationStatusChanged(valid, version);
                    return;
                case 1:
                    AcsEventCallback.this.onAcsConnected();
                    return;
                case 2:
                    AcsEventCallback.this.onAcsDisconnected();
                    return;
                default:
                    return;
            }
        }
    }
}
