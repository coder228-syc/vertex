package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.MultiSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public final class MultiSettingRenderer implements SettingRenderer<MultiSetting> {

  public static final int COLLAPSED_HEIGHT = 13;

  @Override
  public void render(DrawContext ctx, MultiSetting multi, int x, int y, int w, SettingRenderContext env) {
    FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), multi.getName(), x + 0.5f, y, env.textPrimary());
    drawCountBadge(ctx, multi, x, y, w, env);

    List<String> modes = multi.getAvailableModes();
    List<String> labels = formatLabels(modes);
    float chipsY = y + SettingChips.chipsOffsetY();
    List<SettingChips.ChipBounds> chips = SettingChips.layout(labels, x, chipsY, w);

    for (SettingChips.ChipBounds chip : chips) {
      String mode = modes.get(chip.index());
      SettingChips.drawChip(ctx, env, chip, labels.get(chip.index()), multi.get(mode));
    }
  }

  @Override
  public boolean mouseClicked(MultiSetting multi, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
    if (button != 0) {
      return false;
    }
    List<String> labels = formatLabels(multi.getAvailableModes());
    float chipsY = y + SettingChips.chipsOffsetY();
    List<SettingChips.ChipBounds> chips = SettingChips.layout(labels, x, chipsY, w);
    SettingChips.ChipBounds hit = SettingChips.findAt(chips, mouseX, mouseY);
    if (hit == null) {
      return false;
    }
    multi.toggle(multi.getAvailableModes().get(hit.index()));
    return true;
  }

  @Override
  public int getHeight() {
    return COLLAPSED_HEIGHT;
  }

  public static int getHeight(MultiSetting multi, int width) {
    return SettingChips.totalHeight(formatLabels(multi.getAvailableModes()), width);
  }

  private static void drawCountBadge(DrawContext ctx, MultiSetting multi, int x, int y, int w, SettingRenderContext env) {
    int enabled = multi.getAllSelected();
    int total = multi.getAvailableModes().size();
    String enabledText = String.valueOf(enabled);
    String sep = "/";
    String totalText = String.valueOf(total);

    var font = FontUtils.gilroy[12];
    float enabledW = font.getWidth(enabledText);
    float sepW = font.getWidth(sep);
    float totalW = font.getWidth(totalText);
    float badgeW = enabledW + sepW + totalW;
    float badgeX = x + w - badgeW;
    float badgeY = y;
    int accent = env.ao(env.accentRgb());

    font.drawLeftAligned(ctx.getMatrices(), enabledText, badgeX, badgeY, accent);
    font.drawLeftAligned(ctx.getMatrices(), sep, badgeX + enabledW, badgeY, accent);
    font.drawLeftAligned(ctx.getMatrices(), totalText, badgeX + enabledW + sepW, badgeY, accent);
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
