package com.vertex.client.mixin;

import com.vertex.client.screen.MainMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    private static final Identifier VERTEX$BACKGROUND = Identifier.of("vertexclient", "textures/gui/menushka.png");

    @Inject(method = "init", at = @At("HEAD"), cancellable = true, require = 0)
    private void vertex$redirectToMainMenu(CallbackInfo ci) {
        MinecraftClient.getInstance().setScreen(new MainMenuScreen());
        ci.cancel();
    }

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void vertex$renderPanorama(DrawContext context, float delta, CallbackInfo ci) {
        vertex$drawBackground(context);
        ci.cancel();
    }

    private void vertex$drawBackground(DrawContext context) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.drawTexture(RenderLayer::getGuiTextured, VERTEX$BACKGROUND, 0, 0, 0f, 0f, width, height, width, height);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/LogoDrawer;draw(Lnet/minecraft/client/gui/DrawContext;IF)V"
            ),
            require = 0
    )
    private void vertex$suppressLogo(LogoDrawer instance, DrawContext context, int width, float alpha) {
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/SplashTextRenderer;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/client/font/TextRenderer;I)V"
            ),
            require = 0
    )
    private void vertex$suppressSplash(SplashTextRenderer instance, DrawContext context, int width, TextRenderer textRenderer, int color) {
    }
}