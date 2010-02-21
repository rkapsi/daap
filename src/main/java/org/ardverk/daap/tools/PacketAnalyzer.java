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

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;

import org.ardverk.daap.DaapInputStream;
import org.ardverk.daap.chunks.AbstractChunk;
import org.ardverk.daap.chunks.BooleanChunk;
import org.ardverk.daap.chunks.Chunk;

public class PacketAnalyzer {

    private static Chunk newChunk(DaapInputStream in) throws Throwable {
        Chunk chunk = in.readChunk();
        if (chunk instanceof BooleanChunk) {
            ((BooleanChunk) chunk).setValue(true);
        }
        return chunk;
    }

    private static String process(File file) throws Throwable {
        StringBuffer buffer = new StringBuffer();
        DaapInputStream in = null;

        try {
            in = new DaapInputStream(new FileInputStream(file));
            while (in.available() > 0) {
                process(buffer, 0, in);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return buffer.toString();
    }

    private static void process(StringBuffer buffer, int indent,
            DaapInputStream in) throws Throwable {
        AbstractChunk chunk = (AbstractChunk) newChunk(in);
        buffer.append(chunk.toString(indent)).append("\n");
    }

    public static void main(String[] args) {
        try {
            File file = null;

            if (args.length == 0) {
                FileDialog dialog = new FileDialog(new Frame(),
                        "Select file...", FileDialog.LOAD);
                dialog.show();

                String d = dialog.getDirectory();
                String f = dialog.getFile();

                if (d != null && f != null) {
                    args = new String[] { d + f };
                } else {
                    System.out.println("No file selected... Bye!");
                    System.exit(0);
                }
            }

            file = new File(args[0]);
            String result = process(file);
            System.out.println(result);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}