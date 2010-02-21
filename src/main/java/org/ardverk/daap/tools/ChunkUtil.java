/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
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

package org.ardverk.daap.tools;

import java.io.File;
import java.io.FilenameFilter;

import org.ardverk.daap.chunks.AbstractChunk;
import org.ardverk.daap.chunks.Chunk;

public final class ChunkUtil {

    public static final String CHUNK_PACKAGE = Chunk.class.getPackage()
            .getName();
    public static final String CHUNK_IMPL_PACKAGE = CHUNK_PACKAGE + ".impl";

    public static final String CHUNK_DIR = CHUNK_PACKAGE.replace('.',
            File.separatorChar);
    public static final String CHUNK_IMPL_DIR = CHUNK_IMPL_PACKAGE.replace('.',
            File.separatorChar);

    // private static final Map map = new HashMap();

    static {

    }

    private ChunkUtil() {

    }

    public static Chunk[] getChunks() {
        return getChunks(new File(CHUNK_IMPL_DIR));
    }

    public static Chunk[] getChunks(File dir) {
        String[] files = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".java");
            }
        });

        Chunk[] chunks = new Chunk[files.length];

        try {
            for (int i = 0; i < files.length; i++) {
                String clazzName = CHUNK_IMPL_PACKAGE
                        + "."
                        + files[i].substring(0, files[i].length()
                                - ".java".length());
                Class clazz = Class.forName(clazzName);

                chunks[i] = (AbstractChunk) clazz.newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return chunks;
    }
}