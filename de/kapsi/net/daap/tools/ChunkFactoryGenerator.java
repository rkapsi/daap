package de.kapsi.net.daap.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

import de.kapsi.net.daap.chunks.Chunk;

public class ChunkFactoryGenerator {
    
    public static final String CLASS = "ChunkFactory";
    public static final String FILE = ChunkUtil.CHUNK_DIR + "/" + CLASS + ".java";
    
    public static final String CLASS_COMMENT 
        = "/**\n"
        + " * This class is machine-made by {" + ChunkFactoryGenerator.class.getName() + "}!\n"
        + " * It is needed because Reflection cannot list the classes of a package so that we\n"
        + " * must pre-create a such list manually. This file must be rebuild whenever a class\n"
        + " * is removed or a class is added to the {@see de.kapsi.net.daap.chunks.impl} package.\n"
        + " */";
    
    public static void main(String[] args) throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append(CLASS_COMMENT);
        buffer.append("\n");
        
        buffer.append("package ").append(ChunkUtil.CHUNK_PACKAGE).append(";\n");
        buffer.append("\n");
        
        buffer.append("import java.util.HashMap;\n");
        buffer.append("import de.kapsi.net.daap.DaapUtil;\n");
        buffer.append("\n");
        
        buffer.append("public final class ").append(CLASS).append(" {\n\n");
        buffer.append("    private final HashMap map = new HashMap();\n\n");
        buffer.append("    public ").append(CLASS).append("() {\n");
                
        Chunk[] chunks = ChunkUtil.getChunks();
        
        for (int i = 0; i < chunks.length; i++) {
            Chunk chunk = chunks[i];
            String contentCode = "0x" + Integer.toHexString(chunk.getContentCode()).toUpperCase(Locale.US);
            String contentCodeString = chunk.getContentCodeString();
            //String name = chunk.getName();
            //int type = chunk.getType();

            buffer.append("        ");
            buffer.append("map.put(new Integer(").append(contentCode).append("), ").append(chunk.getClass().getName()).append(".class); //").append(contentCodeString);
            buffer.append("\n");
        }
            
        buffer.append("    }\n\n");
        
        buffer.append("    public Class getChunkClass(Integer contentCode) {\n");
        buffer.append("        return (Class)map.get(contentCode);\n");
        buffer.append("    }\n\n");
        
        buffer.append("    public Chunk newChunk(int contentCode) {\n");
        buffer.append("        Class clazz = getChunkClass(new Integer(contentCode));\n");
        buffer.append("        try {\n");
        buffer.append("            return (Chunk)clazz.newInstance();\n");
        buffer.append("        } catch (Exception err) {\n");
        buffer.append("            throw new RuntimeException(DaapUtil.toContentCodeString(contentCode), err);\n");
        buffer.append("        }\n");
        buffer.append("    }\n");
        
        buffer.append("}\n");
        
        System.out.println(buffer);
        
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(FILE)));
        out.write(buffer.toString());
        out.close();
    }
}
