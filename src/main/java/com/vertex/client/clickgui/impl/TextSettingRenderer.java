package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.TextSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import net.minecraft.client.gui.DrawContext;

public final class TextSettingRenderer implements SettingRenderer<TextSetting> {

    public static final int HEIGHT = 26;

    @Override
    public void render(DrawContext ctx, TextSetting ts, int x, int y, int w, SettingRenderContext env) {
        FontUtils.gilroy[10].drawLeftAligned(ctx.getMatrices(), ts.getName(), x, y, env.textPrimary());
        int boxY = y + 11;
        env.drawField(ctx, x, boxY, w, 14, 3f, 1f);
        String txt = ts.getValue();
        if (ts.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0) txt += "|";
        FontUtils.gilroy[8].drawLeftAligned(ctx.getMatrices(), txt, x + 5, boxY + 3, env.textMuted());
    }

    @Override
    public boolean mouseClicked(TextSetting ts, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        ts.setFocused(!ts.isFocused());
        return true;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
