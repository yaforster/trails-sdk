package io.github.yaforster.trails.core.data;

record SignatureFragment(int offset, byte[] bytes) {

    boolean matches(byte[] content) {
        if (content.length < offset + bytes.length) {
            return false;
        }
        for (int index = 0; index < bytes.length; index++) {
            if (content[offset + index] != bytes[index]) {
                return false;
            }
        }
        return true;
    }
}
