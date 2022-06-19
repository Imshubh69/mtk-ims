package com.mediatek.ims;

import android.telephony.ims.ImsReasonInfo;
import com.android.ims.internal.IImsRegistrationListener;
import java.util.function.Consumer;

public final /* synthetic */ class ImsService$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ ImsReasonInfo f$0;

    public /* synthetic */ ImsService$$ExternalSyntheticLambda0(ImsReasonInfo imsReasonInfo) {
        this.f$0 = imsReasonInfo;
    }

    public final void accept(Object obj) {
        ImsService.lambda$updateImsRegstrationEx$2(this.f$0, (IImsRegistrationListener) obj);
    }
}
