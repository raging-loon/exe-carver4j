package org.cppisbetter.execarver.struct;

///
/// PURPOSE
///     Wrapper around common table values
///
public class UnpackedValue {

    private final Object m_value;
    private final int m_size;
    private final int m_offset;

    public UnpackedValue(Object value, int size, int offset) {
        m_value = value;
        m_size = size;
        m_offset = offset;
    }

    public Object getValue() { return m_value; }

    public int getSize() { return m_size; }

    public int getOffset() { return m_offset; }

    public String toString() {
        String formatString = String.format("%%0%dX", m_size * 2);
        return String.format(formatString, m_value);
    }

    public String getSizeType() {
        return switch (m_size) {
            case 1 -> "BYTE";
            case 2 -> "WORD";
            case 4 -> "DWORD";
            case 8 -> "QWORD";
            default -> "UNKNOWN";
        };
    }

}


