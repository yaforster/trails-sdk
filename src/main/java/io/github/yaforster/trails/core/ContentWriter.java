package io.github.yaforster.trails.core;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface ContentWriter {

    void writeTo(OutputStream outputStream) throws IOException;

}
