package com.mediatek.ims.rcsua.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.ims.rcsua.RegistrationInfo;

public interface IImsEventCallback extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.ims.rcsua.service.IImsEventCallback";

    void onDeregStarted(RegistrationInfo registrationInfo) throws RemoteException;

    void onReregistered(RegistrationInfo registrationInfo) throws RemoteException;

    void onStatusChanged(RegistrationInfo registrationInfo) throws RemoteException;

    void onVopsIndication(int i) throws RemoteException;

    public static class Default implements IImsEventCallback {
        public void onStatusChanged(RegistrationInfo regInfo) throws RemoteException {
        }

        public void onReregistered(RegistrationInfo regInfo) throws RemoteException {
        }

        public void onDeregStarted(RegistrationInfo regInfo) throws RemoteException {
        }

        public void onVopsIndication(int vops) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsEventCallback {
        static final int TRANSACTION_onDeregStarted = 3;
        static final int TRANSACTION_onReregistered = 2;
        static final int TRANSACTION_onStatusChanged = 1;
        static final int TRANSACTION_onVopsIndication = 4;

        public Stub() {
            attachInterface(this, IImsEventCallback.DESCRIPTOR);
        }

        public static IImsEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IImsEventCallback.DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsEventCallback)) {
                return new Proxy(obj);
            }
            return (IImsEventCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RegistrationInfo _arg0;
            RegistrationInfo _arg02;
            RegistrationInfo _arg03;
            switch (code) {
                case 1598968902:
                    reply.writeString(IImsEventCallback.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            data.enforceInterface(IImsEventCallback.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg0 = RegistrationInfo.CREATOR.createFromParcel(data);
                            } else {
                                _arg0 = null;
                            }
                            onStatusChanged(_arg0);
                            return true;
                        case 2:
                            data.enforceInterface(IImsEventCallback.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg02 = RegistrationInfo.CREATOR.createFromParcel(data);
                            } else {
                                _arg02 = null;
                            }
                            onReregistered(_arg02);
                            return true;
                        case 3:
                            data.enforceInterface(IImsEventCallback.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg03 = RegistrationInfo.CREATOR.createFromParcel(data);
                            } else {
                                _arg03 = null;
                            }
                            onDeregStarted(_arg03);
                            return true;
                        case 4:
                            data.enforceInterface(IImsEventCallback.DESCRIPTOR);
                            onVopsIndication(data.readInt());
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IImsEventCallback {
            public static IImsEventCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IImsEventCallback.DESCRIPTOR;
            }

            public void onStatusChanged(RegistrationInfo regInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IImsEventCallback.DESCRIPTOR);
                    if (regInfo != null) {
                        _data.writeInt(1);
                        regInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStatusChanged(regInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            public void onReregistered(RegistrationInfo regInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IImsEventCallback.DESCRIPTOR);
                    if (regInfo != null) {
                        _data.writeInt(1);
                        regInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReregistered(regInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            public void onDeregStarted(RegistrationInfo regInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IImsEventCallback.DESCRIPTOR);
                    if (regInfo != null) {
                        _data.writeInt(1);
                        regInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDeregStarted(regInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            public void onVopsIndication(int vops) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IImsEventCallback.DESCRIPTOR);
                    _data.writeInt(vops);
                    if (this.mRemote.transact(4, _data, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVopsIndication(vops);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsEventCallback impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IImsEventCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
