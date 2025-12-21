package net.boostedbrightness.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class MixinLightmapTextureManager {

    // require=0 makes this optional - if target changed in MC 1.21.10, skip
    // gracefully
    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 2), require = 0)
    private float max(float arg0, float arg1) {
        return arg1;
    }
}