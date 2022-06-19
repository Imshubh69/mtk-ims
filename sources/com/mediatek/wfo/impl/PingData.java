package com.mediatek.wfo.impl;

import android.util.Log;

public class PingData {
    private static final String TAG = "PingData";
    private double mLatencyData;
    private int mPacketLostData;
    private int mSimId;

    public PingData(int simId, double latency, int lostData) {
        this.mSimId = simId;
        this.mLatencyData = latency;
        this.mPacketLostData = lostData;
    }

    public double getPingLatency() {
        debugInfo("mPingLatency:" + this.mLatencyData);
        return this.mLatencyData;
    }

    public int getPacketLoss() {
        debugInfo("mPacketLost:" + this.mPacketLostData);
        return this.mPacketLostData;
    }

    private void debugInfo(String info) {
        Log.i(TAG, "[" + this.mSimId + "]: " + info);
    }
}
