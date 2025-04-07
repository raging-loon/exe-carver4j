package org.cppisbetter.execarver.struct;


import java.util.HashMap;

///
/// PURPOSE
///     Store ANY data type and reference it by string
///
///     This is to be used in conjunction with Struct
///
public class AssocMap {

    private HashMap<String, Object> m_map;

    public AssocMap() {
        m_map = new HashMap<>();
    }

    public void set(String key, Object value) {
        m_map.put(key, value);
    }

    public <T> T get(String key) {
        return (T)m_map.get(key);
    }

    public int getInt(String key) {
        return (int)m_map.get(key);
    }

    public long getUINT32(String key) {
        return (long)m_map.get(key);
    }

    public long getUINT64(String key) {
        return (long)m_map.get(key);
    }

    public short getUINT16(String key) {
        return (short)m_map.get(key);
    }

    public byte getUINT8(String key) {
        return (byte)m_map.get(key);
    }

}
