
package de.kapsi.net.daap;

public class DaapException extends RuntimeException {
    
    public DaapException() {
        super();
    }
    
    public DaapException(String msg) {
        super(msg);
    }
    
    public DaapException(Throwable throwable) {
        super(throwable);
    }
}
