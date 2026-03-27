package net.boostedbrightness.mixin;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// In MC 26.1, lightmap brightness is computed in LightmapRenderStateExtractor.extract()
// The Math.max(0.0F, gamma - darknessEffect) clamps negative gamma to 0.
// This redirect bypasses the clamp so negative gamma values (darker than normal) work.
@Mixin(LightmapRenderStateExtractor.class)
public class MixinLightmapTextureManager {

    @Redirect(method = "extract", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0), require = 0)
    private float max(float arg0, float arg1) {
        return arg1;
    }
}
