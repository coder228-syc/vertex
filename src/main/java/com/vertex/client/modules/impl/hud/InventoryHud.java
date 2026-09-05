package com.vertex.client.modules.impl.hud;

import com.vertex.client.hud.HUDManager;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.modules.setting.BooleanSetting;
import com.vertex.client.render.util.RenderUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "Inventory HUD", desc = "Показывает содержимое инвентаря поверх игры", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class InventoryHud extends Module {

    private final BooleanSetting background = new BooleanSetting("Фон", true);
    private final BooleanSetting slotBackground = new BooleanSetting("Подложка слотов", true);

    public InventoryHud() {
        addSettings(background, slotBackground);
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        int slotSize = 18;
        int gap = 2;
        int cols = 9;
        int rows = 3;
        int padding = 4;

        int gridW = cols * (slotSize + gap) - gap;
        int gridH = rows * (slotSize + gap) - gap;
        int cardW = gridW + padding * 2;
        int cardH = gridH + padding * 2;

        HUDManager.Slot slot = HUDManager.inventoryHud;
        if (!slot.enabled) return;
        slot.resize(cardW, cardH);
        slot.beginScale(ctx);

        int x = Math.round(slot.x);
        int y = Math.round(slot.y);

        if (background.get()) {
            RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, cardW, cardH, 8f, 0xEE111115);
            RenderUtil.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, cardW - 2, cardH - 2, 7.5f, 0xFF141419);
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int invSlot = 9 + row * cols + col;
                int slotX = x + padding + col * (slotSize + gap);
                int slotY = y + padding + row * (slotSize + gap);

                if (slotBackground.get()) {
                    RenderUtil.drawRoundedRect(ctx.getMatrices(), slotX, slotY, slotSize, slotSize, 4f, 0xFF18181D);
                }

                ItemStack stack = mc.player.getInventory().getStack(invSlot);
                if (!stack.isEmpty()) {
                    ctx.drawItem(stack, slotX + 1, slotY + 1);
                    ctx.drawStackOverlay(mc.textRenderer, stack, slotX + 1, slotY + 1);
                }
            }
        }

        slot.endScale(ctx);
    }
}