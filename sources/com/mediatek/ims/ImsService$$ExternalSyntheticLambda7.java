package com.mediatek.ims;

import com.android.ims.internal.IImsRegistrationListener;
import java.util.function.Consumer;

public final /* synthetic */ class ImsService$$ExternalSyntheticLambda7 implements Consumer {
    public final /* synthetic */ int[] f$0;
    public final /* synthetic */ int[] f$1;

    public /* synthetic */ ImsService$$ExternalSyntheticLambda7(int[] iArr, int[] iArr2) {
        this.f$0 = iArr;
        this.f$1 = iArr2;
    }

    public final void accept(Object obj) {
        ImsService.lambda$notifyCapabilityChangedEx$3(this.f$0, this.f$1, (IImsRegistrationListener) obj);
    }
}
