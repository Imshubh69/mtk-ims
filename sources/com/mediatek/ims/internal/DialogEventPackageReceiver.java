package com.mediatek.ims.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.Rlog;
import android.telephony.ims.ImsExternalCallState;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.ImsConstants;
import com.mediatek.ims.ImsServiceCallTracker;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class DialogEventPackageReceiver {
    private static final String TAG = "DialogEventPackageReceiver";
    /* access modifiers changed from: private */
    public Listener mListener;
    private DialogEventPackageParser mParser;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ImsConstants.ACTION_IMS_DIALOG_EVENT_PACKAGE)) {
                return;
            }
            if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                DialogEventPackageReceiver.this.processDepIntent(intent);
                return;
            }
            ArrayList<ImsExternalCallState> dialogList = intent.getParcelableArrayListExtra(ImsConstants.EXTRA_DEP_CONTENT);
            Iterator<ImsExternalCallState> it = dialogList.iterator();
            while (it.hasNext()) {
                Rlog.d(DialogEventPackageReceiver.TAG, "ACTION_IMS_DIALOG_EVENT_PACKAGE content:" + it.next().toString());
            }
            DialogEventPackageReceiver.this.mListener.onStateChanged(dialogList);
        }
    };

    public interface Listener {
        void onStateChanged(List<ImsExternalCallState> list);
    }

    public static abstract class ListenerBase implements Listener {
        public void onStateChanged(List<ImsExternalCallState> list) {
        }
    }

    public DialogEventPackageReceiver(Context context, Listener listener) {
        registerForBroadcast(context);
        this.mParser = new DepXmlPullParser();
        this.mListener = listener;
        Rlog.d(TAG, TAG);
    }

    private void registerForBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ImsConstants.ACTION_IMS_DIALOG_EVENT_PACKAGE);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.mReceiver, filter);
    }

    /* access modifiers changed from: private */
    public void processDepIntent(Intent intent) {
        String content = intent.getStringExtra(ImsConstants.EXTRA_DEP_CONTENT);
        Rlog.d(TAG, "ACTION_IMS_DIALOG_EVENT_PACKAGE " + content);
        try {
            this.mListener.onStateChanged(ExternalCallStateFactory.getInstance().makeExternalCallStates(this.mParser.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))));
        } catch (XmlPullParserException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("processDepIntent has XmlPullParserException ");
            sb.append(ImsServiceCallTracker.sensitiveEncode("" + e));
            Rlog.d(TAG, sb.toString());
        } catch (IOException e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("processDepIntent has IOException ");
            sb2.append(ImsServiceCallTracker.sensitiveEncode("" + e2));
            Rlog.d(TAG, sb2.toString());
        }
    }
}
