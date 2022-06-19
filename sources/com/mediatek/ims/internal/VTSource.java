package com.mediatek.ims.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.radio.V1_5.NgranBands;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Range;
import android.view.Surface;
import com.mediatek.ims.SipMessage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class VTSource {
    public static final int CAMERA_HARWARE_LEVEL_1 = 1;
    public static final int CAMERA_HARWARE_LEVEL_3 = 3;
    private static final String TAG = "VT SRC";
    private static final int TIME_OUT_MS = 6500;
    public static final int VT_SRV_CALL_3G = 1;
    public static final int VT_SRV_CALL_4G = 2;
    protected static Resolution[] sCameraResolutions;
    protected static Context sContext;
    /* access modifiers changed from: private */
    public Surface mCachedPauseImageSurface;
    /* access modifiers changed from: private */
    public Surface mCachedPreviewSurface;
    /* access modifiers changed from: private */
    public Surface mCachedRecordSurface;
    /* access modifiers changed from: private */
    public final CameraManager mCameraManager;
    private final EventCallback mEventCallBack;
    /* access modifiers changed from: private */
    public boolean mIsWaitRelease;
    /* access modifiers changed from: private */
    public AtomicInteger mMessageId;
    private final int mMode;
    /* access modifiers changed from: private */
    public boolean mNeedRecordStream;
    /* access modifiers changed from: private */
    public int mPauseImageHeight;
    /* access modifiers changed from: private */
    public int mPauseImageWidth;
    private Handler mRequestHandler;
    private HandlerThread mRequestThread;
    /* access modifiers changed from: private */
    public boolean mStopPreviewAndRecord;
    /* access modifiers changed from: private */
    public String mTAG;
    public final ImsVTProviderUtil mVTProviderUtil;

    public interface EventCallback {
        void onError();

        void onOpenFail();

        void onOpenSuccess();
    }

    public static final class Resolution {
        int mDegree;
        int mFacing;
        int mHal;
        int mId;
        int mMaxHeight;
        int mMaxWidth;

        public String toString() {
            return " mId: " + this.mId + " mMaxWidth: " + this.mMaxWidth + " mMaxHeight: " + this.mMaxHeight + " mDegree: " + this.mDegree + " mFacing: " + this.mFacing + " mHal: " + this.mHal;
        }
    }

    private static class HandlerExecutor implements Executor {
        private final Handler mHandler;

        public HandlerExecutor(Handler handler) {
            this.mHandler = handler;
        }

        public void execute(Runnable runCmd) {
            this.mHandler.post(runCmd);
        }
    }

    public static void setContext(Context context) {
        Log.d(TAG, "[STC] [setContext] context:" + context);
        sContext = context;
    }

    public static Resolution[] getAllCameraResolutions() {
        Log.d(TAG, "[STC] [getAllCameraResolutions] Start");
        if (sCameraResolutions == null) {
            ArrayList<Resolution> sensorResolutions = new ArrayList<>();
            CameraManager cameraManager = (CameraManager) sContext.getSystemService("camera");
            try {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    Resolution resolution = new Resolution();
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Rect sensorRes = (Rect) characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    int sensorOrientation = ((Integer) characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
                    int facing = ((Integer) characteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
                    int hal = 3;
                    if (2 == ((Integer) characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue()) {
                        hal = 1;
                    }
                    resolution.mId = Integer.valueOf(cameraId).intValue();
                    resolution.mMaxWidth = sensorRes.width();
                    resolution.mMaxHeight = sensorRes.width();
                    resolution.mDegree = sensorOrientation;
                    resolution.mFacing = facing;
                    resolution.mHal = hal;
                    Log.w(TAG, "[getAllCameraResolutions] " + resolution);
                    sensorResolutions.add(resolution);
                }
            } catch (Exception e) {
                Log.e(TAG, "[STC] [getAllCameraResolutions] getCameraIdList with exception:" + e);
            }
            if (sensorResolutions.size() > 0) {
                Resolution[] resolutionArr = new Resolution[sensorResolutions.size()];
                sCameraResolutions = resolutionArr;
                sCameraResolutions = (Resolution[]) sensorResolutions.toArray(resolutionArr);
            }
            Log.d(TAG, "[STC] [getAllCameraResolutions] resolution size:" + sensorResolutions.size());
        }
        Log.d(TAG, "[STC] [getAllCameraResolutions] Finish");
        return sCameraResolutions;
    }

    public VTSource(int mode, int callId, EventCallback cb) {
        this.mVTProviderUtil = ImsVTProviderUtil.getInstance();
        this.mIsWaitRelease = false;
        this.mPauseImageWidth = 0;
        this.mPauseImageHeight = 0;
        this.mCachedPauseImageSurface = null;
        this.mStopPreviewAndRecord = false;
        this.mMessageId = new AtomicInteger(0);
        String str = "VT SRC - " + callId;
        this.mTAG = str;
        Log.d(str, "[INT] [VTSource] Start, mode: " + mode);
        this.mMode = mode;
        this.mEventCallBack = cb;
        this.mCameraManager = (CameraManager) sContext.getSystemService("camera");
        createRequestThreadAndHandler();
        Log.d(this.mTAG, "[INT] [VTSource] Finish");
    }

    public VTSource() {
        this.mVTProviderUtil = ImsVTProviderUtil.getInstance();
        this.mIsWaitRelease = false;
        this.mPauseImageWidth = 0;
        this.mPauseImageHeight = 0;
        this.mCachedPauseImageSurface = null;
        this.mStopPreviewAndRecord = false;
        this.mMessageId = new AtomicInteger(0);
        this.mMode = 2;
        this.mEventCallBack = null;
        this.mCameraManager = null;
    }

    public void setPauseImage(Uri uri) {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [setPauseImage] Start, uri : " + uri + " [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [setPauseImage] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(14, messageId, 0, uri).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [setPauseImage] Finish [" + messageId + "]");
    }

    public void open(String cameraId) {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [open] Start, id : " + cameraId + " [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [open] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(0, messageId, 0, cameraId).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [open] Finish [" + messageId + "]");
    }

    public void close() {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [close] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [close] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(9, messageId, 0).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [close] Finish [" + messageId + "]");
    }

    public void restart() {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [restart] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [restart] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(12, messageId, 0).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [restart] Finish [" + messageId + "]");
    }

    public void release() {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [release] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [release] Fail [" + messageId + "]");
            return;
        }
        this.mIsWaitRelease = true;
        this.mRequestHandler.obtainMessage(10, messageId, 0).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        this.mRequestHandler.removeCallbacksAndMessages((Object) null);
        this.mRequestThread.quitSafely();
        this.mRequestThread = null;
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [release] Finish [" + messageId + "]");
    }

    public void setRecordSurface(Surface surface) {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [setRecordSurface] Start, surface:" + surface + " [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [setRecordSurface] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(3, messageId, 0, surface).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [setRecordSurface] Finish [" + messageId + "]");
    }

    public void setPreviewSurface(Surface surface) {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [setPreviewSurface] Start, surface:" + surface + " [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            Log.d(this.mTAG, "[INT] [setPreviewSurface] Fail");
            return;
        }
        if (surface == null) {
            this.mRequestHandler.obtainMessage(2, messageId, 0).sendToTarget();
        } else {
            Surface surface2 = this.mCachedPreviewSurface;
            if (surface2 != null) {
                String[] oriSurfaceToken = surface2.toString().split("@");
                String[] newSurfaceToken = surface.toString().split("@");
                String str2 = this.mTAG;
                Log.d(str2, "[INT] [setPreviewSurface] oriSurfaceToken[1]:" + oriSurfaceToken[1] + ", newSurfaceToken[1]:" + newSurfaceToken[1]);
                if (newSurfaceToken[1].equals(oriSurfaceToken[1]) && !this.mVTProviderUtil.getImsOemCallUtil().alwaysSetPreviewSurface()) {
                    String str3 = this.mTAG;
                    Log.d(str3, "[INT] [setPreviewSurface] surface not changed, ignore! [" + messageId + "]");
                    return;
                }
            }
            this.mRequestHandler.obtainMessage(1, messageId, 0, surface).sendToTarget();
        }
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str4 = this.mTAG;
        Log.d(str4, "[INT] [setPreviewSurface] Finish [" + messageId + "]");
    }

    public void setPauseImageSurface(Surface surface) {
        String str = this.mTAG;
        Log.d(str, "setPauseImageSurface, surface: " + surface);
        this.mCachedPauseImageSurface = surface;
    }

    public void setZoom(float zoomValue) {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [setZoom] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [setZoom] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(7, messageId, 0, Float.valueOf(zoomValue)).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [setZoom] Finish [" + messageId + "]");
    }

    public CameraCharacteristics getCameraCharacteristics() {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [getCameraCharacteristics] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [getCameraCharacteristics] Fail [" + messageId + "]");
            return null;
        }
        CameraCharacteristics[] characteristicses = new CameraCharacteristics[1];
        this.mRequestHandler.obtainMessage(8, messageId, 0, characteristicses).sendToTarget();
        if (waitDone(this.mRequestHandler)) {
            String str3 = this.mTAG;
            Log.d(str3, "[INT] [getCameraCharacteristics] Finish [" + messageId + "]");
            return characteristicses[0];
        }
        this.mEventCallBack.onError();
        String str4 = this.mTAG;
        Log.d(str4, "[INT] [getCameraCharacteristics] Finish (null) [" + messageId + "]");
        return null;
    }

    public void startRecording() {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [startRecording] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [startRecording] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(4, messageId, 0).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [startRecording] Finish [" + messageId + "]");
    }

    public void stopRecording() {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [stopRecording] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [stopRecording] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(5, messageId, 0).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [stopRecording] Finish [" + messageId + "]");
    }

    public void stopRecordingAndPreview() {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [stopRecordingAndPreview] Start [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            String str2 = this.mTAG;
            Log.d(str2, "[INT] [stopRecordingAndPreview] Fail [" + messageId + "]");
            return;
        }
        this.mRequestHandler.obtainMessage(13, messageId, 0).sendToTarget();
        this.mStopPreviewAndRecord = true;
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str3 = this.mTAG;
        Log.d(str3, "[INT] [stopRecordingAndPreview] Finish [" + messageId + "]");
    }

    public void hideMe() {
        Log.d(this.mTAG, "[INT] [hideMe]");
    }

    public void showMe() {
        Log.d(this.mTAG, "[INT] [showMe]");
    }

    public void setDeviceOrientation(int degree) {
        int messageId = this.mMessageId.incrementAndGet();
        String str = this.mTAG;
        Log.d(str, "[INT] [setDeviceOrientation] Start, degree : " + degree + " [" + messageId + "]");
        if (IsHandlerThreadUnavailable()) {
            Log.d(this.mTAG, "[INT] [setDeviceOrientation] Fail");
            return;
        }
        this.mRequestHandler.obtainMessage(11, messageId, 0, Integer.valueOf(degree)).sendToTarget();
        if (!waitDone(this.mRequestHandler)) {
            this.mEventCallBack.onError();
        }
        String str2 = this.mTAG;
        Log.d(str2, "[INT] [setDeviceOrientation] Finish [" + messageId + "]");
    }

    public void setPauseImageSize(int width, int height) {
        String str = this.mTAG;
        Log.d(str, "[setPauseImageSize] width=" + width + ", height=" + height);
        this.mPauseImageWidth = width;
        this.mPauseImageHeight = height;
    }

    private void createRequestThreadAndHandler() {
        if (this.mRequestThread == null) {
            HandlerThread handlerThread = new HandlerThread("VTSource-Request");
            this.mRequestThread = handlerThread;
            handlerThread.start();
            this.mRequestHandler = new DeviceHandler(this.mRequestThread.getLooper(), this.mMode == 2, this.mEventCallBack);
        }
    }

    private boolean IsHandlerThreadUnavailable() {
        boolean z = false;
        if (this.mRequestThread != null && !this.mIsWaitRelease) {
            return false;
        }
        String str = this.mTAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Thread = null:");
        if (this.mRequestThread == null) {
            z = true;
        }
        sb.append(z);
        sb.append(", mIsWaitRelease:");
        sb.append(this.mIsWaitRelease);
        Log.d(str, sb.toString());
        return true;
    }

    private class DeviceHandler extends Handler {
        private static final int MAX_RETRY_OPEN_CAMERA_COUNT = 25;
        public static final int MSG_CLOSE_CAMERA = 9;
        public static final int MSG_DEVICE_ORIENTATION = 11;
        public static final int MSG_GET_CAMERA_CHARACTERISTICS = 8;
        public static final int MSG_OPEN_CAMERA = 0;
        public static final int MSG_PERFORM_ZOOM = 7;
        public static final int MSG_RELEASE = 10;
        public static final int MSG_RESTART_CAMERA = 12;
        public static final int MSG_SET_PAUSE_IMAGE = 14;
        public static final int MSG_START_PREVIEW = 1;
        public static final int MSG_START_RECORDING = 4;
        public static final int MSG_STOP_PREVIEW = 2;
        public static final int MSG_STOP_RECORDING = 5;
        public static final int MSG_STOP_RECORDING_PREVIEW = 13;
        public static final int MSG_SUBMIT_REQUEST = 6;
        public static final int MSG_UPDATE_RECORD_SURFACE = 3;
        /* access modifiers changed from: private */
        public CameraCaptureSession mCameraCaptureSession;
        private CameraCharacteristics mCameraCharacteristics;
        /* access modifiers changed from: private */
        public CameraDevice mCameraDevice;
        /* access modifiers changed from: private */
        public String mCameraId;
        private CameraDevice.StateCallback mDeviceCallback = new CameraDevice.StateCallback() {
            public void onError(CameraDevice cameraDevice, int error) {
                String access$000 = VTSource.this.mTAG;
                Log.e(access$000, "[HDR] [onError] error:" + error);
                if (DeviceHandler.this.mRetryCount >= 25 || VTSource.this.mStopPreviewAndRecord || !(error == 1 || error == 2)) {
                    DeviceHandler.this.mDeviceConditionVariable.open();
                    if (!VTSource.this.mIsWaitRelease) {
                        DeviceHandler.this.mEventCallBack.onError();
                    } else {
                        Log.d(VTSource.this.mTAG, "mIsWaitRelease means call end or VTS error, ignore MA error");
                    }
                    DeviceHandler.this.mEventCallBack.onOpenFail();
                    return;
                }
                DeviceHandler.access$1008(DeviceHandler.this);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DeviceHandler deviceHandler = DeviceHandler.this;
                deviceHandler.doOpenCamera(deviceHandler.mCameraId);
            }

            public void onDisconnected(CameraDevice cameraDevice) {
                String access$000 = VTSource.this.mTAG;
                Log.e(access$000, "[HDR] [onDisconnected] cameraDevice:" + cameraDevice);
                if (DeviceHandler.this.mCameraDevice != null) {
                    DeviceHandler.this.mCameraDevice.close();
                    CameraDevice unused = DeviceHandler.this.mCameraDevice = null;
                }
                DeviceHandler.this.mDeviceConditionVariable.open();
            }

            public void onOpened(CameraDevice cameraDevice) {
                Log.d(VTSource.this.mTAG, "[HDR] [onOpened]");
                CameraDevice unused = DeviceHandler.this.mCameraDevice = cameraDevice;
                if (VTSource.this.mCachedPreviewSurface != null) {
                    DeviceHandler deviceHandler = DeviceHandler.this;
                    deviceHandler.obtainMessage(1, VTSource.this.mMessageId.incrementAndGet(), 0, VTSource.this.mCachedPreviewSurface).sendToTarget();
                    String access$000 = VTSource.this.mTAG;
                    Log.d(access$000, "[HDR] [onOpened] Send message to handler [" + VTSource.this.mMessageId.get() + "]");
                }
                DeviceHandler.this.mDeviceConditionVariable.open();
                DeviceHandler.this.mEventCallBack.onOpenSuccess();
            }

            public void onClosed(CameraDevice cameraDevice) {
                Log.d(VTSource.this.mTAG, "[HDR] [onClosed]");
                super.onClosed(cameraDevice);
                DeviceHandler.this.mDeviceConditionVariable.open();
            }
        };
        /* access modifiers changed from: private */
        public ConditionVariable mDeviceConditionVariable = new ConditionVariable();
        private int mDeviceDegree;
        /* access modifiers changed from: private */
        public EventCallback mEventCallBack;
        private boolean mHasAddTarget = false;
        private boolean mNeedPortraitBuffer;
        private List<OutputConfiguration> mOutputConfigurations = new ArrayList();
        private HandlerThread mRespondThread;
        /* access modifiers changed from: private */
        public int mRetryCount;
        private CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                Log.d(VTSource.this.mTAG, "[onConfigured]");
                CameraCaptureSession unused = DeviceHandler.this.mCameraCaptureSession = cameraCaptureSession;
                DeviceHandler deviceHandler = DeviceHandler.this;
                deviceHandler.obtainMessage(6, VTSource.this.mMessageId.incrementAndGet(), 0).sendToTarget();
                String access$000 = VTSource.this.mTAG;
                Log.d(access$000, "[onConfigured] Send message to handler [" + VTSource.this.mMessageId.get() + "]");
                DeviceHandler.this.mSessionConditionVariable.open();
            }

            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                Log.d(VTSource.this.mTAG, "[onConfigureFailed]");
                DeviceHandler.this.mSessionConditionVariable.open();
                if (!VTSource.this.mIsWaitRelease) {
                    DeviceHandler.this.mEventCallBack.onError();
                } else {
                    Log.d(VTSource.this.mTAG, "mIsWaitRelease means call end or VTS error, ignore MA error");
                }
            }
        };
        /* access modifiers changed from: private */
        public ConditionVariable mSessionConditionVariable = new ConditionVariable();
        private List<Surface> mSessionUsedSurfaceList = new ArrayList();
        private float mZoomValue = 1.0f;

        static /* synthetic */ int access$1008(DeviceHandler x0) {
            int i = x0.mRetryCount;
            x0.mRetryCount = i + 1;
            return i;
        }

        DeviceHandler(Looper looper, boolean needPortraitBuffer, EventCallback cb) {
            super(looper);
            this.mNeedPortraitBuffer = needPortraitBuffer;
            HandlerThread handlerThread = new HandlerThread("VTSource-Respond");
            this.mRespondThread = handlerThread;
            handlerThread.start();
            this.mDeviceDegree = 0;
            this.mEventCallBack = cb;
        }

        public void handleMessage(Message msg) {
            if (this.mRespondThread == null) {
                Log.w(VTSource.this.mTAG, "[handleMessage] mRespondThread null, ignore message!!");
                return;
            }
            switch (msg.what) {
                case 0:
                    String access$000 = VTSource.this.mTAG;
                    Log.d(access$000, "[HDR] [handleMessage] MSG_OPEN_CAMERA [" + msg.arg1 + "]");
                    openCamera((String) msg.obj);
                    return;
                case 1:
                    String access$0002 = VTSource.this.mTAG;
                    Log.d(access$0002, "[HDR] [handleMessage] MSG_START_PREVIEW [" + msg.arg1 + "]");
                    Surface newSurface = (Surface) msg.obj;
                    if (this.mCameraDevice == null || newSurface == null || !newSurface.isValid()) {
                        String access$0003 = VTSource.this.mTAG;
                        Log.w(access$0003, "[HDR] [handleMessage] start preview with status error, device:" + this.mCameraDevice + ", new surface:" + newSurface);
                        if (newSurface != null && newSurface.isValid()) {
                            Log.d(VTSource.this.mTAG, "[HDR] [handleMessage] Camera closed, store the surface for use later.");
                            Surface unused = VTSource.this.mCachedPreviewSurface = newSurface;
                            return;
                        }
                        return;
                    }
                    if (newSurface.equals(VTSource.this.mCachedPreviewSurface)) {
                        closeSession();
                    }
                    Surface unused2 = VTSource.this.mCachedPreviewSurface = newSurface;
                    createSession();
                    return;
                case 2:
                    String access$0004 = VTSource.this.mTAG;
                    Log.d(access$0004, "[HDR] [handleMessage] MSG_STOP_PREVIEW [" + msg.arg1 + "]");
                    Surface unused3 = VTSource.this.mCachedPreviewSurface = null;
                    closeSession();
                    createSession();
                    return;
                case 3:
                    String access$0005 = VTSource.this.mTAG;
                    Log.d(access$0005, "[HDR] [handleMessage] MSG_UPDATE_RECORD_SURFACE [" + msg.arg1 + "]");
                    Surface newSurface2 = (Surface) msg.obj;
                    if (newSurface2 != null || VTSource.this.mCachedRecordSurface != null) {
                        Surface unused4 = VTSource.this.mCachedRecordSurface = newSurface2;
                        if (newSurface2 == null) {
                            Log.d(VTSource.this.mTAG, "[HDR] record surface change to null, no need recreate Session because recording should be stopped already");
                            return;
                        }
                        closeSession();
                        createSession();
                        return;
                    }
                    return;
                case 4:
                    String access$0006 = VTSource.this.mTAG;
                    Log.d(access$0006, "[HDR] [handleMessage] MSG_START_RECORDING [" + msg.arg1 + "]");
                    if (this.mCameraDevice == null || this.mCameraCaptureSession == null || VTSource.this.mNeedRecordStream) {
                        String access$0007 = VTSource.this.mTAG;
                        Log.w(access$0007, "[HDR] [handleMessage] start recording status error, device:" + this.mCameraDevice + ", session:" + this.mCameraCaptureSession + ", record status:" + VTSource.this.mNeedRecordStream);
                        boolean unused5 = VTSource.this.mNeedRecordStream = true;
                        return;
                    }
                    boolean unused6 = VTSource.this.mNeedRecordStream = true;
                    submitRepeatingRequest();
                    return;
                case 5:
                    String access$0008 = VTSource.this.mTAG;
                    Log.d(access$0008, "[HDR] [handleMessage] MSG_STOP_RECORDING [" + msg.arg1 + "]");
                    if (VTSource.this.mNeedRecordStream) {
                        boolean unused7 = VTSource.this.mNeedRecordStream = false;
                        if (VTSource.this.mCachedRecordSurface != null) {
                            closeSession();
                            createSession();
                            return;
                        }
                        return;
                    }
                    return;
                case 6:
                    String access$0009 = VTSource.this.mTAG;
                    Log.d(access$0009, "[HDR] [handleMessage] MSG_SUBMIT_REQUEST [" + msg.arg1 + "]");
                    if (this.mCameraDevice == null || this.mCameraCaptureSession == null) {
                        Log.w(VTSource.this.mTAG, "[HDR] [handleMessage] submitRepeatingRequest illegal state, ignore!");
                        return;
                    } else {
                        submitRepeatingRequest();
                        return;
                    }
                case 7:
                    String access$00010 = VTSource.this.mTAG;
                    Log.d(access$00010, "[HDR] [handleMessage] MSG_PERFORM_ZOOM [" + msg.arg1 + "]");
                    if (this.mCameraDevice == null || this.mCameraCaptureSession == null) {
                        Log.w(VTSource.this.mTAG, "[HDR] [handleMessage] perform zoom with null device or session!!!");
                        return;
                    }
                    this.mZoomValue = ((Float) msg.obj).floatValue();
                    submitRepeatingRequest();
                    return;
                case 8:
                    String access$00011 = VTSource.this.mTAG;
                    Log.d(access$00011, "[HDR] [handleMessage] MSG_GET_CAMERA_CHARACTERISTICS [" + msg.arg1 + "]");
                    ((CameraCharacteristics[]) msg.obj)[0] = this.mCameraCharacteristics;
                    return;
                case 9:
                    String access$00012 = VTSource.this.mTAG;
                    Log.d(access$00012, "[HDR] [handleMessage] MSG_CLOSE_CAMERA [" + msg.arg1 + "]");
                    this.mCameraCaptureSession = null;
                    this.mZoomValue = 1.0f;
                    doCloseCamera(false);
                    return;
                case 10:
                    String access$00013 = VTSource.this.mTAG;
                    Log.d(access$00013, "[HDR] [handleMessage] MSG_RELEASE [" + msg.arg1 + "]");
                    this.mCameraCaptureSession = null;
                    this.mZoomValue = 1.0f;
                    doCloseCamera(false);
                    this.mSessionUsedSurfaceList.clear();
                    this.mOutputConfigurations.clear();
                    Surface unused8 = VTSource.this.mCachedRecordSurface = null;
                    Surface unused9 = VTSource.this.mCachedPreviewSurface = null;
                    this.mRespondThread.quitSafely();
                    return;
                case 11:
                    String access$00014 = VTSource.this.mTAG;
                    Log.d(access$00014, "[HDR] [handleMessage] MSG_DEVICE_ORIENTATION [" + msg.arg1 + "]");
                    if (this.mDeviceDegree != ((Integer) msg.obj).intValue()) {
                        String access$00015 = VTSource.this.mTAG;
                        Log.d(access$00015, "[HDR] [handleMessage] Change device orientation from " + this.mDeviceDegree + "to " + ((Integer) msg.obj).intValue());
                        this.mDeviceDegree = ((Integer) msg.obj).intValue();
                        return;
                    }
                    return;
                case 12:
                    String access$00016 = VTSource.this.mTAG;
                    Log.d(access$00016, "[HDR] [handleMessage] MSG_RESTART_CAMERA [" + msg.arg1 + "]");
                    closeSession();
                    doCloseCamera(true);
                    openCamera(this.mCameraId);
                    return;
                case 13:
                    String access$00017 = VTSource.this.mTAG;
                    Log.d(access$00017, "[HDR] [handleMessage] MSG_STOP_RECORDING_PREVIEW [" + msg.arg1 + "]");
                    if (VTSource.this.mNeedRecordStream) {
                        boolean unused10 = VTSource.this.mNeedRecordStream = false;
                        closeSession();
                        return;
                    }
                    return;
                case 14:
                    String access$00018 = VTSource.this.mTAG;
                    Log.d(access$00018, "[HDR] [handleMessage] MSG_SET_PAUSE_IMAGE [" + msg.arg1 + "]");
                    doSetPauseImage((Uri) msg.obj);
                    return;
                default:
                    String access$00019 = VTSource.this.mTAG;
                    Log.d(access$00019, "[HDR] [handleMessage] what:" + msg.what + " [" + msg.arg1 + "]");
                    return;
            }
        }

        private void createSession() {
            Log.d(VTSource.this.mTAG, "[HDR] [createSession] Start");
            if (this.mCameraDevice == null) {
                Log.w(VTSource.this.mTAG, "[HDR] [createSession] mCameraDevice is null !!!");
                return;
            }
            boolean ret = prepareOutputConfiguration();
            if (this.mSessionUsedSurfaceList.size() <= 0 || !ret) {
                Log.w(VTSource.this.mTAG, "[HDR] [createSession] Session surface list size <=0 or prepareOutputConfiguration fail");
                return;
            }
            SessionConfiguration sessionConfigByOutput = new SessionConfiguration(0, this.mOutputConfigurations, new HandlerExecutor(new Handler(this.mRespondThread.getLooper())), this.mSessionCallback);
            Log.d(VTSource.this.mTAG, "[HDR] [createSession] Create sessionConfig");
            CaptureRequest.Builder requestBuilder = makeRequestBuilder();
            if (requestBuilder == null) {
                Log.w(VTSource.this.mTAG, "[HDR] [createSession] requestBuilder == null");
                this.mEventCallBack.onError();
                return;
            }
            sessionConfigByOutput.setSessionParameters(requestBuilder.build());
            this.mSessionConditionVariable.close();
            try {
                this.mCameraDevice.createCaptureSession(sessionConfigByOutput);
                this.mSessionConditionVariable.block();
                Log.d(VTSource.this.mTAG, "[HDR] [createSession] Finish");
            } catch (Exception e) {
                String access$000 = VTSource.this.mTAG;
                Log.e(access$000, "[HDR] [createSession] create preview session with exception:" + e);
                if (!VTSource.this.mIsWaitRelease) {
                    this.mEventCallBack.onError();
                } else {
                    Log.d(VTSource.this.mTAG, "mIsWaitRelease means call end or VTS error, ignore MA error");
                }
            }
        }

        private int getSessionRotationIndex(int rotation) {
            String access$000 = VTSource.this.mTAG;
            Log.d(access$000, "[HDR] [getSessionRotationIndex] rotation = " + rotation);
            switch (rotation) {
                case 0:
                    return 0;
                case NgranBands.BAND_90:
                    return 1;
                case SipMessage.CODE_SESSION_RINGING:
                    return 2;
                case 270:
                    return 3;
                default:
                    return 0;
            }
        }

        private void closeSession() {
            Log.d(VTSource.this.mTAG, "[HDR] [closeSession] Start");
            CameraCaptureSession cameraCaptureSession = this.mCameraCaptureSession;
            if (cameraCaptureSession != null) {
                try {
                    cameraCaptureSession.abortCaptures();
                    this.mCameraCaptureSession.close();
                    this.mCameraCaptureSession = null;
                } catch (CameraAccessException e) {
                    Log.e(VTSource.TAG, "[HDR] [closeSession] exception", e);
                } catch (IllegalStateException e2) {
                    Log.e(VTSource.TAG, "[HDR] [closeSession] exception", e2);
                    e2.printStackTrace();
                }
            } else {
                Log.d(VTSource.this.mTAG, "[HDR] [closeSession] mCameraCaptureSession = NULL");
            }
            Log.d(VTSource.this.mTAG, "[HDR] [closeSession] Finish");
        }

        private Rect calculateCropRegionByZoomValue(float zoomValue) {
            String access$000 = VTSource.this.mTAG;
            Log.d(access$000, "[HDR] [calculateCropRegionByZoomValue] Start, zoomValue = " + zoomValue);
            Log.d(VTSource.this.mTAG, "[HDR] [calculateCropRegionByZoomValue] Finish");
            return getCropRegionForZoom(zoomValue, new PointF(0.5f, 0.5f), ((Float) this.mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue(), (Rect) this.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE));
        }

        private Range calculateAeFpsRange() {
            Log.d(VTSource.this.mTAG, "[HDR] [calculateAeFpsRange] Start");
            if (ImsVTProviderUtil.isVideoQualityTestMode()) {
                Range<Integer> preSetFps = new Range<>(30, 30);
                Log.d(VTSource.this.mTAG, "[HDR] [calculateAeFpsRange] for VQ test, Range = [" + preSetFps.getLower() + ", " + preSetFps.getUpper() + "], Finish");
                return preSetFps;
            }
            Range<Integer>[] availableFpsRange = (Range[]) this.mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Range<Integer> bestRange = availableFpsRange[0];
            for (Range<Integer> r : availableFpsRange) {
                if (bestRange.getUpper().intValue() < r.getUpper().intValue()) {
                    bestRange = r;
                } else if (bestRange.getUpper() == r.getUpper() && bestRange.getLower().intValue() > r.getLower().intValue()) {
                    bestRange = r;
                }
            }
            Log.d(VTSource.this.mTAG, "[HDR] [calculateAeFpsRange] Finish, Range = [" + bestRange.getLower() + ", " + bestRange.getUpper() + "]");
            return bestRange;
        }

        /* JADX WARNING: Removed duplicated region for block: B:23:0x00ef A[Catch:{ Exception -> 0x010e }] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private android.hardware.camera2.CaptureRequest.Builder makeRequestBuilder() {
            /*
                r8 = this;
                com.mediatek.ims.internal.VTSource r0 = com.mediatek.ims.internal.VTSource.this
                java.lang.String r0 = r0.mTAG
                java.lang.String r1 = "[HDR] [makeRequestBuilder] Start"
                android.util.Log.d(r0, r1)
                r0 = 0
                r8.mHasAddTarget = r0
                r1 = 0
                android.hardware.camera2.CameraDevice r2 = r8.mCameraDevice     // Catch:{ Exception -> 0x010e }
                r3 = 3
                android.hardware.camera2.CaptureRequest$Builder r2 = r2.createCaptureRequest(r3)     // Catch:{ Exception -> 0x010e }
                r1 = r2
                float r2 = r8.mZoomValue     // Catch:{ Exception -> 0x010e }
                android.graphics.Rect r2 = r8.calculateCropRegionByZoomValue(r2)     // Catch:{ Exception -> 0x010e }
                android.hardware.camera2.CaptureRequest$Key r3 = android.hardware.camera2.CaptureRequest.SCALER_CROP_REGION     // Catch:{ Exception -> 0x010e }
                r1.set(r3, r2)     // Catch:{ Exception -> 0x010e }
                android.util.Range r3 = r8.calculateAeFpsRange()     // Catch:{ Exception -> 0x010e }
                android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE     // Catch:{ Exception -> 0x010e }
                r1.set(r4, r3)     // Catch:{ Exception -> 0x010e }
                boolean r4 = com.mediatek.ims.internal.ImsVTProviderUtil.is512mbProject()     // Catch:{ Exception -> 0x010e }
                r5 = 1
                if (r4 != 0) goto L_0x0058
                boolean r4 = com.mediatek.ims.internal.ImsVTProviderUtil.isVideoQualityTestMode()     // Catch:{ Exception -> 0x010e }
                if (r4 == 0) goto L_0x0039
                goto L_0x0058
            L_0x0039:
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                java.lang.String r4 = r4.mTAG     // Catch:{ Exception -> 0x010e }
                java.lang.String r6 = "[HDR] [makeRequestBuilder] Turn on face detection"
                android.util.Log.d(r4, r6)     // Catch:{ Exception -> 0x010e }
                android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.CONTROL_MODE     // Catch:{ Exception -> 0x010e }
                r6 = 2
                java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ Exception -> 0x010e }
                r1.set(r4, r6)     // Catch:{ Exception -> 0x010e }
                android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.CONTROL_SCENE_MODE     // Catch:{ Exception -> 0x010e }
                java.lang.Integer r6 = java.lang.Integer.valueOf(r5)     // Catch:{ Exception -> 0x010e }
                r1.set(r4, r6)     // Catch:{ Exception -> 0x010e }
                goto L_0x0063
            L_0x0058:
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                java.lang.String r4 = r4.mTAG     // Catch:{ Exception -> 0x010e }
                java.lang.String r6 = "[HDR] [makeRequestBuilder] 512MB project or VQtest,turn off face detection"
                android.util.Log.d(r4, r6)     // Catch:{ Exception -> 0x010e }
            L_0x0063:
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                java.lang.String r4 = r4.mTAG     // Catch:{ Exception -> 0x010e }
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x010e }
                r6.<init>()     // Catch:{ Exception -> 0x010e }
                java.lang.String r7 = "[HDR] [makeRequestBuilder] Add target mNeedRecordStream = "
                r6.append(r7)     // Catch:{ Exception -> 0x010e }
                com.mediatek.ims.internal.VTSource r7 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                boolean r7 = r7.mNeedRecordStream     // Catch:{ Exception -> 0x010e }
                r6.append(r7)     // Catch:{ Exception -> 0x010e }
                java.lang.String r7 = ", mCachedRecordSurface = "
                r6.append(r7)     // Catch:{ Exception -> 0x010e }
                com.mediatek.ims.internal.VTSource r7 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r7 = r7.mCachedRecordSurface     // Catch:{ Exception -> 0x010e }
                r6.append(r7)     // Catch:{ Exception -> 0x010e }
                java.lang.String r7 = ", mCachedPreviewSurface = "
                r6.append(r7)     // Catch:{ Exception -> 0x010e }
                com.mediatek.ims.internal.VTSource r7 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r7 = r7.mCachedPreviewSurface     // Catch:{ Exception -> 0x010e }
                r6.append(r7)     // Catch:{ Exception -> 0x010e }
                java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x010e }
                android.util.Log.d(r4, r6)     // Catch:{ Exception -> 0x010e }
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                boolean r4 = r4.mNeedRecordStream     // Catch:{ Exception -> 0x010e }
                if (r4 == 0) goto L_0x00c8
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r4 = r4.mCachedRecordSurface     // Catch:{ Exception -> 0x010e }
                if (r4 == 0) goto L_0x00c8
                java.util.List<android.view.Surface> r4 = r8.mSessionUsedSurfaceList     // Catch:{ Exception -> 0x010e }
                com.mediatek.ims.internal.VTSource r6 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r6 = r6.mCachedRecordSurface     // Catch:{ Exception -> 0x010e }
                boolean r4 = r4.contains(r6)     // Catch:{ Exception -> 0x010e }
                if (r4 == 0) goto L_0x00c8
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r4 = r4.mCachedRecordSurface     // Catch:{ Exception -> 0x010e }
                r1.addTarget(r4)     // Catch:{ Exception -> 0x010e }
                r8.mHasAddTarget = r5     // Catch:{ Exception -> 0x010e }
            L_0x00c8:
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r4 = r4.mCachedPreviewSurface     // Catch:{ Exception -> 0x010e }
                if (r4 == 0) goto L_0x00e9
                java.util.List<android.view.Surface> r4 = r8.mSessionUsedSurfaceList     // Catch:{ Exception -> 0x010e }
                com.mediatek.ims.internal.VTSource r6 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r6 = r6.mCachedPreviewSurface     // Catch:{ Exception -> 0x010e }
                boolean r4 = r4.contains(r6)     // Catch:{ Exception -> 0x010e }
                if (r4 == 0) goto L_0x00e9
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                android.view.Surface r4 = r4.mCachedPreviewSurface     // Catch:{ Exception -> 0x010e }
                r1.addTarget(r4)     // Catch:{ Exception -> 0x010e }
                r8.mHasAddTarget = r5     // Catch:{ Exception -> 0x010e }
            L_0x00e9:
                boolean r4 = com.mediatek.ims.internal.ImsVTProviderUtil.isVideoQualityTestMode()     // Catch:{ Exception -> 0x010e }
                if (r4 == 0) goto L_0x0104
                com.mediatek.ims.internal.VTSource r4 = com.mediatek.ims.internal.VTSource.this     // Catch:{ Exception -> 0x010e }
                java.lang.String r4 = r4.mTAG     // Catch:{ Exception -> 0x010e }
                java.lang.String r5 = "[HDR] [makeRequestBuilder] set CONTINUOUS_PICTURE"
                android.util.Log.d(r4, r5)     // Catch:{ Exception -> 0x010e }
                android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE     // Catch:{ Exception -> 0x010e }
                r5 = 4
                java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ Exception -> 0x010e }
                r1.set(r4, r5)     // Catch:{ Exception -> 0x010e }
            L_0x0104:
                android.hardware.camera2.CaptureRequest$Key r4 = android.hardware.camera2.CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE     // Catch:{ Exception -> 0x010e }
                java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ Exception -> 0x010e }
                r1.set(r4, r0)     // Catch:{ Exception -> 0x010e }
                goto L_0x0131
            L_0x010e:
                r0 = move-exception
                com.mediatek.ims.internal.VTSource r2 = com.mediatek.ims.internal.VTSource.this
                java.lang.String r2 = r2.mTAG
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r4 = "[HDR] [makeRequestBuilder] exception: "
                r3.append(r4)
                r3.append(r0)
                java.lang.String r3 = r3.toString()
                android.util.Log.d(r2, r3)
                r0.printStackTrace()
                com.mediatek.ims.internal.VTSource$EventCallback r2 = r8.mEventCallBack
                r2.onError()
            L_0x0131:
                com.mediatek.ims.internal.VTSource r0 = com.mediatek.ims.internal.VTSource.this
                java.lang.String r0 = r0.mTAG
                java.lang.String r2 = "[HDR] [makeRequestBuilder] Finish"
                android.util.Log.d(r0, r2)
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.internal.VTSource.DeviceHandler.makeRequestBuilder():android.hardware.camera2.CaptureRequest$Builder");
        }

        private void submitRepeatingRequest() {
            Log.d(VTSource.this.mTAG, "[HDR] [submitRepeatingRequest] Start");
            if (this.mCameraDevice == null || this.mCameraCaptureSession == null) {
                Log.w(VTSource.this.mTAG, "submitRepeatingRequest illegal state, ignore!");
                return;
            }
            CaptureRequest.Builder requestBuilder = makeRequestBuilder();
            if (requestBuilder == null) {
                Log.w(VTSource.this.mTAG, "submitRepeatingRequest requestBuilder == null");
                this.mEventCallBack.onError();
                return;
            }
            try {
                if (this.mHasAddTarget) {
                    this.mCameraCaptureSession.setRepeatingRequest(requestBuilder.build(), (CameraCaptureSession.CaptureCallback) null, new Handler(this.mRespondThread.getLooper()));
                }
                if (ImsVTProviderUtil.isVideoQualityTestMode()) {
                    Log.d(VTSource.this.mTAG, "[HDR] [submitRepeatingRequest] trigger set focus once");
                    requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, 1);
                    this.mCameraCaptureSession.capture(requestBuilder.build(), (CameraCaptureSession.CaptureCallback) null, new Handler(this.mRespondThread.getLooper()));
                }
            } catch (Exception e) {
                String access$000 = VTSource.this.mTAG;
                Log.d(access$000, "[HDR] [submitRepeatingRequest] exception: " + e);
                e.printStackTrace();
                this.mEventCallBack.onError();
            }
            Log.d(VTSource.this.mTAG, "[HDR] [submitRepeatingRequest] Finish");
        }

        private void prepareForOpenCamera(String cameraId) {
            String str;
            String access$000 = VTSource.this.mTAG;
            Log.d(access$000, "[HDR] [prepareForOpenCamera] Start, cameraId = " + cameraId);
            if (!(this.mCameraDevice == null || (str = this.mCameraId) == null || str.equals(cameraId))) {
                closeSession();
                doCloseCamera(true);
            }
            this.mCameraId = cameraId;
            try {
                this.mCameraCharacteristics = VTSource.this.mCameraManager.getCameraCharacteristics(this.mCameraId);
            } catch (Exception e) {
                String access$0002 = VTSource.this.mTAG;
                Log.e(access$0002, "[HDR] [prepareForOpenCamera] before open camera getCameraCharacteristics access exception: " + e);
                this.mEventCallBack.onError();
            }
            Log.d(VTSource.this.mTAG, "[HDR] [prepareForOpenCamera] Finish");
        }

        private void doCloseCamera(boolean needWaitComplete) {
            Log.d(VTSource.this.mTAG, "[HDR] [doCloseCamera] Start");
            if (this.mCameraDevice != null) {
                if (needWaitComplete) {
                    this.mDeviceConditionVariable.close();
                }
                this.mCameraDevice.close();
                this.mCameraDevice = null;
                if (needWaitComplete) {
                    this.mDeviceConditionVariable.block();
                }
            } else {
                Log.d(VTSource.this.mTAG, "[HDR] [doCloseCamera] mCameraDevice = NULL");
            }
            Log.d(VTSource.this.mTAG, "[HDR] [doCloseCamera] Finish");
        }

        private boolean prepareOutputConfiguration() {
            Log.d(VTSource.this.mTAG, "[HDR] [prepareOutputConfiguration] Start");
            this.mSessionUsedSurfaceList.clear();
            this.mOutputConfigurations.clear();
            if (VTSource.this.mCachedPreviewSurface != null) {
                Log.d(VTSource.this.mTAG, "[HDR] [prepareOutputConfiguration][Preview]");
                this.mSessionUsedSurfaceList.add(VTSource.this.mCachedPreviewSurface);
                try {
                    this.mOutputConfigurations.add(new OutputConfiguration(VTSource.this.mCachedPreviewSurface));
                } catch (Exception ex) {
                    String access$000 = VTSource.this.mTAG;
                    Log.e(access$000, "[HDR] [prepareOutputConfiguration][Preview] new OutputConfiguration with exception: " + ex);
                    this.mSessionUsedSurfaceList.remove(VTSource.this.mCachedPreviewSurface);
                    Surface unused = VTSource.this.mCachedPreviewSurface = null;
                    this.mEventCallBack.onError();
                    Log.d(VTSource.this.mTAG, "[HDR] [prepareOutputConfiguration] Finish");
                    return false;
                }
            }
            if (VTSource.this.mCachedRecordSurface != null) {
                Log.d(VTSource.this.mTAG, "[HDR] [prepareOutputConfiguration][Record]");
                this.mSessionUsedSurfaceList.add(VTSource.this.mCachedRecordSurface);
                try {
                    this.mOutputConfigurations.add(new OutputConfiguration(VTSource.this.mCachedRecordSurface));
                } catch (Exception ex2) {
                    String access$0002 = VTSource.this.mTAG;
                    Log.e(access$0002, "[HDR] [prepareOutputConfiguration][Record] new OutputConfiguration with exception: " + ex2);
                    this.mSessionUsedSurfaceList.remove(VTSource.this.mCachedRecordSurface);
                    Surface unused2 = VTSource.this.mCachedRecordSurface = null;
                    this.mEventCallBack.onError();
                    Log.d(VTSource.this.mTAG, "[HDR] [prepareOutputConfiguration] Finish");
                    return false;
                }
            }
            Log.d(VTSource.this.mTAG, "[HDR] [prepareOutputConfiguration] Finish");
            return true;
        }

        private void openCamera(String cameraId) {
            Log.d(VTSource.this.mTAG, "[HDR] [openCamera] Start");
            CameraDevice cameraDevice = this.mCameraDevice;
            if (cameraDevice == null || !cameraDevice.getId().equals(cameraId)) {
                prepareForOpenCamera(cameraId);
                this.mRetryCount = 0;
                boolean unused = VTSource.this.mStopPreviewAndRecord = false;
                this.mDeviceConditionVariable.close();
                doOpenCamera(this.mCameraId);
                this.mDeviceConditionVariable.block();
                Log.d(VTSource.this.mTAG, "[HDR] [openCamera] Finish");
                return;
            }
            Log.w(VTSource.this.mTAG, "open existing camera, ignore open!!!");
        }

        /* access modifiers changed from: private */
        public void doOpenCamera(String cameraId) {
            Log.d(VTSource.this.mTAG, "[HDR] [doOpenCamera] Start");
            try {
                VTSource.this.mCameraManager.openCamera(cameraId, this.mDeviceCallback, new Handler(this.mRespondThread.getLooper()));
            } catch (Exception e) {
                String access$000 = VTSource.this.mTAG;
                Log.i(access$000, "[HDR] [doOpenCamera] open camera with access exception:" + e);
                this.mDeviceConditionVariable.open();
                this.mEventCallBack.onError();
            }
            Log.d(VTSource.this.mTAG, "[HDR] [doOpenCamera] Finish");
        }

        private int getCameraRotation(int degrees, CameraCharacteristics characteristics) {
            int result;
            Log.d(VTSource.this.mTAG, "[HDR] [getCameraRotation] Start");
            int facing = ((Integer) characteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
            int orientation = ((Integer) characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
            String access$000 = VTSource.this.mTAG;
            Log.d(access$000, "[HDR] [getCameraRotation] degrees: " + degrees + ", facing: " + facing + ", orientation: " + orientation);
            if (facing != 0) {
                switch (degrees) {
                    case 0:
                        result = 0;
                        break;
                    case NgranBands.BAND_90:
                        result = 90;
                        break;
                    case SipMessage.CODE_SESSION_RINGING:
                        result = SipMessage.CODE_SESSION_RINGING;
                        break;
                    case 270:
                        result = 270;
                        break;
                    default:
                        result = 0;
                        break;
                }
            } else {
                switch (degrees) {
                    case 0:
                        result = 0;
                        break;
                    case NgranBands.BAND_90:
                        result = 270;
                        break;
                    case SipMessage.CODE_SESSION_RINGING:
                        result = SipMessage.CODE_SESSION_RINGING;
                        break;
                    case 270:
                        result = 90;
                        break;
                    default:
                        result = 0;
                        break;
                }
            }
            String access$0002 = VTSource.this.mTAG;
            Log.d(access$0002, "[HDR] [getCameraRotation] Fisnish, Final angle = " + result);
            return result;
        }

        private Rect getCropRegionForZoom(float zoomFactor, PointF center, float maxZoom, Rect activeArray) {
            String access$000 = VTSource.this.mTAG;
            Log.d(access$000, "[HDR] [getCropRegionForZoom] Start, zoomFactor = " + zoomFactor + ", center = " + center + ", maxZoom = " + maxZoom + ", activeArray = " + activeArray);
            if (((double) zoomFactor) < 1.0d) {
                throw new IllegalArgumentException("zoom factor " + zoomFactor + " should be >= 1.0");
            } else if (((double) center.x) > 1.0d || center.x < 0.0f) {
                throw new IllegalArgumentException("center.x " + center.x + " should be in range of [0, 1.0]");
            } else if (((double) center.y) > 1.0d || center.y < 0.0f) {
                throw new IllegalArgumentException("center.y " + center.y + " should be in range of [0, 1.0]");
            } else if (((double) maxZoom) < 1.0d) {
                throw new IllegalArgumentException("max zoom factor " + maxZoom + " should be >= 1.0");
            } else if (activeArray != null) {
                float minEffectiveZoom = 0.5f / Math.min(Math.min(center.x, 1.0f - center.x), Math.min(center.y, 1.0f - center.y));
                if (minEffectiveZoom <= maxZoom) {
                    if (zoomFactor < minEffectiveZoom) {
                        String access$0002 = VTSource.this.mTAG;
                        Log.w(access$0002, "Requested zoomFactor " + zoomFactor + " > minimal zoomable factor " + minEffectiveZoom + ". It will be overwritten by " + minEffectiveZoom);
                        zoomFactor = minEffectiveZoom;
                    }
                    int cropCenterX = (int) (((float) activeArray.width()) * center.x);
                    int cropCenterY = (int) (((float) activeArray.height()) * center.y);
                    int cropWidth = (int) (((float) activeArray.width()) / zoomFactor);
                    int cropHeight = (int) (((float) activeArray.height()) / zoomFactor);
                    Log.d(VTSource.this.mTAG, "[HDR] [getCropRegionForZoom] Finish");
                    return new Rect(cropCenterX - (cropWidth / 2), cropCenterY - (cropHeight / 2), ((cropWidth / 2) + cropCenterX) - 1, ((cropHeight / 2) + cropCenterY) - 1);
                }
                throw new IllegalArgumentException("Requested center " + center.toString() + " has minimal zoomable factor " + minEffectiveZoom + ", which exceeds max zoom factor " + maxZoom);
            } else {
                throw new IllegalArgumentException("activeArray must not be null");
            }
        }

        private void doSetPauseImage(Uri uri) {
            Log.d(VTSource.this.mTAG, "[HDR] [doSetPauseImage] Start");
            if (VTSource.this.mCachedPauseImageSurface == null) {
                Log.d(VTSource.this.mTAG, "no surface for picture, return");
            }
            InputStream imageStream = null;
            try {
                imageStream = VTSource.sContext.getContentResolver().openInputStream(uri);
                if (imageStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    if (imageStream != null) {
                        try {
                            imageStream.close();
                        } catch (IOException e) {
                            Log.e(VTSource.this.mTAG, "can not close imageStream");
                        }
                    }
                    if (VTSource.this.mCachedPauseImageSurface == null) {
                        Log.d(VTSource.this.mTAG, "mCachedPauseImageSurface, skip");
                        return;
                    }
                    Canvas canvas = VTSource.this.mCachedPauseImageSurface.lockCanvas((Rect) null);
                    int cavasWidth = canvas.getWidth();
                    int cavasHeight = canvas.getHeight();
                    String access$000 = VTSource.this.mTAG;
                    Log.d(access$000, "srcWidth=" + bitmap.getWidth() + " srcHeight=" + bitmap.getHeight() + " dstWidth=" + VTSource.this.mPauseImageWidth + " dstHeight=" + VTSource.this.mPauseImageHeight + "cavasWidth=" + cavasWidth + ", cavasHeight=" + cavasHeight);
                    canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, VTSource.this.mPauseImageWidth, VTSource.this.mPauseImageHeight), (Paint) null);
                    VTSource.this.mCachedPauseImageSurface.unlockCanvasAndPost(canvas);
                    VTSource.this.mCachedPauseImageSurface.release();
                    Surface unused = VTSource.this.mCachedPauseImageSurface = null;
                    Log.d(VTSource.this.mTAG, "[HDR] [doSetPauseImage] Finish");
                    return;
                }
                Log.e(VTSource.this.mTAG, "imageStream is null");
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e2) {
                        Log.e(VTSource.this.mTAG, "can not close imageStream");
                    }
                }
            } catch (FileNotFoundException e3) {
                Log.e(VTSource.this.mTAG, "can not find the file");
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e4) {
                        Log.e(VTSource.this.mTAG, "can not close imageStream");
                    }
                }
            } catch (Throwable th) {
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e5) {
                        Log.e(VTSource.this.mTAG, "can not close imageStream");
                    }
                }
                throw th;
            }
        }
    }

    private boolean waitDone(Handler handler) {
        if (handler == null) {
            return false;
        }
        final ConditionVariable waitDoneCondition = new ConditionVariable();
        Runnable unlockRunnable = new Runnable() {
            public void run() {
                synchronized (waitDoneCondition) {
                    waitDoneCondition.open();
                }
            }
        };
        synchronized (waitDoneCondition) {
            if (!handler.post(unlockRunnable)) {
                return true;
            }
            waitDoneCondition.block();
            return true;
        }
    }
}
