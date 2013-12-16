package es.orm.utils;

import java.io.IOException;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UUID {
    private static class SecureRandomHolder {
        // class loading is atomic - this is a lazy & safe singleton
        private static final SecureRandom INSTANCE = new SecureRandom();
    }

    /**
     * Format the double value with a single decimal points, trimming trailing '.0'.
     */
    public static String format1Decimals(double value, String suffix) {
        String p = String.valueOf(value);
        int ix = p.indexOf('.') + 1;
        int ex = p.indexOf('E');
        char fraction = p.charAt(ix);
        if (fraction == '0') {
            if (ex != -1) {
                return p.substring(0, ix - 1) + p.substring(ex) + suffix;
            } else {
                return p.substring(0, ix - 1) + suffix;
            }
        } else {
            if (ex != -1) {
                return p.substring(0, ix) + fraction + p.substring(ex) + suffix;
            } else {
                return p.substring(0, ix) + fraction + suffix;
            }
        }
    }

    /**
     * Static factory to retrieve a type 3 (name based) <tt>UUID</tt> based on
     * the specified byte array.
     *
     * @param name a byte array to be used to construct a <tt>UUID</tt>.
     * @return a <tt>UUID</tt> generated from the specified array.
     */
    public static String nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("SHA1 not supported");
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6] &= 0x0f;  /* clear version        */
        md5Bytes[6] |= 0x40;  /* set to version 4     */
        md5Bytes[8] &= 0x3f;  /* clear variant        */
        md5Bytes[8] |= 0x80;  /* set to IETF variant  */
        try {
            byte[] encoded = Base64.encodeBytesToBytes(md5Bytes, 0, md5Bytes.length, Base64.URL_SAFE);
            // we know the bytes are 16, and not a multi of 3, so remove the 2 padding chars that are added
            assert encoded[encoded.length - 1] == '=';
            assert encoded[encoded.length - 2] == '=';
            // we always have padding of two at the end, encode it differently
            return new String(encoded, 0, encoded.length - 2, Base64.PREFERRED_ENCODING);
        } catch (IOException e) {
            throw new RuntimeException("should not be thrown");
        }

    }
    /**
     * Static factory to retrieve a type 3 (name based) <tt>UUID</tt> based on
     * the specified string.
     *
     * @param name a string to be used to construct a <tt>UUID</tt>.
     * @return a <tt>UUID</tt> generated from the specified string.
     */
    //PARO
    public static String nameUUIDFromString(String name) {
        return nameUUIDFromBytes(name.getBytes());
    }

    /**
     * Returns a Base64 encoded version of a Version 4.0 compatible UUID
     * as defined here: http://www.ietf.org/rfc/rfc4122.txt
     */
    public static String randomBase64UUID() {
        return randomBase64UUID(SecureRandomHolder.INSTANCE);
    }


    /**
     * Returns a Base64 encoded version of a Version 4.0 compatible UUID
     * randomly initialized by the given {@link java.util.Random} instance
     * as defined here: http://www.ietf.org/rfc/rfc4122.txt
     */
    public static String randomBase64UUID(Random random) {
        final byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);

        /* Set the version to version 4 (see http://www.ietf.org/rfc/rfc4122.txt)
         * The randomly or pseudo-randomly generated version.
         * The version number is in the most significant 4 bits of the time
         * stamp (bits 4 through 7 of the time_hi_and_version field).*/
        randomBytes[6] &= 0x0f;  /* clear the 4 most significant bits for the version  */
        randomBytes[6] |= 0x40;  /* set the version to 0100 / 0x40 */

        /* Set the variant:
         * The high field of th clock sequence multiplexed with the variant.
         * We set only the MSB of the variant*/
        randomBytes[8] &= 0x3f;  /* clear the 2 most significant bits */
        randomBytes[8] |= 0x80;  /* set the variant (MSB is set)*/
        try {
            byte[] encoded = Base64.encodeBytesToBytes(randomBytes, 0, randomBytes.length, Base64.URL_SAFE);
            // we know the bytes are 16, and not a multi of 3, so remove the 2 padding chars that are added
            assert encoded[encoded.length - 1] == '=';
            assert encoded[encoded.length - 2] == '=';
            // we always have padding of two at the end, encode it differently
            return new String(encoded, 0, encoded.length - 2, Base64.PREFERRED_ENCODING);
        } catch (IOException e) {
            throw new RuntimeException("should not be thrown");
        }
    }
}