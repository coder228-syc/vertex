package com.vertex.client.modules.impl.hud;

import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.render.font.RvFont;
import com.vertex.client.render.font.RvFontManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "Armor HUD", desc = "Отображение прочности брони в процентах у хотбара", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class ArmorHud extends Module {

    public ArmorHud() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        int scaledWidth = mc.getWindow().getScaledWidth();
        int scaledHeight = mc.getWindow().getScaledHeight();
        int hotbarLeft = scaledWidth / 2 - 91;
        int hotbarRight = scaledWidth / 2 + 91;
        int hotbarTop = scaledHeight - 22;

        RvFont font = RvFontManager.getMainFont();

        ItemStack helmet = mc.player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = mc.player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = mc.player.getEquippedStack(EquipmentSlot.FEET);

        if (!helmet.isEmpty()) drawArmorItemLeft(ctx, font, helmet, hotbarLeft - 50, hotbarTop - 14);
        if (!chestplate.isEmpty()) drawArmorItemLeft(ctx, font, chestplate, hotbarLeft - 50, hotbarTop + 4);
        if (!leggings.isEmpty()) drawArmorItemRight(ctx, font, leggings, hotbarRight + 8, hotbarTop - 14);
        if (!boots.isEmpty()) drawArmorItemRight(ctx, font, boots, hotbarRight + 8, hotbarTop + 4);
    }

    private static void drawArmorItemLeft(DrawContext ctx, RvFont font, ItemStack stack, int x, int y) {
        String text = durabilityPercent(stack);
        int textW = font.getStringWidth(text);
        font.drawString(ctx.getMatrices(), text, x + 24 - textW, y + 4, 0xFFFFFFFF);
        ctx.drawItem(stack, x + 28, y);
    }

    private static void drawArmorItemRight(DrawContext ctx, RvFont font, ItemStack stack, int x, int y) {
        String text = durabilityPercent(stack);
        ctx.drawItem(stack, x, y);
        font.drawString(ctx.getMatrices(), text, x + 20, y + 4, 0xFFFFFFFF);
    }

    private static String durabilityPercent(ItemStack stack) {
        int maxDur = stack.getMaxDamage();
        int dur = maxDur - stack.getDamage();
        int pct = maxDur > 0 ? (int) ((float) dur / maxDur * 100.0F) : 100;
        return pct + "%";
    }
}