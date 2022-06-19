package com.mediatek.ims;

import com.android.ims.internal.IImsRegistrationListener;
import java.util.function.Consumer;

public final /* synthetic */ class ImsService$$ExternalSyntheticLambda1 implements Consumer {
    public final /* synthetic */ ImsService f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ ImsService$$ExternalSyntheticLambda1(ImsService imsService, int i) {
        this.f$0 = imsService;
        this.f$1 = i;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$updateImsRegstrationEx$1$ImsService(this.f$1, (IImsRegistrationListener) obj);
    }
}
