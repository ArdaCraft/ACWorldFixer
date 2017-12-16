package me.dags.massblockr.util;

/**
 * @author dags <dags@dags.me>
 */
public interface EntryIterator<K, V> {

    boolean hasNext();

    boolean next();

    boolean remove();

    K key();

    V value();
}
