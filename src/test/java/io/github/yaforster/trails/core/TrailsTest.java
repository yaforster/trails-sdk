package io.github.yaforster.trails.core;

import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Random;
import org.instancio.generator.Generator;
import org.instancio.settings.Keys;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class TrailsTest {

    protected static final int TEST_REPETITIONS = 100;
    protected static final Generator<String> CUSTOM_STRING_GENERATOR = new TestStringGenerator();
    protected static final long TEST_USER_IMAGE_MAX_SIZE_BYTES = 5L * 1024L * 1024L;
    protected static final int TEST_USER_IMAGE_MAX_WIDTH = 1920;
    protected static final int TEST_USER_IMAGE_MAX_HEIGHT = 1920;
    private static final org.instancio.settings.Settings SETTINGS = org.instancio.settings.Settings.create()
                                                                                                   .set(Keys.MAX_DEPTH,
                                                                                                           5)
                                                                                                   .set(Keys.COLLECTION_MAX_SIZE,
                                                                                                           10)
                                                                                                   .set(Keys.FAIL_ON_ERROR,
                                                                                                           true);

    protected <T> InstancioApi<T> getInstancioOf(Class<T> type) {
        return Instancio.of(type)
                        .withSettings(SETTINGS);
    }

    protected TrailsScreenshotFile screenshotFile(String filename, byte[] content) {
        return new TrailsScreenshotFileBuilder(TEST_USER_IMAGE_MAX_SIZE_BYTES, TEST_USER_IMAGE_MAX_WIDTH,
                TEST_USER_IMAGE_MAX_HEIGHT).build(filename, content);
    }

    protected byte[] pngBytes() {
        return imageBytes("png", 1, 1);
    }

    protected byte[] jpegBytes() {
        return imageBytes("jpeg", 1, 1);
    }

    protected byte[] imageBytes(String format, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    protected byte[] pngSignatureBytes() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private static class TestStringGenerator implements Generator<String> {

        private static final int[] CODE_POINTS = ("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789" + "!@#$%^&*()_+-=[]{}|;:'\",.<>?/`~"
                + "\u00E4\u00F6\u00FC\u00DF\u00C4\u00D6\u00DC\u00F8\u00E5\u00E6\u00F1\u00E7"
                + "\u4F60\u597D\u3053\u3093\u306B\u3061\u306F\uC548\uB155\uD558\uC138\uC694"
                + "\uD83D\uDE00\uD83D\uDE02\uD83D\uDD25\uD83D\uDE80\uD83D\uDCA5\uD83D\uDCA9\uD83C\uDF89").codePoints()
                                                                                                         .toArray();

        @Override
        public String generate(Random random) {
            int length = random.intRange(0, 50);

            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int cp = CODE_POINTS[random.intRange(0, CODE_POINTS.length - 1)];
                sb.appendCodePoint(cp);
            }
            return sb.toString();
        }

    }

}
