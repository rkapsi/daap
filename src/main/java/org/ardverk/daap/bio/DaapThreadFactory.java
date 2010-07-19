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

package org.ardverk.daap.bio;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} for DAAP {@link Thread}s.
 */
class DaapThreadFactory implements ThreadFactory {
    
    private final AtomicInteger count = new AtomicInteger();
    
    private final String name;

    private final boolean daemon;
    
    public DaapThreadFactory(String name) {
        this(name, true);
    }
    
    DaapThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    private String createName() {
        return name + "-" + count.incrementAndGet();
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, createName());
        thread.setDaemon(daemon);
        return thread;
    }
}
