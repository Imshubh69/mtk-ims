package com.mediatek.ims;

import android.net.Uri;
import android.telephony.ims.ImsReasonInfo;
import com.mediatek.ims.internal.IMtkImsRegistrationListener;
import java.util.function.Consumer;

public final /* synthetic */ class ImsService$$ExternalSyntheticLambda4 implements Consumer {
    public final /* synthetic */ ImsService f$0;
    public final /* synthetic */ ImsRegInfo f$1;
    public final /* synthetic */ Uri[] f$2;
    public final /* synthetic */ ImsReasonInfo f$3;

    public /* synthetic */ ImsService$$ExternalSyntheticLambda4(ImsService imsService, ImsRegInfo imsRegInfo, Uri[] uriArr, ImsReasonInfo imsReasonInfo) {
        this.f$0 = imsService;
        this.f$1 = imsRegInfo;
        this.f$2 = uriArr;
        this.f$3 = imsReasonInfo;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$notifyImsRegInd$6$ImsService(this.f$1, this.f$2, this.f$3, (IMtkImsRegistrationListener) obj);
    }
}
