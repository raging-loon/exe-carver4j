package org.cppisbetter.execarver.controller;

import java.util.HashMap;

///
/// PURPOSE
///     Represent a cached component
///
class CacheItem<T> {

    private final String m_key;
    private T m_value;

    public CacheItem<?> next = null;
    public CacheItem<?> prev = null;

    public CacheItem(String key, T value) {
        m_key = key;
        m_value = value;
    }

    public CacheItem() {
        m_key = null;
        m_value = null;
    }

    public String getKey() { return m_key; }

    public T getValue() { return m_value; }

    public void setValue(T value) { m_value = value; }

}

///
/// PURPOSE
///     LRU cache of Tables/Other Components
///
public class TableCache {
    private final HashMap<String, CacheItem<?>> m_cache;
    private final CacheItem<?> m_cacheHead;
    private final CacheItem<?> m_cacheTail;

    private final int m_capacity;
    private int m_currentSize;

    public TableCache(int capacity) {
        m_capacity = capacity;
        m_currentSize = 0;

        m_cache = new HashMap<>();

        m_cacheHead = new CacheItem<>();
        m_cacheTail = new CacheItem<>();

        m_cacheHead.next = m_cacheTail;
        m_cacheTail.prev = m_cacheHead;
    }

    public <T> T get(String key) {
        if(!m_cache.containsKey(key))
            return null;
        CacheItem<?> item = m_cache.get(key);
        moveToFront(item);
        return (T)item.getValue();
    }

    public <T> void put(String key, T table) {
        if(m_cache.containsKey(key)) {
            CacheItem<T> item = (CacheItem<T>)m_cache.get(key);
            item.setValue(table);
            moveToFront(item);
        } else {
            CacheItem<T> newItem = new CacheItem<>(key, table);
            m_cache.put(key, newItem);
            setRecentlyUsed(newItem);

            m_currentSize++;

            if(m_currentSize > m_capacity) {
                CacheItem<?> tail = removeTail();
                m_cache.remove(tail.getKey());
                m_currentSize--;
            }

        }
    }


    private void moveToFront(CacheItem<?> item) {
        removeItem(item);
        setRecentlyUsed(item);
    }

    private void removeItem(CacheItem<?> item) {
        assert(item.prev != null
            && item.next != null);

        item.prev.next = item.next;
        item.next.prev = item.prev;
    }

    private void setRecentlyUsed(CacheItem<?> item) {
        item.next = m_cacheHead.next;
        item.prev = m_cacheHead;

        m_cacheHead.next.prev = item;
        m_cacheHead.next = item;
    }

    private CacheItem<?> removeTail() {
        CacheItem<?> tail = m_cacheTail.prev;
        removeItem(tail);

        return tail;
    }

    public void printCache() {
        CacheItem<?> head = m_cacheHead.next;

        while(head != m_cacheTail) {

            if(head.getKey() != null)
                System.out.printf("%s", head.getKey());

            head = head.next;
            if(head != m_cacheTail)
                System.out.printf(" <-> ");
        }
        System.out.printf("\n");
    }

}
