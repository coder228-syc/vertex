package com.vertex.client.render.font;

public final class RankTextGradient {
    private RankTextGradient() {}

    public interface GlyphConsumer {
        void accept(char ch, float r, float g, float b);
    }

    public static String upperCase(String text) {
        return text == null ? "" : text.toUpperCase();
    }

    public static int tryProcess(String text, String upperText, int len, int index, GlyphConsumer consumer) {
        return -1;
    }
}
