package com.mediatek.ims;

import android.os.AsyncResult;
import com.mediatek.ims.internal.IMtkImsRegistrationListener;
import java.util.function.Consumer;

public final /* synthetic */ class ImsService$$ExternalSyntheticLambda3 implements Consumer {
    public final /* synthetic */ ImsService f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ AsyncResult f$2;

    public /* synthetic */ ImsService$$ExternalSyntheticLambda3(ImsService imsService, int i, AsyncResult asyncResult) {
        this.f$0 = imsService;
        this.f$1 = i;
        this.f$2 = asyncResult;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$notifyRedirectIncomingCall$7$ImsService(this.f$1, this.f$2, (IMtkImsRegistrationListener) obj);
    }
}
