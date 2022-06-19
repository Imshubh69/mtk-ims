package com.mediatek.presence.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMtkCoreServiceWrapper extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.presence.service.IMtkCoreServiceWrapper";

    IBinder getAospOptionsServiceBinder(int i) throws RemoteException;

    IBinder getAospPresenceServiceBinder(int i) throws RemoteException;

    IBinder getCapabilitiesServiceBinder() throws RemoteException;

    IBinder getChatServiceBinder() throws RemoteException;

    IBinder getContactsServiceBinder() throws RemoteException;

    IBinder getFileTransferServiceBinder() throws RemoteException;

    IBinder getGeolocServiceBinder() throws RemoteException;

    IBinder getHistoryServiceBinder() throws RemoteException;

    IBinder getImageSharingServiceBinder() throws RemoteException;

    IBinder getMtkPresenceServiceBinder(int i) throws RemoteException;

    IBinder getNetworkConnectivityApiBinder() throws RemoteException;

    IBinder getVideoSharingServiceBinder() throws RemoteException;

    public static class Default implements IMtkCoreServiceWrapper {
        public IBinder getChatServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getFileTransferServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getCapabilitiesServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getContactsServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getGeolocServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getVideoSharingServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getImageSharingServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getNetworkConnectivityApiBinder() throws RemoteException {
            return null;
        }

        public IBinder getHistoryServiceBinder() throws RemoteException {
            return null;
        }

        public IBinder getAospPresenceServiceBinder(int slotId) throws RemoteException {
            return null;
        }

        public IBinder getMtkPresenceServiceBinder(int slotId) throws RemoteException {
            return null;
        }

        public IBinder getAospOptionsServiceBinder(int slotId) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkCoreServiceWrapper {
        static final int TRANSACTION_getAospOptionsServiceBinder = 12;
        static final int TRANSACTION_getAospPresenceServiceBinder = 10;
        static final int TRANSACTION_getCapabilitiesServiceBinder = 3;
        static final int TRANSACTION_getChatServiceBinder = 1;
        static final int TRANSACTION_getContactsServiceBinder = 4;
        static final int TRANSACTION_getFileTransferServiceBinder = 2;
        static final int TRANSACTION_getGeolocServiceBinder = 5;
        static final int TRANSACTION_getHistoryServiceBinder = 9;
        static final int TRANSACTION_getImageSharingServiceBinder = 7;
        static final int TRANSACTION_getMtkPresenceServiceBinder = 11;
        static final int TRANSACTION_getNetworkConnectivityApiBinder = 8;
        static final int TRANSACTION_getVideoSharingServiceBinder = 6;

        public Stub() {
            attachInterface(this, IMtkCoreServiceWrapper.DESCRIPTOR);
        }

        public static IMtkCoreServiceWrapper asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkCoreServiceWrapper)) {
                return new Proxy(obj);
            }
            return (IMtkCoreServiceWrapper) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(IMtkCoreServiceWrapper.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result = getChatServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result);
                            return true;
                        case 2:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result2 = getFileTransferServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result2);
                            return true;
                        case 3:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result3 = getCapabilitiesServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result3);
                            return true;
                        case 4:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result4 = getContactsServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result4);
                            return true;
                        case 5:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result5 = getGeolocServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result5);
                            return true;
                        case 6:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result6 = getVideoSharingServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result6);
                            return true;
                        case 7:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result7 = getImageSharingServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result7);
                            return true;
                        case 8:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result8 = getNetworkConnectivityApiBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result8);
                            return true;
                        case 9:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result9 = getHistoryServiceBinder();
                            reply.writeNoException();
                            reply.writeStrongBinder(_result9);
                            return true;
                        case 10:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result10 = getAospPresenceServiceBinder(data.readInt());
                            reply.writeNoException();
                            reply.writeStrongBinder(_result10);
                            return true;
                        case 11:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result11 = getMtkPresenceServiceBinder(data.readInt());
                            reply.writeNoException();
                            reply.writeStrongBinder(_result11);
                            return true;
                        case 12:
                            data.enforceInterface(IMtkCoreServiceWrapper.DESCRIPTOR);
                            IBinder _result12 = getAospOptionsServiceBinder(data.readInt());
                            reply.writeNoException();
                            reply.writeStrongBinder(_result12);
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IMtkCoreServiceWrapper {
            public static IMtkCoreServiceWrapper sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IMtkCoreServiceWrapper.DESCRIPTOR;
            }

            public IBinder getChatServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getChatServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getFileTransferServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getCapabilitiesServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCapabilitiesServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getContactsServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getContactsServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getGeolocServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGeolocServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getVideoSharingServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVideoSharingServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getImageSharingServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImageSharingServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getNetworkConnectivityApiBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkConnectivityApiBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getHistoryServiceBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoryServiceBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getAospPresenceServiceBinder(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAospPresenceServiceBinder(slotId);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getMtkPresenceServiceBinder(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMtkPresenceServiceBinder(slotId);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getAospOptionsServiceBinder(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkCoreServiceWrapper.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAospOptionsServiceBinder(slotId);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkCoreServiceWrapper impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IMtkCoreServiceWrapper getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
