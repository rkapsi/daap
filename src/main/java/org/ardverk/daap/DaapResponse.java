/*
 * Digital Audio Access Protocol (DAAP) Library
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.daap;

import java.io.IOException;

/**
 * An interface for either NIO or BIO based DaapRespones
 * 
 * @author Roger Kapsi
 */
public interface DaapResponse {

    /**
     * Returns <code>true</code> if there are more bytes to write.
     * 
     * @return <code>true</code> if response has remining bytes in the buffer
     */
    public boolean hasRemaining();

    /**
     * Returns <code>true</code> when the write() operation is complete and
     * <code>false</code> when some bytes were left which shall be written at
     * the next iteration (NIO view, classic I/O DaapRespones will always return
     * <code>true</code>).
     * 
     * @throws IOException
     * @return <code>true</code> if write() operation is complete
     */
    public boolean write() throws IOException;
}
