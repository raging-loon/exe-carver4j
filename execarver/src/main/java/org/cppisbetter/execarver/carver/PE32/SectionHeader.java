package org.cppisbetter.execarver.carver.PE32;

public class SectionHeader {

    private final int m_virtualSize;
    private final int m_virtualAddress;
    private final int m_rawSize;
    private final int m_rawAddress;
    private final int m_relocAddress;
    private final int m_lineNumbers;
    private final short m_relocationsNumber;
    private final short m_lineNumbersNumber;
    private final int m_characteristics;

    public SectionHeader(int virtualSize, int virtualAddress, int rawSize, int rawAddress, int relocAddress,
                         int lineNumbers, short relocationsNumber, short lineNumbersNumber, int characteristics) {
        m_virtualSize = virtualSize;
        m_virtualAddress = virtualAddress;
        m_rawSize = rawSize;
        m_rawAddress = rawAddress;
        m_relocAddress = relocAddress;
        m_lineNumbers = lineNumbers;
        m_relocationsNumber = relocationsNumber;
        m_lineNumbersNumber = lineNumbersNumber;
        m_characteristics = characteristics;
    }


    public int getVirtualSize() {
        return m_virtualSize;
    }

    public int getVirtualAddress() {
        return m_virtualAddress;
    }

    public int getRawSize() {
        return m_rawSize;
    }

    public int getRawAddress() {
        return m_rawAddress;
    }

    public int getRelocAddress() {
        return m_relocAddress;
    }

    public int getLineNumbers() {
        return m_lineNumbers;
    }

    public short getRelocationsNumber() {
        return m_relocationsNumber;
    }

    public short getLineNumbersNumber() {
        return m_lineNumbersNumber;
    }

    public int getCharacteristics() {
        return m_characteristics;
    }
}
