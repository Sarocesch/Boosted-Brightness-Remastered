package net.boostedbrightness.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @Inject(
            method = "update",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void boostedBrightness(float delta, CallbackInfo ci) {
        try {
            java.lang.reflect.Field field = LightmapTextureManager.class.getDeclaredField("maxBrightness"); // anpassen, falls nötig
            field.setAccessible(true);
            field.setFloat(null, 1.0f);
        } catch (Exception ignored) {
        }
    }
}
