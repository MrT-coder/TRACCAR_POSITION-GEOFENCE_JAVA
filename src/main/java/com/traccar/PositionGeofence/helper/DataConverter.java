package com.traccar.PositionGeofence.helper;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public final class DataConverter {

    private DataConverter() {
    }

    public static byte[] parseHex(String string) {
        try {
            return Hex.decodeHex(string);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String printHex(byte[] data) {
        return Hex.encodeHexString(data);
    }

    public static byte[] parseBase64(String string) {
        return Base64.decodeBase64(string);
    }

    public static String printBase64(byte[] data) {
        return Base64.encodeBase64String(data);
    }

}
