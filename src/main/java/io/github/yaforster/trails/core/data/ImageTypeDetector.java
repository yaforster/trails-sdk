package io.github.yaforster.trails.core.data;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ImageTypeDetector {

    private static final List<ImageSignature> SUPPORTED_IMAGE_SIGNATURES = List.of(
            ImageSignature.fromStart(ImageType.PNG, bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)),
            ImageSignature.fromStart(ImageType.JPEG, bytes(0xFF, 0xD8, 0xFF)),
            ImageSignature.fromStart(ImageType.GIF, ascii("GIF87a")),
            ImageSignature.fromStart(ImageType.GIF, ascii("GIF89a")),
            ImageSignature.fromStart(ImageType.BMP, ascii("BM")), ImageSignature.withFragments(ImageType.WEBP,
                    new SignatureFragment(0, ascii("RIFF")), new SignatureFragment(8, ascii("WEBP"))));

    private ImageTypeDetector() {}

    public static List<ImageType> supportedImageTypes() {
        return SUPPORTED_IMAGE_SIGNATURES.stream()
                                         .map(ImageSignature::imageType)
                                         .distinct()
                                         .toList();
    }

    static ImageType detect(byte[] content) {
        return SUPPORTED_IMAGE_SIGNATURES.stream()
                                         .filter(imageSignature -> imageSignature.matches(content))
                                         .map(ImageSignature::imageType)
                                         .findFirst()
                                         .orElse(ImageType.UNSUPPORTED);
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            bytes[index] = (byte) values[index];
        }
        return bytes;
    }

}
