package com.vertex.client.theme;

import java.awt.Color;

public class GuiThemeColor {
    public int color;
    public final String displayName;
    public final String description;
    public final String key;

    public float hue;
    public float saturation;
    public float brightness;
    public float alpha;

    public boolean pickerOpen;
    public float pickerAnim;
    public boolean sbDragging;
    public boolean hueDragging;
    public boolean alphaDragging;
    public float copyHoverAnim;
    public float pasteHoverAnim;

    public GuiThemeColor(String key, String displayName, String description, int color) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        setColor(color);
        this.pickerOpen = false;
        this.pickerAnim = 0f;
    }

    public void setColor(int rgb) {
        this.color = fixAlpha(rgb);
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = ((color >> 24) & 0xFF) / 255f;
    }

    public void syncColorFromHsb() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        int a = Math.round(alpha * 255f);
        this.color = (a << 24) | rgb;
    }

    private static int fixAlpha(int c) {
        if (c != 0 && ((c >> 24) & 0xFF) == 0) {
            return c | 0xFF000000;
        }
        return c;
    }
}
