package io.github.yaforster.trails.core.data;

public enum ImageType {

    PNG("image/png"),
    JPEG("image/jpeg"),
    GIF("image/gif"),
    BMP("image/bmp"),
    WEBP("image/webp"),
    UNSUPPORTED("application/octet-stream");

    private final String contentType;

    ImageType(String contentType) {
        this.contentType = contentType;
    }

    public String contentType() {
        return contentType;
    }

}
