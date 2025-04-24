package org.cppisbetter.execarver.struct;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

///
/// PURPOSE
///     Store ANY data type and reference it by string
///
///     This is to be used in conjunction with Struct
///
public class AssocMap {

    private LinkedHashMap<String, UnpackedValue> m_map;

    public AssocMap() {
        m_map = new LinkedHashMap<>();
    }

    public void set(String key, UnpackedValue value) {
        m_map.put(key, value);
    }

    public <T> T get(String key) {
        return (T)m_map.get(key).getValue();
    }

    public int getInt(String key) {
        return (int)m_map.get(key).getValue();
    }

    public long getUINT32(String key) {
        return (long)m_map.get(key).getValue();
    }

    public long getUINT64(String key) {
        return (long)m_map.get(key).getValue();
    }

    public short getUINT16(String key) {
        return (short)m_map.get(key).getValue();
    }

    public byte getUINT8(String key) {
        return (byte)m_map.get(key).getValue();
    }


    public Set<Map.Entry<String, UnpackedValue>> entrySet() {
        return m_map.entrySet();
    }


}
