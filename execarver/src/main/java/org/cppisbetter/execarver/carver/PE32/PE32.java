package org.cppisbetter.execarver.carver.PE32;

import org.cppisbetter.execarver.carver.BaseCarver;
import org.cppisbetter.execarver.struct.AssocMap;
import org.cppisbetter.execarver.struct.Struct;
import org.cppisbetter.execarver.struct.UnpackedValue;


public class PE32 implements BaseCarver {

    private AssocMap m_DOSHeader;
    private AssocMap m_NTHeaders;
    private AssocMap m_DataDirectories;
    private final byte[] m_fileBytes;

    public PE32(byte[] file) {
        m_fileBytes = file;
    }

    public void parse() {
        parseDOSHeader();
        parseNTHeader();
        parseDataDirectories();
    }

    public String getMachineType() {
        if (m_NTHeaders == null)
            return "";

        short id = m_NTHeaders.getUINT16("Machine");

        return switch (id) {
            case 0x014c -> "Intel I386";
            case (short)0x866C -> "x64";
            default -> "Unknown";
        };
    }

    public boolean is32Bit() {
        assert(m_NTHeaders != null);
        return m_NTHeaders.getUINT16("Magic") == 0x20B;
    }

    public AssocMap getDOSHeader() { return m_DOSHeader; }
    public AssocMap getNTHeaders() { return m_NTHeaders; }

    public AssocMap getDataDirectories() { return m_DataDirectories; }

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
                "vMagic/CMajorLinkerVersion/CMinorLinkerVersion/VSizeOfCode/VSizeOfInitializedData/" +
                "VSizeOfUninitializedData/VAddressOfEntryPoint/VBaseOfCode/QImageBase/" +
                "VSectionAlignment/VFileAlignment/vMajorOSVersion/vMinorOSVersion/vMajorImageVersion/" +
                "vMinorImageVersion/vMajorSubSystemVersion/vMinorSubsystemVersion/VWin32VersionValue/" +
                "VSizeOfImage/VSizeOfHeaders/VCheckSum/vSubSystem/vDllCharacteristics/" +
                "VSizeOfStackReserve/VSizeOfStackCommit/VSizeOfHeapReserve/VSizeOfHeapCommit/" +
                "VLoaderFlags/VNumberOfRvaAndSizes";

        m_NTHeaders = Struct.unpack(ntHeaderFormat, m_fileBytes, m_DOSHeader.getInt("e_lfanew"));
    }

    private void parseDataDirectories() {
        UnpackedValue numRvaAndSizes = m_NTHeaders.get("NumberOfRvaAndSizes");

        int offset = numRvaAndSizes.getOffset() + 4;

        int numRva = (int)numRvaAndSizes.getValue();

        String[] members = {
                "Export", "Import", "Resource", "Exception", "Security", "Relocation", "Debug",
                "Architecture",  "Reserved", "TLS", "Configuration", "Bound Import", "Import Address Table",
                "Delay Import", ".NET MetaData"
        };

        assert(numRva == members.length);

        StringBuilder formatString = new StringBuilder();

        for(String member : members) {

            String fmt = "";
            if (member == "Reserved") {
                // hacky
                fmt = "VReserved/VReserved /";
            } else
                fmt = String.format("V%s RVA/V%s Size/", member, member);


            formatString.append(fmt);
        }

        int len = formatString.length() - 1;

        if(formatString.charAt(len) == '/')
            formatString.deleteCharAt(len);


        m_DataDirectories = Struct.unpack(formatString.toString(), m_fileBytes, offset);
    }
}
