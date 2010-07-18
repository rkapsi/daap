package org.ardverk.daap.io;

import java.io.Closeable;
import java.io.IOException;

public class IoUtils {

    private IoUtils() {}
    
    public static boolean close(Closeable c) {
        if (c != null) {
            try {
                 c.close();
                 return true;
            } catch (IOException ignore) {
            }
        }
        return false;
    }
}
