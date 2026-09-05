package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.ModeSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public final class ModeSettingRenderer implements SettingRenderer<ModeSetting> {

  public static final int COLLAPSED_HEIGHT = 13;

  @Override
  public void render(DrawContext ctx, ModeSetting ms, int x, int y, int w, SettingRenderContext env) {
    FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), ms.getName(), x + 0.5f, y, env.textPrimary());

    List<String> modes = ms.getModes();
    List<String> labels = formatLabels(modes);
    float chipsY = y + SettingChips.chipsOffsetY();
    List<SettingChips.ChipBounds> chips = SettingChips.layout(labels, x, chipsY, w);
    String selected = ms.get();

    for (SettingChips.ChipBounds chip : chips) {
      String mode = modes.get(chip.index());
      SettingChips.drawChip(ctx, env, chip, labels.get(chip.index()), mode.equals(selected));
    }
  }

  @Override
  public boolean mouseClicked(ModeSetting ms, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
    if (button != 0) {
      return false;
    }
    List<String> labels = formatLabels(ms.getModes());
    float chipsY = y + SettingChips.chipsOffsetY();
    List<SettingChips.ChipBounds> chips = SettingChips.layout(labels, x, chipsY, w);
    SettingChips.ChipBounds hit = SettingChips.findAt(chips, mouseX, mouseY);
    if (hit == null) {
      return false;
    }
    ms.set(ms.getModes().get(hit.index()));
    return true;
  }

  @Override
  public int getHeight() {
    return COLLAPSED_HEIGHT;
  }

  public static int getHeight(ModeSetting ms, int width) {
    return SettingChips.totalHeight(formatLabels(ms.getModes()), width);
  }

  private static List<String> formatLabels(List<String> modes) {
    List<String> labels = new ArrayList<>(modes.size());
    for (String mode : modes) {
      labels.add(formatLabel(mode));
    }
    return labels;
  }

  private static String formatLabel(String raw) {
    if (raw == null || raw.isEmpty()) {
      return "";
    }
    if (raw.length() == 1) {
      return raw.toUpperCase();
    }
    return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
  }
}
