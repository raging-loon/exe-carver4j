package org.cppisbetter.execarver.struct;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

///
/// PURPOSE
///     Re-Implementation of PHP's 'unpack' function
///     Because despite running on 3 billion devices,
///     the knuckleheads at Oracle haven't developed
///     any *modern* method of binary data extraction
///
///     Even literally assembly could handle this better
///
/// DETAILS
///     - FORMAT SPECIFICATION
///         - Formats should be listed like so: "[type][name]/[type][name]..."
///     - FORMAT IDENTIFIERS
///         |  name | size, endianess |
///         --------------------------|
///         |   V   | 32, little      |
///         |   v   | 16, little      |
///         |   Q   | 64, depends     |
///         |   C   | 1, n/a          |
///         |   P   | 64, little      |
///         ---------------------------
///     - SPECIAL FORMAT IDENTIFIERS
///         |  name  |         purpose        |
///         -----------------------------------
///         |  x<N>  | skip N bytes           |
///         |  a<N>  | get string of N length |
///         -----------------------------------
///
public class Struct {

    ///
    /// PURPOSE
    ///     Interface for unpacking
    ///
    /// DETAILS
    ///     Extracts information for each format string
    ///
    /// PARAMS
    ///     [in] formatString - format string as described above
    ///     [in] bytes        - array of bytes to be unpacked
    ///     [in] startIndex   - where to start unpacking
    ///                         defaults to zero if you use the overloaded function
    /// RETURNS
    ///     AssocMap containing variables => unpacked values
    ///
    public static AssocMap unpack(String formatString, byte[] bytes, int startIndex) {
        AssocMap map = new AssocMap();

        String[] formats = formatString.split("/");
        int counter = startIndex;
        for( String fmt : formats) {
            counter = getNextValue(fmt, bytes, counter, map);
        }
        return map;
    }

    public static AssocMap unpack(String formatString, byte[] bytes) {
        return unpack(formatString, bytes, 0);
    }

    ///
    /// PURPOSE
    ///     Unpack a value
    ///
    /// PARAMS
    ///     [in] format - individial format string
    ///     [in] bytes  - bytes where data is
    ///     [in,out] counter - marks current position in bytes
    ///     [in,out] output  - map to add variables to
    ///
    /// RETURNS
    ///     Updated counter i.e. counter += size of unpacked value
    ///
    private static int getNextValue(String format, byte[] bytes, int counter, AssocMap output) {

        String name = Struct.getVariableName(format);
        char fmtChar = format.charAt(0);
        int oldCounter = counter;
        Object value = null;
        switch(fmtChar) {
            case 'V':   // UIN32T,LE
                value = Integer.reverseBytes(extractInt(counter, bytes));
                counter += 4;

                break;
            case 'v':   // UINT16, LE
                value = extractShort(counter, bytes);
                counter += 2;
                break;
            case 'Q':   // UINT64, LE
            case 'P':
                value = extractLong(counter, bytes);
                counter += 8;
                break;

            case 'x':   // byte skip
                int skip = getBytesFromVariableFmtString(format);

                counter += skip;
                return counter;
            case 'C':
                value = bytes[counter];
                counter++;
                break;
            // TODO: Add exception here

        }
        UnpackedValue uv = new UnpackedValue(value, counter - oldCounter, counter);
        output.set(name, uv);

        return counter;
    }


    private static int getBytesFromVariableFmtString(String fmtString) {
        int number = 0;
        // 1 to skip fmt identifier
        for(int i = 1; i < fmtString.length(); i++) {
            char c = fmtString.charAt(i);
            if(Character.isDigit(c))
                number = number * 10 + (c - '0');
            else
                break;
        }

        return number;
    }

    private static String getVariableName(String fmtString) {
        Matcher m = Pattern.compile("\\w\\d*(\\S+)").matcher(fmtString);

        if(m.find())
            return m.group(1);

        throw new InvalidFormatException(fmtString);
    }

    private static int extractInt(int counter, byte[] array) {
        byte b1 = array[counter + 0];
        byte b2 = array[counter + 1];
        byte b3 = array[counter + 2];
        byte b4 = array[counter + 3];

        return      (b4      )
                |   (b3 << 8 )
                |   (b2 << 16)
                |   (b1 << 24) ;
    }

    private static short extractShort(int counter, byte[] array) {
        short part1 = (short)((array[counter] << 8) & 0xff00);
        short part2 = (short)(array[counter + 1] & 0xff);
        return (short)(((part1 >> 8) & 0x000000ff) | ((part2 << 8) & 0x0000ff00));
    }

    private static long extractLong(int counter, byte[] array) {
        long value = 0;
        int index = 0;
        for(int i = counter; i < counter + 8; i++) {
            value |= (array[i] << index);
            index += 8;
        }

        return value;
    }

}
