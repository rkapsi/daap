

package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.FilterWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * A custom PrintWriter
 */
public class ResponseWriter extends FilterWriter {
    
    public static final String CRLF = "\r\n";
    public static final String ISO_8859_1 = "ISO-8859-1";
    private OutputStream outStream;
    private String encoding;
    
    public ResponseWriter(OutputStream outStream) 
            throws UnsupportedEncodingException {
        this(outStream, CRLF);
    }
    
    public ResponseWriter(OutputStream outStream, String lineSeparator)
    throws UnsupportedEncodingException {
        
        this(outStream, lineSeparator, ISO_8859_1);
    }
    
    public ResponseWriter(OutputStream outStream, String lineSeparator, String encoding)
            throws UnsupportedEncodingException {
        
        super(new BufferedWriter(new OutputStreamWriter(outStream, encoding)));
        this.outStream = outStream;
        this.encoding = encoding;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void close() throws IOException {
        if(out != null) {
            super.close();
            out = null;
        }
    }
    
    public void flush() throws IOException {
        if(out != null) {
            super.flush();
            outStream.flush();
        }
    }
    
    public void write(byte b) throws IOException {
        super.flush();
        outStream.write((int)b);
    }
    
    public void write(byte[] b) throws IOException {
        super.flush();
        outStream.write(b);
    }
    public void write(byte[] b, int off, int len) throws IOException {
        super.flush();
        outStream.write(b,off,len);
    }
    
    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }
        write(s);
    }
    
    public void print(int i) throws IOException {
        write(Integer.toString(i));
    }
    
    public void println(int i) throws IOException {
        write(Integer.toString(i));
        write(CRLF);
    }
    
    public void println(String s) throws IOException {
        print(s);
        write(CRLF);
    }
    
    public void println() throws IOException {
        write(CRLF);
    }
}
