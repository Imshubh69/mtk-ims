package androidx.collection;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;

public final class ArraySet<E> implements Collection<E>, Set<E> {
    private static final int BASE_SIZE = 4;
    private static final int CACHE_SIZE = 10;
    private static final boolean DEBUG = false;
    private static final String TAG = "ArraySet";
    private static Object[] sBaseCache;
    private static final Object sBaseCacheLock = new Object();
    private static int sBaseCacheSize;
    private static Object[] sTwiceBaseCache;
    private static final Object sTwiceBaseCacheLock = new Object();
    private static int sTwiceBaseCacheSize;
    Object[] mArray;
    private int[] mHashes;
    int mSize;

    private int binarySearch(int hash) {
        try {
            return ContainerHelpers.binarySearch(this.mHashes, this.mSize, hash);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
        }
    }

    private int indexOf(Object key, int hash) {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = binarySearch(hash);
        if (index < 0 || key.equals(this.mArray[index])) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == hash) {
            if (key.equals(this.mArray[end])) {
                return end;
            }
            end++;
        }
        int i = index - 1;
        while (i >= 0 && this.mHashes[i] == hash) {
            if (key.equals(this.mArray[i])) {
                return i;
            }
            i--;
        }
        return ~end;
    }

    private int indexOfNull() {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = binarySearch(0);
        if (index < 0 || this.mArray[index] == null) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == 0) {
            if (this.mArray[end] == null) {
                return end;
            }
            end++;
        }
        int i = index - 1;
        while (i >= 0 && this.mHashes[i] == 0) {
            if (this.mArray[i] == null) {
                return i;
            }
            i--;
        }
        return ~end;
    }

    private void allocArrays(int size) {
        if (size == 8) {
            synchronized (sTwiceBaseCacheLock) {
                Object[] array = sTwiceBaseCache;
                if (array != null) {
                    try {
                        this.mArray = array;
                        sTwiceBaseCache = (Object[]) array[0];
                        int[] iArr = (int[]) array[1];
                        this.mHashes = iArr;
                        if (iArr != null) {
                            array[1] = null;
                            array[0] = null;
                            sTwiceBaseCacheSize--;
                            return;
                        }
                        System.out.println("ArraySet Found corrupt ArraySet cache: [0]=" + array[0] + " [1]=" + array[1]);
                        sTwiceBaseCache = null;
                        sTwiceBaseCacheSize = 0;
                    } catch (ClassCastException e) {
                    }
                }
            }
        } else if (size == 4) {
            synchronized (sBaseCacheLock) {
                Object[] array2 = sBaseCache;
                if (array2 != null) {
                    try {
                        this.mArray = array2;
                        sBaseCache = (Object[]) array2[0];
                        int[] iArr2 = (int[]) array2[1];
                        this.mHashes = iArr2;
                        if (iArr2 != null) {
                            array2[1] = null;
                            array2[0] = null;
                            sBaseCacheSize--;
                            return;
                        }
                        System.out.println("ArraySet Found corrupt ArraySet cache: [0]=" + array2[0] + " [1]=" + array2[1]);
                        sBaseCache = null;
                        sBaseCacheSize = 0;
                    } catch (ClassCastException e2) {
                    }
                }
            }
        }
        this.mHashes = new int[size];
        this.mArray = new Object[size];
    }

    private static void freeArrays(int[] hashes, Object[] array, int size) {
        if (hashes.length == 8) {
            synchronized (sTwiceBaseCacheLock) {
                if (sTwiceBaseCacheSize < 10) {
                    array[0] = sTwiceBaseCache;
                    array[1] = hashes;
                    for (int i = size - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    sTwiceBaseCache = array;
                    sTwiceBaseCacheSize++;
                }
            }
        } else if (hashes.length == 4) {
            synchronized (sBaseCacheLock) {
                if (sBaseCacheSize < 10) {
                    array[0] = sBaseCache;
                    array[1] = hashes;
                    for (int i2 = size - 1; i2 >= 2; i2--) {
                        array[i2] = null;
                    }
                    sBaseCache = array;
                    sBaseCacheSize++;
                }
            }
        }
    }

    public ArraySet() {
        this(0);
    }

    public ArraySet(int capacity) {
        if (capacity == 0) {
            this.mHashes = ContainerHelpers.EMPTY_INTS;
            this.mArray = ContainerHelpers.EMPTY_OBJECTS;
        } else {
            allocArrays(capacity);
        }
        this.mSize = 0;
    }

    public ArraySet(ArraySet<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    public ArraySet(Collection<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    public ArraySet(E[] array) {
        this();
        if (array != null) {
            for (E value : array) {
                add(value);
            }
        }
    }

    public void clear() {
        if (this.mSize != 0) {
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            int osize = this.mSize;
            this.mHashes = ContainerHelpers.EMPTY_INTS;
            this.mArray = ContainerHelpers.EMPTY_OBJECTS;
            this.mSize = 0;
            freeArrays(ohashes, oarray, osize);
        }
        if (this.mSize != 0) {
            throw new ConcurrentModificationException();
        }
    }

    public void ensureCapacity(int minimumCapacity) {
        int oSize = this.mSize;
        if (this.mHashes.length < minimumCapacity) {
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(minimumCapacity);
            int i = this.mSize;
            if (i > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, i);
                System.arraycopy(oarray, 0, this.mArray, 0, this.mSize);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
        if (this.mSize != oSize) {
            throw new ConcurrentModificationException();
        }
    }

    public boolean contains(Object key) {
        return indexOf(key) >= 0;
    }

    public int indexOf(Object key) {
        return key == null ? indexOfNull() : indexOf(key, key.hashCode());
    }

    public E valueAt(int index) {
        return this.mArray[index];
    }

    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    public boolean add(E value) {
        int index;
        int hash;
        int oSize = this.mSize;
        if (value == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = value.hashCode();
            index = indexOf(value, hash);
        }
        if (index >= 0) {
            return false;
        }
        int index2 = ~index;
        if (oSize >= this.mHashes.length) {
            int n = 4;
            if (oSize >= 8) {
                n = (oSize >> 1) + oSize;
            } else if (oSize >= 4) {
                n = 8;
            }
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            if (oSize == this.mSize) {
                int[] iArr = this.mHashes;
                if (iArr.length > 0) {
                    System.arraycopy(ohashes, 0, iArr, 0, ohashes.length);
                    System.arraycopy(oarray, 0, this.mArray, 0, oarray.length);
                }
                freeArrays(ohashes, oarray, oSize);
            } else {
                throw new ConcurrentModificationException();
            }
        }
        if (index2 < oSize) {
            int[] iArr2 = this.mHashes;
            System.arraycopy(iArr2, index2, iArr2, index2 + 1, oSize - index2);
            Object[] objArr = this.mArray;
            System.arraycopy(objArr, index2, objArr, index2 + 1, oSize - index2);
        }
        int i = this.mSize;
        if (oSize == i) {
            int[] iArr3 = this.mHashes;
            if (index2 < iArr3.length) {
                iArr3[index2] = hash;
                this.mArray[index2] = value;
                this.mSize = i + 1;
                return true;
            }
        }
        throw new ConcurrentModificationException();
    }

    public void addAll(ArraySet<? extends E> array) {
        int N = array.mSize;
        ensureCapacity(this.mSize + N);
        if (this.mSize != 0) {
            for (int i = 0; i < N; i++) {
                add(array.valueAt(i));
            }
        } else if (N > 0) {
            System.arraycopy(array.mHashes, 0, this.mHashes, 0, N);
            System.arraycopy(array.mArray, 0, this.mArray, 0, N);
            if (this.mSize == 0) {
                this.mSize = N;
                return;
            }
            throw new ConcurrentModificationException();
        }
    }

    public boolean remove(Object object) {
        int index = indexOf(object);
        if (index < 0) {
            return false;
        }
        removeAt(index);
        return true;
    }

    public E removeAt(int index) {
        int i;
        int oSize = this.mSize;
        Object old = this.mArray[index];
        if (oSize <= 1) {
            clear();
        } else {
            int nSize = oSize - 1;
            int[] iArr = this.mHashes;
            int i2 = 8;
            if (iArr.length <= 8 || (i = this.mSize) >= iArr.length / 3) {
                if (index < nSize) {
                    System.arraycopy(iArr, index + 1, iArr, index, nSize - index);
                    Object[] objArr = this.mArray;
                    System.arraycopy(objArr, index + 1, objArr, index, nSize - index);
                }
                this.mArray[nSize] = null;
            } else {
                if (i > 8) {
                    i2 = i + (i >> 1);
                }
                int n = i2;
                int[] ohashes = this.mHashes;
                Object[] oarray = this.mArray;
                allocArrays(n);
                if (index > 0) {
                    System.arraycopy(ohashes, 0, this.mHashes, 0, index);
                    System.arraycopy(oarray, 0, this.mArray, 0, index);
                }
                if (index < nSize) {
                    System.arraycopy(ohashes, index + 1, this.mHashes, index, nSize - index);
                    System.arraycopy(oarray, index + 1, this.mArray, index, nSize - index);
                }
            }
            if (oSize == this.mSize) {
                this.mSize = nSize;
            } else {
                throw new ConcurrentModificationException();
            }
        }
        return old;
    }

    public boolean removeAll(ArraySet<? extends E> array) {
        int N = array.mSize;
        int originalSize = this.mSize;
        for (int i = 0; i < N; i++) {
            remove(array.valueAt(i));
        }
        return originalSize != this.mSize;
    }

    public int size() {
        return this.mSize;
    }

    public Object[] toArray() {
        int i = this.mSize;
        Object[] result = new Object[i];
        System.arraycopy(this.mArray, 0, result, 0, i);
        return result;
    }

    public <T> T[] toArray(T[] array) {
        if (array.length < this.mSize) {
            array = (Object[]) Array.newInstance(array.getClass().getComponentType(), this.mSize);
        }
        System.arraycopy(this.mArray, 0, array, 0, this.mSize);
        int length = array.length;
        int i = this.mSize;
        if (length > i) {
            array[i] = null;
        }
        return array;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Set)) {
            return false;
        }
        Set<?> set = (Set) object;
        if (size() != set.size()) {
            return false;
        }
        int i = 0;
        while (i < this.mSize) {
            try {
                if (!set.contains(valueAt(i))) {
                    return false;
                }
                i++;
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e2) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int[] hashes = this.mHashes;
        int result = 0;
        int s = this.mSize;
        for (int i = 0; i < s; i++) {
            result += hashes[i];
        }
        return result;
    }

    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.mSize * 14);
        buffer.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Set)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    public Iterator<E> iterator() {
        return new ElementIterator();
    }

    private class ElementIterator extends IndexBasedArrayIterator<E> {
        ElementIterator() {
            super(ArraySet.this.mSize);
        }

        /* access modifiers changed from: protected */
        public E elementAt(int index) {
            return ArraySet.this.valueAt(index);
        }

        /* access modifiers changed from: protected */
        public void removeAt(int index) {
            ArraySet.this.removeAt(index);
        }
    }

    public boolean containsAll(Collection<?> collection) {
        for (Object item : collection) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> collection) {
        ensureCapacity(this.mSize + collection.size());
        boolean added = false;
        for (E value : collection) {
            added |= add(value);
        }
        return added;
    }

    public boolean removeAll(Collection<?> collection) {
        boolean removed = false;
        for (Object value : collection) {
            removed |= remove(value);
        }
        return removed;
    }

    public boolean retainAll(Collection<?> collection) {
        boolean removed = false;
        for (int i = this.mSize - 1; i >= 0; i--) {
            if (!collection.contains(this.mArray[i])) {
                removeAt(i);
                removed = true;
            }
        }
        return removed;
    }
}
