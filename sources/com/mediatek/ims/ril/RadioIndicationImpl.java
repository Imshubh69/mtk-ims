package com.mediatek.ims.ril;

public class RadioIndicationImpl extends RadioIndicationBase {
    private ImsRILAdapter mRil;

    RadioIndicationImpl(ImsRILAdapter ril, int phoneId) {
        this.mRil = ril;
    }

    public void enterEmergencyCallbackMode(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1024);
        if (this.mRil.mEnterECBMRegistrants != null) {
            this.mRil.mEnterECBMRegistrants.notifyRegistrants();
        }
    }

    public void exitEmergencyCallbackMode(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1033);
        if (this.mRil.mExitECBMRegistrants != null) {
            this.mRil.mExitECBMRegistrants.notifyRegistrants();
        }
    }

    public void radioStateChanged(int type, int radioState) {
        this.mRil.processIndication(type);
        int newState = getRadioStateFromInt(radioState);
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogMore(1000, "radioStateChanged: " + newState);
        this.mRil.setRadioState(newState);
        this.mRil.notifyRadioStateChanged(newState);
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
        this.mRil.riljLoge(msg);
    }

    /* access modifiers changed from: protected */
    public int getRadioStateFromInt(int stateInt) {
        switch (stateInt) {
            case 0:
                return 0;
            case 1:
                return 2;
            case 10:
                return 1;
            default:
                throw new RuntimeException("Unrecognized RadioState: " + stateInt);
        }
    }
}
