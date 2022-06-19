package androidx.collection;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ArrayMap<K, V> extends SimpleArrayMap<K, V> implements Map<K, V> {
    ArrayMap<K, V>.EntrySet mEntrySet;
    ArrayMap<K, V>.KeySet mKeySet;
    ArrayMap<K, V>.ValueCollection mValues;

    public ArrayMap() {
    }

    public ArrayMap(int capacity) {
        super(capacity);
    }

    public ArrayMap(SimpleArrayMap map) {
        super(map);
    }

    public boolean containsAll(Collection<?> collection) {
        for (Object o : collection) {
            if (!containsKey(o)) {
                return false;
            }
        }
        return true;
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.mSize + map.size());
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public boolean removeAll(Collection<?> collection) {
        int oldSize = this.mSize;
        for (Object o : collection) {
            remove(o);
        }
        return oldSize != this.mSize;
    }

    public boolean retainAll(Collection<?> collection) {
        int oldSize = this.mSize;
        for (int i = this.mSize - 1; i >= 0; i--) {
            if (!collection.contains(keyAt(i))) {
                removeAt(i);
            }
        }
        if (oldSize != this.mSize) {
            return true;
        }
        return false;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entrySet = this.mEntrySet;
        if (entrySet != null) {
            return entrySet;
        }
        ArrayMap<K, V>.EntrySet entrySet2 = new EntrySet();
        this.mEntrySet = entrySet2;
        return entrySet2;
    }

    public Set<K> keySet() {
        Set<K> keySet = this.mKeySet;
        if (keySet != null) {
            return keySet;
        }
        ArrayMap<K, V>.KeySet keySet2 = new KeySet();
        this.mKeySet = keySet2;
        return keySet2;
    }

    public Collection<V> values() {
        Collection<V> values = this.mValues;
        if (values != null) {
            return values;
        }
        ArrayMap<K, V>.ValueCollection values2 = new ValueCollection();
        this.mValues = values2;
        return values2;
    }

    final class EntrySet implements Set<Map.Entry<K, V>> {
        EntrySet() {
        }

        public boolean add(Map.Entry<K, V> entry) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends Map.Entry<K, V>> collection) {
            int oldSize = ArrayMap.this.mSize;
            for (Map.Entry<K, V> entry : collection) {
                ArrayMap.this.put(entry.getKey(), entry.getValue());
            }
            return oldSize != ArrayMap.this.mSize;
        }

        public void clear() {
            ArrayMap.this.clear();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            int index = ArrayMap.this.indexOfKey(e.getKey());
            if (index < 0) {
                return false;
            }
            return ContainerHelpers.equal(ArrayMap.this.valueAt(index), e.getValue());
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object o : collection) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return ArrayMap.this.isEmpty();
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return new MapIterator();
        }

        public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return ArrayMap.this.mSize;
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public <T> T[] toArray(T[] tArr) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object object) {
            return ArrayMap.equalsSetHelper(this, object);
        }

        public int hashCode() {
            int result = 0;
            for (int i = ArrayMap.this.mSize - 1; i >= 0; i--) {
                K key = ArrayMap.this.keyAt(i);
                V value = ArrayMap.this.valueAt(i);
                int i2 = 0;
                int hashCode = key == null ? 0 : key.hashCode();
                if (value != null) {
                    i2 = value.hashCode();
                }
                result += i2 ^ hashCode;
            }
            return result;
        }
    }

    final class KeySet implements Set<K> {
        KeySet() {
        }

        public boolean add(K k) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            ArrayMap.this.clear();
        }

        public boolean contains(Object object) {
            return ArrayMap.this.containsKey(object);
        }

        public boolean containsAll(Collection<?> collection) {
            return ArrayMap.this.containsAll(collection);
        }

        public boolean isEmpty() {
            return ArrayMap.this.isEmpty();
        }

        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public boolean remove(Object object) {
            int index = ArrayMap.this.indexOfKey(object);
            if (index < 0) {
                return false;
            }
            ArrayMap.this.removeAt(index);
            return true;
        }

        public boolean removeAll(Collection<?> collection) {
            return ArrayMap.this.removeAll(collection);
        }

        public boolean retainAll(Collection<?> collection) {
            return ArrayMap.this.retainAll(collection);
        }

        public int size() {
            return ArrayMap.this.mSize;
        }

        public Object[] toArray() {
            int N = ArrayMap.this.mSize;
            Object[] result = new Object[N];
            for (int i = 0; i < N; i++) {
                result[i] = ArrayMap.this.keyAt(i);
            }
            return result;
        }

        public <T> T[] toArray(T[] array) {
            return ArrayMap.this.toArrayHelper(array, 0);
        }

        public boolean equals(Object object) {
            return ArrayMap.equalsSetHelper(this, object);
        }

        public int hashCode() {
            int result = 0;
            for (int i = ArrayMap.this.mSize - 1; i >= 0; i--) {
                K obj = ArrayMap.this.keyAt(i);
                result += obj == null ? 0 : obj.hashCode();
            }
            return result;
        }
    }

    final class ValueCollection implements Collection<V> {
        ValueCollection() {
        }

        public boolean add(V v) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            ArrayMap.this.clear();
        }

        public boolean contains(Object object) {
            return ArrayMap.this.indexOfValue(object) >= 0;
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object o : collection) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return ArrayMap.this.isEmpty();
        }

        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public boolean remove(Object object) {
            int index = ArrayMap.this.indexOfValue(object);
            if (index < 0) {
                return false;
            }
            ArrayMap.this.removeAt(index);
            return true;
        }

        public boolean removeAll(Collection<?> collection) {
            int N = ArrayMap.this.mSize;
            boolean changed = false;
            int i = 0;
            while (i < N) {
                if (collection.contains(ArrayMap.this.valueAt(i))) {
                    ArrayMap.this.removeAt(i);
                    i--;
                    N--;
                    changed = true;
                }
                i++;
            }
            return changed;
        }

        public boolean retainAll(Collection<?> collection) {
            int N = ArrayMap.this.mSize;
            boolean changed = false;
            int i = 0;
            while (i < N) {
                if (!collection.contains(ArrayMap.this.valueAt(i))) {
                    ArrayMap.this.removeAt(i);
                    i--;
                    N--;
                    changed = true;
                }
                i++;
            }
            return changed;
        }

        public int size() {
            return ArrayMap.this.mSize;
        }

        public Object[] toArray() {
            int N = ArrayMap.this.mSize;
            Object[] result = new Object[N];
            for (int i = 0; i < N; i++) {
                result[i] = ArrayMap.this.valueAt(i);
            }
            return result;
        }

        public <T> T[] toArray(T[] array) {
            return ArrayMap.this.toArrayHelper(array, 1);
        }
    }

    final class KeyIterator extends IndexBasedArrayIterator<K> {
        KeyIterator() {
            super(ArrayMap.this.mSize);
        }

        /* access modifiers changed from: protected */
        public K elementAt(int index) {
            return ArrayMap.this.keyAt(index);
        }

        /* access modifiers changed from: protected */
        public void removeAt(int index) {
            ArrayMap.this.removeAt(index);
        }
    }

    final class ValueIterator extends IndexBasedArrayIterator<V> {
        ValueIterator() {
            super(ArrayMap.this.mSize);
        }

        /* access modifiers changed from: protected */
        public V elementAt(int index) {
            return ArrayMap.this.valueAt(index);
        }

        /* access modifiers changed from: protected */
        public void removeAt(int index) {
            ArrayMap.this.removeAt(index);
        }
    }

    final class MapIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V> {
        int mEnd;
        boolean mEntryValid;
        int mIndex = -1;

        MapIterator() {
            this.mEnd = ArrayMap.this.mSize - 1;
        }

        public boolean hasNext() {
            return this.mIndex < this.mEnd;
        }

        public Map.Entry<K, V> next() {
            if (hasNext()) {
                this.mIndex++;
                this.mEntryValid = true;
                return this;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.mEntryValid) {
                ArrayMap.this.removeAt(this.mIndex);
                this.mIndex--;
                this.mEnd--;
                this.mEntryValid = false;
                return;
            }
            throw new IllegalStateException();
        }

        public K getKey() {
            if (this.mEntryValid) {
                return ArrayMap.this.keyAt(this.mIndex);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public V getValue() {
            if (this.mEntryValid) {
                return ArrayMap.this.valueAt(this.mIndex);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public V setValue(V object) {
            if (this.mEntryValid) {
                return ArrayMap.this.setValueAt(this.mIndex, object);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public boolean equals(Object o) {
            if (!this.mEntryValid) {
                throw new IllegalStateException("This container does not support retaining Map.Entry objects");
            } else if (!(o instanceof Map.Entry)) {
                return false;
            } else {
                Map.Entry<?, ?> e = (Map.Entry) o;
                if (!ContainerHelpers.equal(e.getKey(), ArrayMap.this.keyAt(this.mIndex)) || !ContainerHelpers.equal(e.getValue(), ArrayMap.this.valueAt(this.mIndex))) {
                    return false;
                }
                return true;
            }
        }

        public int hashCode() {
            if (this.mEntryValid) {
                K key = ArrayMap.this.keyAt(this.mIndex);
                V value = ArrayMap.this.valueAt(this.mIndex);
                int i = 0;
                int hashCode = key == null ? 0 : key.hashCode();
                if (value != null) {
                    i = value.hashCode();
                }
                return i ^ hashCode;
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    /* access modifiers changed from: package-private */
    public <T> T[] toArrayHelper(T[] array, int offset) {
        int N = this.mSize;
        if (array.length < N) {
            array = (Object[]) Array.newInstance(array.getClass().getComponentType(), N);
        }
        for (int i = 0; i < N; i++) {
            array[i] = this.mArray[(i << 1) + offset];
        }
        if (array.length > N) {
            array[N] = null;
        }
        return array;
    }

    static <T> boolean equalsSetHelper(Set<T> set, Object object) {
        if (set == object) {
            return true;
        }
        if (object instanceof Set) {
            Set<?> s = (Set) object;
            try {
                if (set.size() != s.size() || !set.containsAll(s)) {
                    return false;
                }
                return true;
            } catch (ClassCastException | NullPointerException e) {
            }
        }
        return false;
    }
}
