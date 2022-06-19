package com.mediatek.ims.rcsua.service;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.IOException;
import java.util.Arrays;

public class RcsUaException implements Parcelable {
    public static final Parcelable.Creator<RcsUaException> CREATOR = new Parcelable.Creator<RcsUaException>() {
        public RcsUaException createFromParcel(Parcel in) {
            return new RcsUaException(in);
        }

        public RcsUaException[] newArray(int size) {
            return new RcsUaException[size];
        }
    };
    private static final Class[] EXCEPTIONS = {IOException.class, SecurityException.class, IllegalStateException.class, IllegalArgumentException.class, UnsupportedOperationException.class, NullPointerException.class};
    private String clazz;
    private String message;

    public RcsUaException() {
        this.clazz = "";
        this.message = "";
    }

    protected RcsUaException(Parcel in) {
        readFromParcel(in);
    }

    public void set(Exception e) throws IllegalArgumentException {
        if (e != null) {
            Class clazz2 = e.getClass();
            if (Arrays.asList(EXCEPTIONS).contains(clazz2)) {
                this.clazz = clazz2.getCanonicalName();
                this.message = e.getMessage() != null ? e.getMessage() : "";
                return;
            }
            throw new IllegalArgumentException("Unexpected exception class: " + clazz2.getCanonicalName());
        }
        throw new IllegalArgumentException("Cannot set a null exception");
    }

    public boolean isSet() {
        String str = this.clazz;
        return str != null && !str.isEmpty();
    }

    public void throwException() throws IOException, SecurityException, IllegalStateException, IllegalArgumentException, UnsupportedOperationException, NullPointerException {
        if (this.clazz.equals(IOException.class.getCanonicalName())) {
            throw new IOException(this.message);
        } else if (this.clazz.equals(SecurityException.class.getCanonicalName())) {
            throw new SecurityException(this.message);
        } else if (this.clazz.equals(IllegalStateException.class.getCanonicalName())) {
            throw new IllegalStateException(this.message);
        } else if (this.clazz.equals(IllegalArgumentException.class.getCanonicalName())) {
            throw new IllegalArgumentException(this.message);
        } else if (this.clazz.equals(UnsupportedOperationException.class.getCanonicalName())) {
            throw new UnsupportedOperationException(this.message);
        } else if (this.clazz.equals(NullPointerException.class.getCanonicalName())) {
            throw new NullPointerException(this.message);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.clazz);
        out.writeString(this.message);
    }

    public void readFromParcel(Parcel in) {
        this.clazz = in.readString();
        this.message = in.readString();
    }

    public void clear() {
        this.clazz = "";
        this.message = "";
    }

    public String toString() {
        if (!isSet()) {
            return "Exception: none";
        }
        return "Exception: name[" + this.clazz + "], message[" + this.message + "]";
    }
}
