package com.mediatek.wfo.util;

import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.util.Log;
import java.util.Arrays;

public class RssiMonitoringProcessor {
    static final String TAG = "RssiMonitoringProcessor";
    ConnectivityManager mConnectivityManager;
    private RssiMonitorRequest[][] mRssiMonitoringList;
    int mSimCount;

    class RssiMonitorRequest {
        ConnectivityManager.NetworkCallback mCallback;
        NetworkRequest mRequest;

        RssiMonitorRequest(NetworkRequest request, ConnectivityManager.NetworkCallback callback) {
            this.mRequest = request;
            this.mCallback = callback;
        }
    }

    public RssiMonitoringProcessor(ConnectivityManager connectivityManager) {
        this.mConnectivityManager = connectivityManager;
    }

    public void initialize(int simCount) {
        this.mSimCount = simCount;
        this.mRssiMonitoringList = new RssiMonitorRequest[simCount][];
    }

    public void notifyMultiSimConfigChanged(int activeModemCount) {
        Log.i(TAG, "notifyMultiSimConfigChanged, phone:" + this.mSimCount + "->" + activeModemCount);
        this.mSimCount = activeModemCount;
        this.mRssiMonitoringList = (RssiMonitorRequest[][]) Arrays.copyOf(this.mRssiMonitoringList, activeModemCount);
    }

    public void registerRssiMonitoring(int simId, int size, int[] rssiThresholds) {
        if (!checkInvalidSimIdx(simId, "onRssiMonitorRequest: invalid SIM id")) {
            Log.d(TAG, "onRssiMonitorRequest simId: " + simId + " size: " + size + " rssiThresholds: " + printIntArray(rssiThresholds));
            if (this.mConnectivityManager == null) {
                Log.d(TAG, "onRssiMonitorRequest: mConnectivityManager is null");
                return;
            }
            unregisterAllRssiMonitoring(simId);
            RssiMonitorRequest[] newList = new RssiMonitorRequest[size];
            for (int i = 0; i < size; i++) {
                NetworkRequest request = new NetworkRequest.Builder().setSignalStrength(rssiThresholds[i]).addTransportType(1).build();
                ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback();
                this.mConnectivityManager.registerNetworkCallback(request, callback);
                Log.d(TAG, "onRssiMonitorRequest registerNetworkCallback with rssi: " + rssiThresholds[i]);
                newList[i] = new RssiMonitorRequest(request, callback);
            }
            this.mRssiMonitoringList[simId] = newList;
        }
    }

    private String printIntArray(int[] input) {
        if (input == null) {
            return "input is null";
        }
        String s = new String();
        for (int i = 0; i < input.length; i++) {
            if (i == 0) {
                s = "" + input[i];
            } else {
                s = s + "," + input[i];
            }
        }
        return s;
    }

    public void unregisterAllRssiMonitoring(int simId) {
        Log.d(TAG, "unregisterAllRssiMonitoring(), simId:" + simId);
        if (this.mRssiMonitoringList[simId] != null) {
            int i = 0;
            while (true) {
                RssiMonitorRequest[][] rssiMonitorRequestArr = this.mRssiMonitoringList;
                if (i < rssiMonitorRequestArr[simId].length) {
                    this.mConnectivityManager.unregisterNetworkCallback(rssiMonitorRequestArr[simId][i].mCallback);
                    i++;
                } else {
                    rssiMonitorRequestArr[simId] = null;
                    return;
                }
            }
        }
    }

    private boolean checkInvalidSimIdx(int simIdx, String dbgMsg) {
        if (simIdx >= 0 && simIdx < this.mSimCount) {
            return false;
        }
        Log.d(TAG, dbgMsg);
        return true;
    }
}
