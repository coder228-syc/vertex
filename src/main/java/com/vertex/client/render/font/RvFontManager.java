package com.vertex.client.render.font;

import com.vertex.client.util.IMinecraft;
import net.minecraft.util.Identifier;

import java.io.InputStream;

public class RvFontManager {

    private static RvFont mainFont;
    private static RvFont titleFont;
    private static RvFont subFont;
    private static RvFont iconFont;
    private static RvFont largeIconFont;

    public static void init() {
        if (mainFont == null) {
            try {
                InputStream sfStream = IMinecraft.getMc().getResourceManager()
                        .getResource(Identifier.of("vertexclient", "font/sfprodisplay_medium.ttf"))
                        .orElseThrow().getInputStream();
                mainFont = new RvFont(sfStream, 8.0F);
            } catch (Exception e) {
                try {
                    InputStream sfAltStream = IMinecraft.getMc().getResourceManager()
                            .getResource(Identifier.of("vertexclient", "font/onest_semibold.ttf"))
                            .orElseThrow().getInputStream();
                    mainFont = new RvFont(sfAltStream, 8.0F);
                } catch (Exception ignored) {
                    mainFont = new RvFont(8.0F);
                }
            }

            try {
                InputStream sfSubStream = IMinecraft.getMc().getResourceManager()
                        .getResource(Identifier.of("vertexclient", "font/sfprodisplayregular.ttf"))
                        .orElseThrow().getInputStream();
                subFont = new RvFont(sfSubStream, 7.8F);
            } catch (Exception e) {
                subFont = new RvFont(8.0F);
            }

            try {
                InputStream onestStream = IMinecraft.getMc().getResourceManager()
                        .getResource(Identifier.of("vertexclient", "font/onest_semibold.ttf"))
                        .orElseThrow().getInputStream();
                titleFont = new RvFont(onestStream, 11.5F);
            } catch (Exception e) {
                titleFont = new RvFont(11.5F);
            }

            try {
                InputStream iconStream = IMinecraft.getMc().getResourceManager()
                        .getResource(Identifier.of("vertexclient", "font/really.ttf"))
                        .orElseThrow().getInputStream();
                iconFont = new RvFont(iconStream, 13.0F);
            } catch (Exception e) {
                iconFont = new RvFont(13.0F);
            }

            try {
                InputStream largeIconStream = IMinecraft.getMc().getResourceManager()
                        .getResource(Identifier.of("vertexclient", "font/really.ttf"))
                        .orElseThrow().getInputStream();
                largeIconFont = new RvFont(largeIconStream, 15.0F);
            } catch (Exception e) {
                largeIconFont = new RvFont(15.0F);
            }
        }
    }

    public static RvFont getMainFont() {
        if (mainFont == null) {
            init();
        }
        return mainFont;
    }

    public static RvFont getTitleFont() {
        if (titleFont == null) {
            init();
        }
        return titleFont;
    }

    public static RvFont getSubFont() {
        if (subFont == null) {
            init();
        }
        return subFont;
    }

    public static RvFont getIconFont() {
        if (iconFont == null) {
            init();
        }
        return iconFont;
    }

    @SuppressWarnings("unused")
    public static RvFont getLargeIconFont() {
        if (largeIconFont == null) {
            init();
        }
        return largeIconFont;
    }
}