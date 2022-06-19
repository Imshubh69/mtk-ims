package com.mediatek.rcs.provisioning;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.rcs.provisioning.IAcsCallback;

public interface IAcsService extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.rcs.provisioning.IAcsService";

    boolean clearAcsConfiguration() throws RemoteException;

    int getAcsConfigInt(String str) throws RemoteException;

    String getAcsConfigString(String str) throws RemoteException;

    AcsConfigInfo getAcsConfiguration() throws RemoteException;

    int getAcsConnectionStatus() throws RemoteException;

    boolean getAcsSwitchState() throws RemoteException;

    void registerAcsCallback(IAcsCallback iAcsCallback) throws RemoteException;

    boolean setAcsMsisdn(String str) throws RemoteException;

    boolean setAcsProvisioningAddress(String str) throws RemoteException;

    boolean setAcsSwitchState(boolean z) throws RemoteException;

    void setAcsonfiguration(String str, String str2, String str3, String str4) throws RemoteException;

    void setRcsSwitchState(boolean z) throws RemoteException;

    boolean triggerAcsRequest(int i) throws RemoteException;

    void unregisterAcsCallback(IAcsCallback iAcsCallback) throws RemoteException;

    public static class Default implements IAcsService {
        public AcsConfigInfo getAcsConfiguration() throws RemoteException {
            return null;
        }

        public int getAcsConfigInt(String configItem) throws RemoteException {
            return 0;
        }

        public String getAcsConfigString(String configItem) throws RemoteException {
            return null;
        }

        public void setRcsSwitchState(boolean state) throws RemoteException {
        }

        public int getAcsConnectionStatus() throws RemoteException {
            return 0;
        }

        public boolean triggerAcsRequest(int reason) throws RemoteException {
            return false;
        }

        public void registerAcsCallback(IAcsCallback callback) throws RemoteException {
        }

        public void unregisterAcsCallback(IAcsCallback callback) throws RemoteException {
        }

        public boolean setAcsSwitchState(boolean state) throws RemoteException {
            return false;
        }

        public void setAcsonfiguration(String rcsVersion, String rcsProfile, String clientVendor, String clientVersion) throws RemoteException {
        }

        public boolean getAcsSwitchState() throws RemoteException {
            return false;
        }

        public boolean setAcsProvisioningAddress(String address) throws RemoteException {
            return false;
        }

        public boolean clearAcsConfiguration() throws RemoteException {
            return false;
        }

        public boolean setAcsMsisdn(String msisdn) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAcsService {
        static final int TRANSACTION_clearAcsConfiguration = 13;
        static final int TRANSACTION_getAcsConfigInt = 2;
        static final int TRANSACTION_getAcsConfigString = 3;
        static final int TRANSACTION_getAcsConfiguration = 1;
        static final int TRANSACTION_getAcsConnectionStatus = 5;
        static final int TRANSACTION_getAcsSwitchState = 11;
        static final int TRANSACTION_registerAcsCallback = 7;
        static final int TRANSACTION_setAcsMsisdn = 14;
        static final int TRANSACTION_setAcsProvisioningAddress = 12;
        static final int TRANSACTION_setAcsSwitchState = 9;
        static final int TRANSACTION_setAcsonfiguration = 10;
        static final int TRANSACTION_setRcsSwitchState = 4;
        static final int TRANSACTION_triggerAcsRequest = 6;
        static final int TRANSACTION_unregisterAcsCallback = 8;

        public Stub() {
            attachInterface(this, IAcsService.DESCRIPTOR);
        }

        public static IAcsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IAcsService.DESCRIPTOR);
            if (iin == null || !(iin instanceof IAcsService)) {
                return new Proxy(obj);
            }
            return (IAcsService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(IAcsService.DESCRIPTOR);
                    return true;
                default:
                    boolean _arg0 = false;
                    switch (code) {
                        case 1:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            AcsConfigInfo _result = getAcsConfiguration();
                            reply.writeNoException();
                            if (_result != null) {
                                reply.writeInt(1);
                                _result.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        case 2:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            int _result2 = getAcsConfigInt(data.readString());
                            reply.writeNoException();
                            reply.writeInt(_result2);
                            return true;
                        case 3:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            String _result3 = getAcsConfigString(data.readString());
                            reply.writeNoException();
                            reply.writeString(_result3);
                            return true;
                        case 4:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg0 = true;
                            }
                            setRcsSwitchState(_arg0);
                            reply.writeNoException();
                            return true;
                        case 5:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            int _result4 = getAcsConnectionStatus();
                            reply.writeNoException();
                            reply.writeInt(_result4);
                            return true;
                        case 6:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            boolean _result5 = triggerAcsRequest(data.readInt());
                            reply.writeNoException();
                            reply.writeInt(_result5);
                            return true;
                        case 7:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            registerAcsCallback(IAcsCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        case 8:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            unregisterAcsCallback(IAcsCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        case 9:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg0 = true;
                            }
                            boolean _result6 = setAcsSwitchState(_arg0);
                            reply.writeNoException();
                            reply.writeInt(_result6);
                            return true;
                        case 10:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            setAcsonfiguration(data.readString(), data.readString(), data.readString(), data.readString());
                            reply.writeNoException();
                            return true;
                        case 11:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            boolean _result7 = getAcsSwitchState();
                            reply.writeNoException();
                            reply.writeInt(_result7);
                            return true;
                        case 12:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            boolean _result8 = setAcsProvisioningAddress(data.readString());
                            reply.writeNoException();
                            reply.writeInt(_result8);
                            return true;
                        case 13:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            boolean _result9 = clearAcsConfiguration();
                            reply.writeNoException();
                            reply.writeInt(_result9);
                            return true;
                        case 14:
                            data.enforceInterface(IAcsService.DESCRIPTOR);
                            boolean _result10 = setAcsMsisdn(data.readString());
                            reply.writeNoException();
                            reply.writeInt(_result10);
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IAcsService {
            public static IAcsService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IAcsService.DESCRIPTOR;
            }

            public AcsConfigInfo getAcsConfiguration() throws RemoteException {
                AcsConfigInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAcsConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AcsConfigInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAcsConfigInt(String configItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeString(configItem);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAcsConfigInt(configItem);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAcsConfigString(String configItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeString(configItem);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAcsConfigString(configItem);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRcsSwitchState(boolean state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeInt(state ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRcsSwitchState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAcsConnectionStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAcsConnectionStatus();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean triggerAcsRequest(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeInt(reason);
                    boolean z = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().triggerAcsRequest(reason);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerAcsCallback(IAcsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerAcsCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterAcsCallback(IAcsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterAcsCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAcsSwitchState(boolean state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(state ? 1 : 0);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAcsSwitchState(state);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAcsonfiguration(String rcsVersion, String rcsProfile, String clientVendor, String clientVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeString(rcsVersion);
                    _data.writeString(rcsProfile);
                    _data.writeString(clientVendor);
                    _data.writeString(clientVersion);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAcsonfiguration(rcsVersion, rcsProfile, clientVendor, clientVersion);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getAcsSwitchState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAcsSwitchState();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAcsProvisioningAddress(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeString(address);
                    boolean z = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAcsProvisioningAddress(address);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearAcsConfiguration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearAcsConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAcsMsisdn(String msisdn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsService.DESCRIPTOR);
                    _data.writeString(msisdn);
                    boolean z = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAcsMsisdn(msisdn);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAcsService impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IAcsService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
