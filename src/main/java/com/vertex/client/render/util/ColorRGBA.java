package com.vertex.client.render.util;

/** Простой контейнер цвета, используется декоративными эффектами RenderUtil. */
public record ColorRGBA(float r, float g, float b, float a) {

    public static ColorRGBA of(int argb) {
        float[] c = ColorUtil.rgba(argb);
        return new ColorRGBA(c[0], c[1], c[2], c[3]);
    }

    public int getRed()   { return Math.round(r * 255f); }
    public int getGreen() { return Math.round(g * 255f); }
    public int getBlue()  { return Math.round(b * 255f); }
    public int getAlpha() { return Math.round(a * 255f); }
}
