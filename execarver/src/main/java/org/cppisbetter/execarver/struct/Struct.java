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


    public static AssocMap unpack(String formatString, byte[] bytes) {
        AssocMap map = new AssocMap();

        String[] formats = formatString.split("/");
        int counter = 0;
        for( String fmt : formats) {
            counter = getNextValue(fmt, bytes, counter, map);
        }
        return map;
    }

    private static int getNextValue(String format, byte[] bytes, int counter, AssocMap output) {

        String name = Struct.getVariableName(format);
        char fmtChar = format.charAt(0);

        Object value = null;

        switch(fmtChar) {
            case 'V':
                value = Integer.reverseBytes(extractInt(counter, bytes));
                counter += 4;

                break;
            case 'v':
                value = extractShort(counter, bytes);
                counter += 2;
                break;
            case 'Q':
            case 'P':
                value = extractLong(counter, bytes);
                counter += 8;
                break;

            case 'x':
                int skip = getBytesFromVariableFmtString(format);

                counter += skip;
                return counter;
        }

        output.set(name, value);

        return counter;
    }


    private static int getBytesFromVariableFmtString(String fmtString) {
        int number = 0;
        // 1 to skip fmt identifier
        for(int i = 1; i < fmtString.length(); i++) {
            char c = fmtString.charAt(i);
            if(Character.isDigit(c)) {
                number = number * 10 + (c - '0');
            } else {
                break;
            }
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
        return (short)(
                array[counter]
            | ((array[counter + 1]) << 8));
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
