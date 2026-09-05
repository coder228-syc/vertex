package com.vertex.client.render.font;

import java.awt.Font;

@SuppressWarnings("All")
public class FontUtils {

    public final String fontsDir = "/assets/vertexclient/font/";

    public static volatile RenderFonts[] inter = new RenderFonts[256];
    public static volatile RenderFonts[] roboto = new RenderFonts[256];
    public static volatile RenderFonts[] durman = new RenderFonts[256];
    public static volatile RenderFonts[] gilroy = new RenderFonts[256];
    public static volatile RenderFonts[] nuclear = new RenderFonts[256];
    public static volatile RenderFonts[] icomoon = new RenderFonts[256];

    private static volatile boolean initialized = false;

    public synchronized void init() {
        if (initialized) return;

        initializationFont(inter, "inter.ttf");
        initializationFont(roboto, "roboto.ttf");
        initializationFont(durman, "durman.ttf");
        initializationFont(gilroy, "gilroy.ttf");
        initializationFont(nuclear, "nuclear.ttf");
        initializationFont(icomoon, "icomoon.ttf");


        initialized = true;
        System.out.println("[VertexClient] Шрифты загружены");
    }

    private void initializationFont(RenderFonts[] fontArray, String fontName) {
        if (fontArray == null) return;
        try {
            var stream = FontUtils.class.getResourceAsStream(fontsDir + fontName);
            if (stream == null) {
                System.out.println("[VertexClient] НЕ НАЙДЕН файл шрифта: " + fontsDir + fontName);
                return;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            for (int i = 1; i < fontArray.length; i++) {
                fontArray[i] = new RenderFonts(font, i);
            }
        } catch (Exception e) {
            System.out.println("[VertexClient] ОШИБКА загрузки шрифта " + fontName + ": " + e);
        }
    }
}
