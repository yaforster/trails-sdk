package io.github.yaforster.trails.core.data;

import java.awt.*;
import java.nio.charset.StandardCharsets;

final class WebpImageDimensionReader {

    private WebpImageDimensionReader() {}

    static Dimension read(byte[] content) {
        if (content.length < 30) {
            throw TrailsScreenshotProcessingException.unreadableImage();
        }
        if (matchesAscii(content, "VP8X") && littleEndianInt(content) >= 10) {
            return new Dimension(1 + unsigned24LittleEndian(content, 24), 1 + unsigned24LittleEndian(content, 27));
        }
        if (matchesAscii(content, "VP8 ") && matchesBytes(content, 23, bytes())) {
            return new Dimension(littleEndianUnsignedShort(content, 26) & 0x3FFF,
                    littleEndianUnsignedShort(content, 28) & 0x3FFF);
        }
        if (matchesAscii(content, "VP8L") && content[20] == 0x2F) {
            int bits = (content[21] & 0xFF) | ((content[22] & 0xFF) << 8) | ((content[23] & 0xFF) << 16)
                    | ((content[24] & 0xFF) << 24);
            return new Dimension(1 + (bits & 0x3FFF), 1 + ((bits >> 14) & 0x3FFF));
        }
        throw TrailsScreenshotProcessingException.unreadableImage();
    }

    private static boolean matchesAscii(byte[] content, String value) {
        return matchesBytes(content, 12, value.getBytes(StandardCharsets.US_ASCII));
    }

    private static boolean matchesBytes(byte[] content, int offset, byte[] expected) {
        if (content.length < offset + expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (content[offset + index] != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private static int littleEndianInt(byte[] content) {
        return (content[16] & 0xFF) | ((content[17] & 0xFF) << 8) | ((content[18] & 0xFF) << 16)
                | ((content[19] & 0xFF) << 24);
    }

    private static int unsigned24LittleEndian(byte[] content, int offset) {
        return (content[offset] & 0xFF) | ((content[offset + 1] & 0xFF) << 8) | ((content[offset + 2] & 0xFF) << 16);
    }

    private static int littleEndianUnsignedShort(byte[] content, int offset) {
        return (content[offset] & 0xFF) | ((content[offset + 1] & 0xFF) << 8);
    }

    private static byte[] bytes() {
        byte[] bytes = new byte[new int[]{157, 1, 42}.length];
        for (int index = 0; index < new int[]{157, 1, 42}.length; index++) {
            bytes[index] = (byte) new int[]{157, 1, 42}[index];
        }
        return bytes;
    }

}
