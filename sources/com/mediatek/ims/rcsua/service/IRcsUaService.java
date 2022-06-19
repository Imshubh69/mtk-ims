package com.mediatek.ims.rcsua.service;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.ims.rcsua.AcsConfiguration;
import com.mediatek.ims.rcsua.Capability;
import com.mediatek.ims.rcsua.service.IAcsEventCallback;
import com.mediatek.ims.rcsua.service.IImsEventCallback;
import com.mediatek.ims.rcsua.service.IRcsUaClient;

public interface IRcsUaService extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.ims.rcsua.service.IRcsUaService";

    void activate() throws RemoteException;

    void addCapability(Capability capability) throws RemoteException;

    boolean clearAcsConfiguration() throws RemoteException;

    void deactivate() throws RemoteException;

    int getAcsConfigInt(String str) throws RemoteException;

    String getAcsConfigString(String str) throws RemoteException;

    AcsConfiguration getAcsConfiguration() throws RemoteException;

    boolean getAcsSwitchState() throws RemoteException;

    Capability getCapabilities() throws RemoteException;

    Bundle getOptions() throws RemoteException;

    boolean isAcsConnected() throws RemoteException;

    boolean isActivated() throws RemoteException;

    void registerAcsCallback(IAcsEventCallback iAcsEventCallback) throws RemoteException;

    IRcsUaClient registerClient(IImsEventCallback iImsEventCallback) throws RemoteException;

    void removeCapability(Capability capability) throws RemoteException;

    boolean setAcsProvisioningAddress(String str) throws RemoteException;

    boolean setAcsSwitchState(boolean z) throws RemoteException;

    void setAcsonfiguration(String str, String str2, String str3, String str4) throws RemoteException;

    void setOptions(Bundle bundle) throws RemoteException;

    void triggerAcsRequest(int i) throws RemoteException;

    void triggerForceReregistration(Capability capability) throws RemoteException;

    void triggerReregistration(Capability capability) throws RemoteException;

    void triggerRestoration() throws RemoteException;

    void unregisterAcsCallback(IAcsEventCallback iAcsEventCallback) throws RemoteException;

    void unregisterClient(IRcsUaClient iRcsUaClient) throws RemoteException;

    void updateCapabilities(Capability capability) throws RemoteException;

    public static class Default implements IRcsUaService {
        public void activate() throws RemoteException {
        }

        public void deactivate() throws RemoteException {
        }

        public void triggerReregistration(Capability features) throws RemoteException {
        }

        public void triggerForceReregistration(Capability features) throws RemoteException {
        }

        public void triggerRestoration() throws RemoteException {
        }

        public void addCapability(Capability capability) throws RemoteException {
        }

        public void removeCapability(Capability capability) throws RemoteException {
        }

        public void updateCapabilities(Capability capabilities) throws RemoteException {
        }

        public IRcsUaClient registerClient(IImsEventCallback callback) throws RemoteException {
            return null;
        }

        public void unregisterClient(IRcsUaClient client) throws RemoteException {
        }

        public void setOptions(Bundle options) throws RemoteException {
        }

        public Bundle getOptions() throws RemoteException {
            return null;
        }

        public Capability getCapabilities() throws RemoteException {
            return null;
        }

        public boolean isActivated() throws RemoteException {
            return false;
        }

        public void registerAcsCallback(IAcsEventCallback callback) throws RemoteException {
        }

        public void unregisterAcsCallback(IAcsEventCallback callback) throws RemoteException {
        }

        public AcsConfiguration getAcsConfiguration() throws RemoteException {
            return null;
        }

        public int getAcsConfigInt(String configItem) throws RemoteException {
            return 0;
        }

        public String getAcsConfigString(String configItem) throws RemoteException {
            return null;
        }

        public boolean isAcsConnected() throws RemoteException {
            return false;
        }

        public void triggerAcsRequest(int reason) throws RemoteException {
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

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRcsUaService {
        static final int TRANSACTION_activate = 1;
        static final int TRANSACTION_addCapability = 6;
        static final int TRANSACTION_clearAcsConfiguration = 26;
        static final int TRANSACTION_deactivate = 2;
        static final int TRANSACTION_getAcsConfigInt = 18;
        static final int TRANSACTION_getAcsConfigString = 19;
        static final int TRANSACTION_getAcsConfiguration = 17;
        static final int TRANSACTION_getAcsSwitchState = 24;
        static final int TRANSACTION_getCapabilities = 13;
        static final int TRANSACTION_getOptions = 12;
        static final int TRANSACTION_isAcsConnected = 20;
        static final int TRANSACTION_isActivated = 14;
        static final int TRANSACTION_registerAcsCallback = 15;
        static final int TRANSACTION_registerClient = 9;
        static final int TRANSACTION_removeCapability = 7;
        static final int TRANSACTION_setAcsProvisioningAddress = 25;
        static final int TRANSACTION_setAcsSwitchState = 22;
        static final int TRANSACTION_setAcsonfiguration = 23;
        static final int TRANSACTION_setOptions = 11;
        static final int TRANSACTION_triggerAcsRequest = 21;
        static final int TRANSACTION_triggerForceReregistration = 4;
        static final int TRANSACTION_triggerReregistration = 3;
        static final int TRANSACTION_triggerRestoration = 5;
        static final int TRANSACTION_unregisterAcsCallback = 16;
        static final int TRANSACTION_unregisterClient = 10;
        static final int TRANSACTION_updateCapabilities = 8;

        public Stub() {
            attachInterface(this, IRcsUaService.DESCRIPTOR);
        }

        public static IRcsUaService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IRcsUaService.DESCRIPTOR);
            if (iin == null || !(iin instanceof IRcsUaService)) {
                return new Proxy(obj);
            }
            return (IRcsUaService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Capability _arg0;
            Capability _arg02;
            Capability _arg03;
            Capability _arg04;
            Capability _arg05;
            Bundle _arg06;
            switch (code) {
                case 1598968902:
                    reply.writeString(IRcsUaService.DESCRIPTOR);
                    return true;
                default:
                    boolean _arg07 = false;
                    switch (code) {
                        case 1:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            activate();
                            reply.writeNoException();
                            return true;
                        case 2:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            deactivate();
                            reply.writeNoException();
                            return true;
                        case 3:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg0 = Capability.CREATOR.createFromParcel(data);
                            } else {
                                _arg0 = null;
                            }
                            triggerReregistration(_arg0);
                            reply.writeNoException();
                            return true;
                        case 4:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg02 = Capability.CREATOR.createFromParcel(data);
                            } else {
                                _arg02 = null;
                            }
                            triggerForceReregistration(_arg02);
                            reply.writeNoException();
                            return true;
                        case 5:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            triggerRestoration();
                            reply.writeNoException();
                            return true;
                        case 6:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg03 = Capability.CREATOR.createFromParcel(data);
                            } else {
                                _arg03 = null;
                            }
                            addCapability(_arg03);
                            reply.writeNoException();
                            return true;
                        case 7:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg04 = Capability.CREATOR.createFromParcel(data);
                            } else {
                                _arg04 = null;
                            }
                            removeCapability(_arg04);
                            reply.writeNoException();
                            return true;
                        case 8:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg05 = Capability.CREATOR.createFromParcel(data);
                            } else {
                                _arg05 = null;
                            }
                            updateCapabilities(_arg05);
                            reply.writeNoException();
                            return true;
                        case 9:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            IRcsUaClient _result = registerClient(IImsEventCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                            return true;
                        case 10:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            unregisterClient(IRcsUaClient.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        case 11:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg06 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                            } else {
                                _arg06 = null;
                            }
                            setOptions(_arg06);
                            reply.writeNoException();
                            return true;
                        case 12:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            Bundle _result2 = getOptions();
                            reply.writeNoException();
                            if (_result2 != null) {
                                reply.writeInt(1);
                                _result2.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        case 13:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            Capability _result3 = getCapabilities();
                            reply.writeNoException();
                            if (_result3 != null) {
                                reply.writeInt(1);
                                _result3.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        case 14:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            boolean _result4 = isActivated();
                            reply.writeNoException();
                            reply.writeInt(_result4);
                            return true;
                        case 15:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            registerAcsCallback(IAcsEventCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        case 16:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            unregisterAcsCallback(IAcsEventCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        case 17:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            AcsConfiguration _result5 = getAcsConfiguration();
                            reply.writeNoException();
                            if (_result5 != null) {
                                reply.writeInt(1);
                                _result5.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        case 18:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            int _result6 = getAcsConfigInt(data.readString());
                            reply.writeNoException();
                            reply.writeInt(_result6);
                            return true;
                        case 19:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            String _result7 = getAcsConfigString(data.readString());
                            reply.writeNoException();
                            reply.writeString(_result7);
                            return true;
                        case 20:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            boolean _result8 = isAcsConnected();
                            reply.writeNoException();
                            reply.writeInt(_result8);
                            return true;
                        case 21:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            triggerAcsRequest(data.readInt());
                            reply.writeNoException();
                            return true;
                        case 22:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg07 = true;
                            }
                            boolean _result9 = setAcsSwitchState(_arg07);
                            reply.writeNoException();
                            reply.writeInt(_result9);
                            return true;
                        case 23:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            setAcsonfiguration(data.readString(), data.readString(), data.readString(), data.readString());
                            reply.writeNoException();
                            return true;
                        case 24:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            boolean _result10 = getAcsSwitchState();
                            reply.writeNoException();
                            reply.writeInt(_result10);
                            return true;
                        case 25:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            boolean _result11 = setAcsProvisioningAddress(data.readString());
                            reply.writeNoException();
                            reply.writeInt(_result11);
                            return true;
                        case 26:
                            data.enforceInterface(IRcsUaService.DESCRIPTOR);
                            boolean _result12 = clearAcsConfiguration();
                            reply.writeNoException();
                            reply.writeInt(_result12);
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IRcsUaService {
            public static IRcsUaService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IRcsUaService.DESCRIPTOR;
            }

            public void activate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().activate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deactivate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deactivate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void triggerReregistration(Capability features) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (features != null) {
                        _data.writeInt(1);
                        features.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerReregistration(features);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void triggerForceReregistration(Capability features) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (features != null) {
                        _data.writeInt(1);
                        features.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerForceReregistration(features);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void triggerRestoration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerRestoration();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addCapability(Capability capability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (capability != null) {
                        _data.writeInt(1);
                        capability.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addCapability(capability);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeCapability(Capability capability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (capability != null) {
                        _data.writeInt(1);
                        capability.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeCapability(capability);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateCapabilities(Capability capabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (capabilities != null) {
                        _data.writeInt(1);
                        capabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateCapabilities(capabilities);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IRcsUaClient registerClient(IImsEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerClient(callback);
                    }
                    _reply.readException();
                    IRcsUaClient _result = IRcsUaClient.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterClient(IRcsUaClient client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterClient(client);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOptions(Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOptions(options);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getOptions() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOptions();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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

            public Capability getCapabilities() throws RemoteException {
                Capability _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCapabilities();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Capability.CREATOR.createFromParcel(_reply);
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

            public boolean isActivated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isActivated();
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

            public void registerAcsCallback(IAcsEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            public void unregisterAcsCallback(IAcsEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            public AcsConfiguration getAcsConfiguration() throws RemoteException {
                AcsConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAcsConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AcsConfiguration.CREATOR.createFromParcel(_reply);
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
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeString(configItem);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeString(configItem);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            public boolean isAcsConnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAcsConnected();
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

            public void triggerAcsRequest(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerAcsRequest(reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAcsSwitchState(boolean state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(state ? 1 : 0);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeString(rcsVersion);
                    _data.writeString(rcsProfile);
                    _data.writeString(clientVendor);
                    _data.writeString(clientVersion);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    _data.writeString(address);
                    boolean z = false;
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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
                    _data.writeInterfaceToken(IRcsUaService.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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
        }

        public static boolean setDefaultImpl(IRcsUaService impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IRcsUaService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
