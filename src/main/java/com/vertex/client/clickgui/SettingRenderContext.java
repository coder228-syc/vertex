package com.vertex.client.clickgui;

import com.vertex.client.modules.setting.SliderSetting;
import net.minecraft.client.gui.DrawContext;

public interface SettingRenderContext {

    int ao(int color);

    int accentRgb();

    int textPrimary();

    int textSecondary();

    int textMuted();

    int iconMuted();

    int fieldFill();

    int panelBorder();

    int sliderTrack();

    void drawPanel(DrawContext ctx, float x, float y, float w, float h, float rounding, float alpha);

    void drawField(DrawContext ctx, float x, float y, float w, float h, float rounding, float alpha);

    void drawPanelBorder(DrawContext ctx, float x, float y, float w, float h, float rounding, int borderRgb, float alpha);

    void renderToggle(DrawContext ctx, int x, int y, boolean state, Object id);

    default void beginSliderDrag(SliderSetting setting, int x, int w) {
    }
}
