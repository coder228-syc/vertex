package com.vertex.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.modules.setting.BooleanSetting;
import com.vertex.client.render.util.RenderUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "NameTags", desc = "Показывает предметы и броню над игроками", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class NameTags extends Module {

    private final BooleanSetting background = new BooleanSetting("Фон", true);
    private final BooleanSetting slotBackground = new BooleanSetting("Подложка слотов", true);

    public NameTags() {
        addSettings(background, slotBackground);
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        int slotSize = 16;
        int gap = 2;
        int padding = 3;
        int totalSlots = 6;

        int gridW = totalSlots * (slotSize + gap) - gap;
        int cardW = gridW + padding * 2;
        int cardH = slotSize + padding * 2;

        float partialTicks = mc.getRenderTickCounter().getTickDelta(false);

        Matrix4f proj = RenderSystem.getProjectionMatrix();
        Matrix4f model = RenderSystem.getModelViewMatrix();
        Matrix4f mvp = new Matrix4f(proj).mul(model);

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isInvisible() && !mc.player.canSee(player)) continue;

            Vec3d pos = player.getLerpedPos(partialTicks).add(0, player.getHeight() + 0.5, 0);

            Vector4f clip = project(mvp, pos);
            if (clip == null) continue;

            float ndcX = clip.x / clip.w;
            float ndcY = clip.y / clip.w;

            float screenX = (ndcX + 1.0f) * 0.5f * mc.getWindow().getScaledWidth();
            float screenY = (1.0f - ndcY) * 0.5f * mc.getWindow().getScaledHeight();

            ctx.getMatrices().push();
            ctx.getMatrices().translate(screenX, screenY, 0);
            ctx.getMatrices().translate(-cardW / 2f, -cardH - 4, 0);

            int x = 0;
            int y = 0;

            if (background.get()) {
                RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, cardW, cardH, 6f, 0xB2000000);
                RenderUtil.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, cardW - 2, cardH - 2, 5.5f, 0xF1121212);
            }

            for (int i = 0; i < totalSlots; i++) {
                ItemStack stack = getItem(player, i);

                int slotX = x + padding + i * (slotSize + gap);
                int slotY = y + padding;

                if (slotBackground.get()) {
                    RenderUtil.drawRoundedRect(ctx.getMatrices(), slotX, slotY, slotSize, slotSize, 3f, 0xF01E1E1E);
                }

                if (!stack.isEmpty()) {
                    ctx.drawItem(stack, slotX, slotY);
                    ctx.drawStackOverlay(mc.textRenderer, stack, slotX, slotY);
                }
            }

            ctx.getMatrices().pop();
        }
    }

    private ItemStack getItem(AbstractClientPlayerEntity player, int index) {
        return switch (index) {
            case 0 -> player.getMainHandStack();
            case 1 -> player.getEquippedStack(EquipmentSlot.HEAD);
            case 2 -> player.getEquippedStack(EquipmentSlot.CHEST);
            case 3 -> player.getEquippedStack(EquipmentSlot.LEGS);
            case 4 -> player.getEquippedStack(EquipmentSlot.FEET);
            case 5 -> player.getOffHandStack();
            default -> ItemStack.EMPTY;
        };
    }

    private Vector4f project(Matrix4f mvp, Vec3d worldPos) {
        float x = (float) worldPos.x;
        float y = (float) worldPos.y;
        float z = (float) worldPos.z;

        Vector4f clip = new Vector4f(x, y, z, 1.0f);
        mvp.transform(clip);

        if (clip.w <= 0) return null;

        return clip;
    }
}
