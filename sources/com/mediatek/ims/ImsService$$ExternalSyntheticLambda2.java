package com.mediatek.ims;

import com.mediatek.ims.internal.IMtkImsRegistrationListener;
import java.util.function.Consumer;

public final /* synthetic */ class ImsService$$ExternalSyntheticLambda2 implements Consumer {
    public final /* synthetic */ ImsService f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ ImsService$$ExternalSyntheticLambda2(ImsService imsService, int i) {
        this.f$0 = imsService;
        this.f$1 = i;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$notifyRegistrationErrorCode$5$ImsService(this.f$1, (IMtkImsRegistrationListener) obj);
    }
}
