package net.boostedbrightness.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class MixinLightmapTextureManager {

    // require = 0 makes this mixin optional - the method signature changed in
    // 1.21.11
    // This allows negative brightness values when the redirect works
    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 2), require = 0)
    private float max(float arg0, float arg1) {
        return arg1;
    }
}