/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.kapsi.net.daap.tools;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FilenameFilter;

/**
 * This tool generates the de.kapsi.net.daap.chunks.ChunkClasses.java
 * file.
 *
 * @author  Roger Kapsi
 */
public class ChunkClassesGenerator {
    
    /** Creates a new instance of ChunkClassesGenerator */
    public ChunkClassesGenerator() {
    }
    
    // Creates the ChunkClasses.java file...
    public static void main(String[] args) throws IOException {

        File fin = new File("de/kapsi/net/daap/chunks/impl/");

        if (!fin.exists() || fin.isFile()) {
            throw new IOException();
        }

        File fout = new File("de/kapsi/net/daap/chunks/ChunkClasses.java");

        BufferedWriter out = new BufferedWriter(new FileWriter(fout));
        StringBuffer buffer = new StringBuffer();

        buffer.append("// This class is machine-made!").append("\n\n");
        buffer.append("package de.kapsi.net.daap.chunks;\n\n");
        buffer.append("public final class ChunkClasses {\n");
        buffer.append("    public static final String[] names = {\n");

        String[] list = fin.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".java");
            }
        });

        for(int i = 0; i < list.length; i++) {
            buffer.append("        ").append("\"");

            String clazz = list[i];
            int q = clazz.lastIndexOf(".");

            buffer.append("de.kapsi.net.daap.chunks.impl." + clazz.substring(0, q));
            buffer.append("\"");

            if (i < list.length-1) {
                buffer.append(",");
            }

            buffer.append("\n");
        }
        buffer.append("    };\n");
        buffer.append("}\n");

        out.write(buffer.toString());
        out.close();
    }
}
