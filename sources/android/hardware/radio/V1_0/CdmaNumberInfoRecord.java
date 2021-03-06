package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CdmaNumberInfoRecord {
    public String number = new String();
    public byte numberPlan = 0;
    public byte numberType = 0;

    /* renamed from: pi */
    public byte f1pi = 0;

    /* renamed from: si */
    public byte f2si = 0;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CdmaNumberInfoRecord.class) {
            return false;
        }
        CdmaNumberInfoRecord other = (CdmaNumberInfoRecord) otherObject;
        if (HidlSupport.deepEquals(this.number, other.number) && this.numberType == other.numberType && this.numberPlan == other.numberPlan && this.f1pi == other.f1pi && this.f2si == other.f2si) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.numberType))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.numberPlan))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.f1pi))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.f2si)))});
    }

    public final String toString() {
        return "{" + ".number = " + this.number + ", .numberType = " + this.numberType + ", .numberPlan = " + this.numberPlan + ", .pi = " + this.f1pi + ", .si = " + this.f2si + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<CdmaNumberInfoRecord> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CdmaNumberInfoRecord> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaNumberInfoRecord _hidl_vec_element = new CdmaNumberInfoRecord();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        String string = _hidl_blob.getString(_hidl_offset + 0);
        this.number = string;
        parcel.readEmbeddedBuffer((long) (string.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.numberType = _hidl_blob.getInt8(16 + _hidl_offset);
        this.numberPlan = _hidl_blob.getInt8(17 + _hidl_offset);
        this.f1pi = _hidl_blob.getInt8(18 + _hidl_offset);
        this.f2si = _hidl_blob.getInt8(19 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CdmaNumberInfoRecord> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.number);
        _hidl_blob.putInt8(16 + _hidl_offset, this.numberType);
        _hidl_blob.putInt8(17 + _hidl_offset, this.numberPlan);
        _hidl_blob.putInt8(18 + _hidl_offset, this.f1pi);
        _hidl_blob.putInt8(19 + _hidl_offset, this.f2si);
    }
}
