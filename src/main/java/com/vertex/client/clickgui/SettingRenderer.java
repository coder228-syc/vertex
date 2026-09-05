package com.vertex.client.clickgui;

import com.vertex.client.modules.setting.Setting;
import net.minecraft.client.gui.DrawContext;

public interface SettingRenderer<T extends Setting> {

    void render(DrawContext ctx, T setting, int x, int y, int w, SettingRenderContext env);

    default boolean mouseClicked(T setting, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        return false;
    }

    int getHeight();
}
