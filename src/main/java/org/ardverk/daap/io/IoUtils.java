package org.ardverk.daap.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

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
    
    public static boolean closeAll(Closeable... c) {
        boolean success = true;
        for (Closeable closeable : c) {
            success &= close(closeable);
        }
        return success;
    }
    
    public static boolean close(Socket socket) {
        if (socket != null) {
            try {
                 socket.close();
                 return true;
            } catch (IOException ignore) {
            }
        }
        return false;
    }
}
