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
 * This tool generates the de.kapsi.net.daap.chunks.ChunkImpls.java
 * file.
 *
 * @author  Roger Kapsi
 */
public class ChunkImplsGenerator {
    
    public static final String CLASS_COMMENT 
        = "/**\n"
        + " * This class is machine-made by {@see de.kapsi.net.daap.tools.ChunkImplsGenerator}!\n"
        + " * It is needed because Reflection cannot list the classes of a package so that we\n"
        + " * must pre-create a such list manually. This file must be rebuild whenever a class\n"
        + " * is removed or a class is added to the {@see de.kapsi.net.daap.chunks.impl} package.\n"
        + " */";
    
    public static final String FIELD_COMMENT
        = "/**\n"
        + " * A list of all classes in the {@see de.kapsi.net.daap.chunks.impl} package.\n"
        + " */";
    
    // Creates the ChunkClasses.java file...
    public static void main(String[] args) throws IOException {

        File fin = new File("de/kapsi/net/daap/chunks/impl/");

        if (!fin.exists() || fin.isFile()) {
            throw new IOException();
        }

        File fout = new File("de/kapsi/net/daap/chunks/ChunkImpls.java");

        BufferedWriter out = new BufferedWriter(new FileWriter(fout));
        StringBuffer buffer = new StringBuffer();

        buffer.append("package de.kapsi.net.daap.chunks;\n\n");
        buffer.append(CLASS_COMMENT).append("\n");
        buffer.append("public final class ChunkImpls {\n");
        buffer.append("   ").append(FIELD_COMMENT).append("\n");
        buffer.append("    public static final String[] classes = {\n");

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
