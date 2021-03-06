package com.camnter.patch.ref;

import java.io.IOException;
import java.io.InputStream;

/**
 * https://github.com/dodola/RocooFix/blob/master/buildsrc/src/main/groovy/com/dodola/rocoofix/ref/ClassPathElement.java
 */
interface ClassPathElement {
    char SEPARATOR_CHAR = '/';
    /**
     * Open a "file" from this {@code ClassPathElement}.
     * @param path a '/' separated relative path to the wanted file.
     * @return an {@code InputStream} ready to read the requested file.
     * @throws IOException if the path can not be found or if an error occurred while opening it.
     */
    InputStream open(String path) throws IOException;
    void close() throws IOException;
    Iterable<String> list();
}