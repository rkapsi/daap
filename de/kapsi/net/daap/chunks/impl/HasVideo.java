package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * 
 * @sice iTunes 6.0.2
 */
public class HasVideo extends BooleanChunk {

    public HasVideo() {
        this(false);
    }

    public HasVideo(boolean value) {
        super("aeHV", "com.apple.itunes.has-video", value);
    }
}
