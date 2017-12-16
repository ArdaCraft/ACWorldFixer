package me.dags.massblockr.util;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author dags <dags@dags.me>
 */
public class OrderedMap<K extends Comparable<K>, V> extends HashMap<K, V> {

    private final TreeSet<K> order;

    private boolean dirty = false;
    private int hash = 0;

    public OrderedMap() {
        this(Comparable::compareTo);
    }

    public OrderedMap(Comparator<K> comparator) {
        order = new TreeSet<>(comparator);
    }

    public Set<K> getOrder() {
        return order;
    }

    public EntryIterator<K, V> iterator() {
        return new OrderedEntryIterator();
    }

    public StringBuilder appendTo(StringBuilder sb) {
        sb.ensureCapacity(32);
        sb.append('[');

        Iterator<K> iterator = getOrder().iterator();
        while (iterator.hasNext()) {
            K key = iterator.next();
            V val = get(key);
            sb.append(key).append('=').append(val);
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }

        return sb.append(']');
    }

    @Override
    public V put(K key, V value) {
        getOrder().add(key);
        dirty = true;
        return super.put(key, value);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> consumer) {
        EntryIterator<K, V> iterator = iterator();
        while (iterator.next()) {
            consumer.accept(iterator.key(), iterator.value());
        }
    }

    @Override
    public int hashCode() {
        if (dirty) {
            dirty = false;
            int result = 0;
            for (K key : order) {
                result += key.hashCode() ^ get(key).hashCode();
            }
            hash = result;
        }
        return hash;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder(32)).toString();
    }

    private class OrderedEntryIterator implements EntryIterator<K, V> {

        private final Iterator<K> iterator = getOrder().iterator();
        private K key;
        private V val;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public boolean next() {
            if (hasNext()) {
                key = iterator.next();
                val = get(key);
                return true;
            }
            return false;
        }

        @Override
        public boolean remove() {
            if (key != null) {
                boolean result = OrderedMap.this.remove(key) != null;
                iterator.remove();
                key = null;
                val = null;
                return result;
            }
            return false;
        }

        @Override
        public K key() {
            return key;
        }

        @Override
        public V value() {
            return val;
        }
    }

    public static void main(String[] args) {
        OrderedMap<String, Object> map = new OrderedMap<>();
        map.put("variant", "alpha");
        map.put("facing", "east");
        map.put("half", "bottom");
        map.put("meta", 5);

        System.out.println(map);

        EntryIterator<String, Object> iterator = map.iterator();
        while (iterator.next()) {
            System.out.printf("%s = %s\n", iterator.key(), iterator.value());
            iterator.remove();
        }

        System.out.println(map);
    }
}
