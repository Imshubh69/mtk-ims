package com.mediatek.ims.config;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import java.util.HashSet;

public abstract class Register {
    protected String argType;
    protected IArgsChangeListener mArgsListener;
    protected ArgsObserver mArgsObserver;
    protected Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HandlerThread mHandlerThread;
    protected int mPhoneId;
    protected HashSet<Integer> mRegArgs;

    public interface IArgsChangeListener {
        void onChange(int i);
    }

    private Register() {
        this.argType = null;
        this.mRegArgs = new HashSet<>();
        this.mContext = null;
        this.mArgsListener = null;
        this.mArgsObserver = null;
        this.mHandler = null;
        this.mHandlerThread = null;
    }

    public Register(Context context, int phoneId) {
        this(context, phoneId, (Handler) null);
    }

    public Register(Context context, int phoneId, Handler handler) {
        this.argType = null;
        this.mRegArgs = new HashSet<>();
        this.mContext = null;
        this.mArgsListener = null;
        this.mArgsObserver = null;
        this.mHandler = null;
        this.mHandlerThread = null;
        this.mContext = context;
        this.mPhoneId = phoneId;
        if (context == null) {
            throw new IllegalArgumentException("Null context!");
        } else if (handler != null) {
            this.mHandler = handler;
        } else {
            HandlerThread handlerThread = new HandlerThread("Ims" + this.argType + "Reg");
            this.mHandlerThread = handlerThread;
            handlerThread.start();
            this.mHandler = new Handler(this.mHandlerThread.getLooper());
        }
    }

    public Register addArg(int argId) throws IllegalArgumentException {
        this.mRegArgs.add(Integer.valueOf(argId));
        return this;
    }

    public void register(IArgsChangeListener listener) throws IllegalArgumentException {
        this.mArgsListener = listener;
        if (this.mRegArgs.isEmpty()) {
            throw new IllegalArgumentException("Empty " + this.argType + " for register");
        } else if (listener != null) {
            this.mArgsObserver = new ArgsObserver(this.mArgsListener);
        } else {
            throw new IllegalArgumentException("Null listener for " + this.argType + " register");
        }
    }

    public void unregister() {
        if (this.mArgsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mArgsObserver);
        }
    }

    public class ArgsObserver extends ContentObserver {
        private IArgsChangeListener mArgsObserver = null;

        public ArgsObserver(IArgsChangeListener listener) {
            super(Register.this.mHandler);
            this.mArgsObserver = listener;
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, (Uri) null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                String str = Register.this.argType;
                char c = 65535;
                switch (str.hashCode()) {
                    case -1354792126:
                        if (str.equals("config")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -979207434:
                        if (str.equals("feature")) {
                            c = 1;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        this.mArgsObserver.onChange(ImsConfigContract.configNameToId(uri.getLastPathSegment()));
                        return;
                    case 1:
                        this.mArgsObserver.onChange(Integer.parseInt(uri.getPathSegments().get(2)));
                        return;
                    default:
                        throw new RuntimeException("Invalid Register class: " + Register.this.argType);
                }
            }
        }
    }
}
