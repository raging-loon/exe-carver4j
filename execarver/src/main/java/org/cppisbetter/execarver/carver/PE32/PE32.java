package org.cppisbetter.execarver.carver.PE32;

import org.cppisbetter.execarver.carver.BaseCarver;
import org.cppisbetter.execarver.struct.AssocMap;
import org.cppisbetter.execarver.struct.Struct;


public class PE32 implements BaseCarver {

    private AssocMap m_DOSHeader;
    private AssocMap m_NTHeaders;

    private final byte[] m_fileBytes;

    public PE32(byte[] file) {
        m_fileBytes = file;
    }

    public void parse() {
        parseDOSHeader();
    }

    public AssocMap getDOSHeader() { return m_DOSHeader; }

    private void parseDOSHeader() {
        String dosFmtString =
                "ve_magic/ve_cblp/ve_cp/ve_crlc/ve_cparhdr/ve_minalloc/" +
                "ve_maxalloc/ve_ss/ve_sp/ve_csum/ve_ip/ve_cs/ve_lfarlc/" +
                "ve_ovno/x8/ve_oemid/ve_oeminfo/x20/Ve_lfanew";

        m_DOSHeader = Struct.unpack(dosFmtString, m_fileBytes);
    }

    private void parseNTHeader() {
        assert(m_DOSHeader != null);

        String ntHeaderFormat =
                "VSignature/vMachine/vNumberOfSections/VTimeStamp/VPointerToSymbolTable/" +
                "VNumberOfSymbols/vSizeOfOptionalHeader/vCharacteristics/" +
                "vMajor/CMajorLinkerVersion/CMinorLinkerVersion/VSizeOfCode/VSizeOfInitializedData/" +
                "VSizeOfUninitializedData/VAddressOfEntryPoint/VBaseOfCode/QImageBase/" +
                "VSectionAlignment/VFileAlignment/vMajorOSVersion/vMinorOSVersion/vMajorImageVersion/" +
                "vMinorImageVersion/vMajorSubSystemVersion/vMinorSubsystemVersion/VWin32VersionValue/" +
                "VSizeOfImage/VSizeOfHeaders/VCheckSum/vSubSystem/vDllCharacteristics/" +
                "QSizeOfStackReserve/QSizeOfStackCommit/QSizeOfHeapReserve/QSizeOfHeapCommit/" +
                "VLoaderFlags/VNumberOfRvaAndSizes";

        m_NTHeaders = Struct.unpack(ntHeaderFormat, m_fileBytes, m_DOSHeader.getInt("e_lfanew"));

    }

}
