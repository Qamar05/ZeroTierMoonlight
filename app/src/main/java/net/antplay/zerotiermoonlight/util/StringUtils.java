package net.antplay.zerotiermoonlight.util;

import com.zerotier.sdk.Peer;
import com.zerotier.sdk.Version;

import java.util.Locale;

import lombok.var;

/**
 * string processing tools
 *
 * @author R Q Abbas
 */
public class StringUtils {

    private static final String VERSION_FORMAT = "%d.%d.%d";

    /**
     * Convert the version number to a readable string
     *
     * @param version version number
     * @return readable string
     */
    public static String toString(Version version) {
        return String.format(Locale.ROOT, VERSION_FORMAT,
                version.getMajor(), version.getMinor(), version.getRevision());
    }

    /**
     * Get the human-readable string of the node version
     *
     * @param peer Node
     * @return readable string
     */
    public static String peerVersionString(Peer peer) {
        return String.format(Locale.ROOT, VERSION_FORMAT,
                peer.getVersionMajor(), peer.getVersionMinor(), peer.getVersionRev());
    }

    /**
     * Convert a hexadecimal string to a character array
     *
     * @param hex hexadecimal string
     * @return character array
     */
    public static byte[] hexStringToBytes(String hex) {
        int length = hex.length();
        if (length % 2 != 0) {
            throw new RuntimeException("String length must be even");
        }
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int highDigit = Character.digit(hex.charAt(i), 16);
            int lowDigit = Character.digit(hex.charAt(i + 1), 16);
            result[i / 2] = (byte) ((highDigit << 4) + lowDigit);
        }
        return result;
    }
}
