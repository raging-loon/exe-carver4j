package org.cppisbetter.execarver.carver.PE32;

import org.cppisbetter.execarver.carver.BaseCarver;
import org.cppisbetter.execarver.struct.AssocMap;
import org.cppisbetter.execarver.struct.Struct;
import org.cppisbetter.execarver.struct.UnpackedValue;

import java.util.LinkedHashMap;
import java.util.Random;


public class PE32 implements BaseCarver {

    private AssocMap m_DOSHeader;
    private AssocMap m_NTHeaders;
    private AssocMap m_DataDirectories;
    private AssocMap m_ExportDirectory;


    private LinkedHashMap<String, SectionHeader> m_sectionHeaders;
    private LinkedHashMap<String, FunctionExportData> m_exports;

    private final byte[] m_fileBytes;



    public PE32(byte[] file) {
        m_fileBytes = file;
        m_exports = new LinkedHashMap<>();
    }

    public void parse() {
        parseDOSHeader();
        parseNTHeader();

        if (m_NTHeaders.getUINT16("NumberOfSections") != 0) {
            m_sectionHeaders = new LinkedHashMap<>();
            parseSectionHeaders();
        }

        parseDataDirectories();
        parseExports();
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
    public AssocMap getExportDirectory() { return m_ExportDirectory; }

    public LinkedHashMap<String, FunctionExportData> getExports() {
        return m_exports;
    }

    public LinkedHashMap<String, SectionHeader> getSectionHeaders() {
        return m_sectionHeaders;
    }

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

    private void parseSectionHeaders() {
        int sectHdrOffset = m_NTHeaders.get("Magic").getOffset() + m_NTHeaders.getUINT16("SizeOfOptionalHeader");

        short numSections = m_NTHeaders.getUINT16("NumberOfSections");

        String fmt = "a8Name/VVirtualSize/VVirtualAddress/VSizeOfRawData/"+
                     "VPointerToRawData/VPointerToRelocations/VPointerToLineNumbers/vNumberOfRelocations/" +
                     "vNumberOfLineNumbers/VCharacteristics";
        for(int i = 0; i < numSections; i++) {

            AssocMap header = Struct.unpack(fmt, m_fileBytes, sectHdrOffset);

            m_sectionHeaders.put(header.getString("Name"),
                new SectionHeader(
                        header.getInt("VirtualSize"),
                        header.getInt("VirtualAddress"),
                        header.getInt("SizeOfRawData"),
                        header.getInt("PointerToRawData"),
                        header.getInt("PointerToRelocations"),
                        header.getInt("PointerToLineNumbers"),
                        header.getUINT16("NumberOfRelocations"),
                        header.getUINT16("NumberOfLineNumbers"),
                        header.getInt("Characteristics")
                )
            );
            System.out.println(header.getString("Name"));
            sectHdrOffset += 40;
        }
    }
    private void parseExports() {
        int rva = m_DataDirectories.getInt("Export RVA");
        int offset = RVAToOffset(rva);

        String exportDirFmt = "VCharacteristics/VTimeDateStamp/vMajorVersion/vMinorVersion/" +
                              "VName/VBase/VNumberOfFunctions/VNumberOfNames/VAddressOfFunctions/" +
                              "VAddressOfNames/VAddressOfNameOrdinals";

        m_ExportDirectory = Struct.unpack(exportDirFmt, m_fileBytes, offset);

        int numNames = m_ExportDirectory.getInt("NumberOfNames");

        int nameListOffset     = RVAToOffset(m_ExportDirectory.getInt("AddressOfNames"));
        int functionListOffset = RVAToOffset(m_ExportDirectory.getInt("AddressOfFunctions"));

        for(int i = 0; i < numNames; i++) {
            int nameRva = getIntFromOffset(nameListOffset + (i * 4));

            String name = Struct.extractString(RVAToOffset(nameRva), 128, m_fileBytes);

            int functionRva  = getIntFromOffset(functionListOffset + (i * 4));

            m_exports.put(name, new FunctionExportData(
                    nameRva, functionRva, i + 1, (short)(i)
            ));
        }

    }

    private int RVAToOffset(int rva) {
        assert(m_sectionHeaders != null);

        for(var sectHdr : m_sectionHeaders.entrySet() ) {
            SectionHeader hdr = sectHdr.getValue();
            int bounds = hdr.getVirtualAddress() + hdr.getVirtualSize();

            if(rva <= bounds) {
                return (rva - hdr.getVirtualAddress()) + hdr.getRawAddress();
            }
        }
        return -1;
    }

    private int getIntFromOffset(int offset) {
        assert (offset + 4 <= m_fileBytes.length);

        byte b1 = m_fileBytes[offset + 3];
        byte b2 = m_fileBytes[offset + 2];
        byte b3 = m_fileBytes[offset + 1];
        byte b4 = m_fileBytes[offset + 0];

        return      ((b4 & 0xFF)      )
                |   ((b3 & 0xFF) << 8 )
                |   ((b2 & 0xFF) << 16)
                |   ((b1 & 0xFF) << 24) ;
    }


}
