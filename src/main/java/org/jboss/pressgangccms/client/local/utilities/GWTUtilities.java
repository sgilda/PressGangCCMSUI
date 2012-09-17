package org.jboss.pressgangccms.client.local.utilities;

/**
 * GWT has some limitations, like not being able to bind an Editor to an array
 * (http://code.google.com/p/google-web-toolkit/issues/detail?id=6600) and not supporting conversion from a byte array to a
 * String.
 * 
 * This class provides some GWT friendly methods to work around these issues, especially for the RESTLanguageImageV1 class.
 * 
 * @author Matthew Casperson
 * 
 */
final public class GWTUtilities {
    private static final int BITS_PER_BYTE = 8;

    private GWTUtilities() {

    }

    public static byte[] getBytesUTF8(final String string) {
        return getBytes(string, 1);
    }

    public static byte[] getBytes(final String string, final int bytesPerChar) {
        if (string == null) {
            throw new IllegalArgumentException("string cannot be null");
        }
        if (bytesPerChar < 1) {
            throw new IllegalArgumentException("bytesPerChar must be greater than 1");
        }

        char[] chars = string.toCharArray();
        byte[] toReturn = new byte[chars.length * bytesPerChar];
        for (int i = 0; i < chars.length; i++) {
            for (int j = 0; j < bytesPerChar; j++) {
                toReturn[i * bytesPerChar + j] = (byte) (chars[i] >>> (BITS_PER_BYTE * (bytesPerChar - 1 - j)));
            }
        }
        return toReturn;
    }

    public static String getStringUTF8(byte[] bytes) {
        return getString(bytes, 1);
    }

    public static String getString(byte[] bytes, int bytesPerChar) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }
        if (bytesPerChar < 1) {
            throw new IllegalArgumentException("bytesPerChar must be greater than 1");
        }

        final int length = bytes.length / bytesPerChar;
        final StringBuilder retValue = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char thisChar = 0;

            for (int j = 0; j < bytesPerChar; j++) {
                final int shift = (bytesPerChar - 1 - j) * BITS_PER_BYTE;
                thisChar |= (0x000000FF << shift) & (bytes[i * bytesPerChar + j] << shift);
            }

            retValue.append(thisChar);
        }

        return retValue.toString();
    }

    /**
     * Replacement for String.toByteArray().
     * 
     * @param string The string to convert
     * @param bytesPerChar The number of bytes per character
     * @return the same as the standard Java String.toByteArray() method
     */
    public static byte[] getByteArray(final String string, final int bytesPerChar) {
        char[] chars = string.toCharArray();
        byte[] toReturn = new byte[chars.length * bytesPerChar];
        for (int i = 0; i < chars.length; i++) {
            for (int j = 0; j < bytesPerChar; j++) {
                toReturn[i * bytesPerChar + j] = (byte) (chars[i] >>> (BITS_PER_BYTE * (bytesPerChar - 1 - j)));
            }
        }
        return toReturn;
    }
    
    /**
     * Compares two strings
     * @param a The first string
     * @param b The second string
     * @return true if both strings are null, or if both strings are equal
     */
    public static boolean compareStrings(final String a, final String b)
    {
        if (a == null && b == null)
            return true;
        
        if (a != null)
            return a.equals(b);
        
        return b.equals(a);
    }
}
