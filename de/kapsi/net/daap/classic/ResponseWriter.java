

package de.kapsi.net.daap.classic;

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
    
    /**
     *
     * @param outStream
     * @throws UnsupportedEncodingException
     */    
    public ResponseWriter(OutputStream outStream) 
            throws UnsupportedEncodingException {
        this(outStream, CRLF);
    }
    
    /**
     *
     * @param outStream
     * @param lineSeparator
     * @throws UnsupportedEncodingException
     */    
    public ResponseWriter(OutputStream outStream, String lineSeparator)
    throws UnsupportedEncodingException {
        
        this(outStream, lineSeparator, ISO_8859_1);
    }
    
    /**
     *
     * @param outStream
     * @param lineSeparator
     * @param encoding
     * @throws UnsupportedEncodingException
     */    
    public ResponseWriter(OutputStream outStream, String lineSeparator, String encoding)
            throws UnsupportedEncodingException {
        
        super(new BufferedWriter(new OutputStreamWriter(outStream, encoding)));
        this.outStream = outStream;
        this.encoding = encoding;
    }
    
    /**
     *
     * @return
     */    
    public String getEncoding() {
        return encoding;
    }
    
    /**
     *
     * @throws IOException
     */    
    public void close() throws IOException {
        if(out != null) {
            super.close();
            out = null;
        }
    }
    
    /**
     *
     * @throws IOException
     */    
    public void flush() throws IOException {
        if(out != null) {
            super.flush();
            outStream.flush();
        }
    }
    
    /**
     *
     * @param b
     * @throws IOException
     */    
    public void write(byte b) throws IOException {
        super.flush();
        outStream.write((int)b);
    }
    
    /**
     *
     * @param b
     * @throws IOException
     */    
    public void write(byte[] b) throws IOException {
        super.flush();
        outStream.write(b);
    }
    /**
     *
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */    
    public void write(byte[] b, int off, int len) throws IOException {
        super.flush();
        outStream.write(b,off,len);
    }
    
    /**
     *
     * @param s
     * @throws IOException
     */    
    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }
        write(s);
    }
    
    /**
     *
     * @param i
     * @throws IOException
     */    
    public void print(int i) throws IOException {
        write(Integer.toString(i));
    }
    
    /**
     *
     * @param i
     * @throws IOException
     */    
    public void println(int i) throws IOException {
        write(Integer.toString(i));
        write(CRLF);
    }
    
    /**
     *
     * @param s
     * @throws IOException
     */    
    public void println(String s) throws IOException {
        print(s);
        write(CRLF);
    }
    
    /**
     *
     * @throws IOException
     */    
    public void println() throws IOException {
        write(CRLF);
    }
}
