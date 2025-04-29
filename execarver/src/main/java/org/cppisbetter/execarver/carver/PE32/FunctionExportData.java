package org.cppisbetter.execarver.carver.PE32;

public class FunctionExportData {
    private final int m_nameRva;
    private final int m_functionRva;
    private final int m_ordinal;
    private final short m_nameOrdinal;

    public FunctionExportData(int nameRva, int fnRva, int ord, short nameOrd) {
        m_functionRva = fnRva;
        m_nameOrdinal = nameOrd;
        m_nameRva = nameRva;
        m_ordinal = ord;
    }

    public int getNameRVA() { return m_nameRva; }
    public int getOrdinal() { return m_ordinal; }
    public int getFunctionRVA() { return m_functionRva; }
    public short getNameOrdinal() { return m_nameOrdinal; }

}
