package com.vertex.client.render.font;

import java.util.concurrent.ConcurrentHashMap;

public final class XroseFonts {

    private static final ConcurrentHashMap<Long, MsdfFontRenderer> RENDERERS = new ConcurrentHashMap<>();

    private XroseFonts() {
    }

    public static MsdfFontRenderer renderer(float size, int weight) {
        long key = ((long) Float.floatToRawIntBits(size) << 32) | (weight & 0xFFFFFFFFL);
        return RENDERERS.computeIfAbsent(key, k -> new MsdfFontRenderer(weight, size));
    }
}