package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.BooleanSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import net.minecraft.client.gui.DrawContext;

public final class BooleanSettingRenderer implements SettingRenderer<BooleanSetting> {

    public static final int HEIGHT = 12;

    @Override
    public void render(DrawContext ctx, BooleanSetting bs, int x, int y, int w, SettingRenderContext env) {
        FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), bs.getName(), x + 0.5f, y + 1, env.textPrimary());
        env.renderToggle(ctx, x + w - 16, y, bs.get(), bs);
    }

    @Override
    public boolean mouseClicked(BooleanSetting bs, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        bs.set(!bs.get());
        return true;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
