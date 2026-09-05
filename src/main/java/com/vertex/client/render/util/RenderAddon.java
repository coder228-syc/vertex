package com.vertex.client.render.util;

import net.minecraft.client.util.math.MatrixStack;

/**
 * Вспомогательные трансформации матриц для анимаций GUI.
 */
public final class RenderAddon {

    private RenderAddon() {}

    /** Масштабирует всё, что рисуется после вызова, вокруг точки (pivotX, pivotY). */
    public static void sizeAnimation(MatrixStack matrices, double pivotX, double pivotY, double scale) {
        matrices.translate(pivotX, pivotY, 0);
        matrices.scale((float) scale, (float) scale, 1f);
        matrices.translate(-pivotX, -pivotY, 0);
    }
}
