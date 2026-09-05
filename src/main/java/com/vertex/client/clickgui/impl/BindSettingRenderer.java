package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.BindSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import net.minecraft.client.gui.DrawContext;

public final class BindSettingRenderer implements SettingRenderer<BindSetting> {

    public static final int HEIGHT = 12;

    @Override
    public void render(DrawContext ctx, BindSetting bs, int x, int y, int w, SettingRenderContext env) {
        FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), bs.getName(), x + 0.5f, y + 1, env.textPrimary());
        String keyName = bs.isBinding() ? "..." : BindSetting.formatBindKeyName(bs.getKey());
        FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), keyName, x + w - (int) FontUtils.gilroy[12].getWidth(keyName) - 1, y + 1, env.ao(env.accentRgb()));
    }

    @Override
    public boolean mouseClicked(BindSetting bs, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        bs.setBinding(true);
        return true;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
