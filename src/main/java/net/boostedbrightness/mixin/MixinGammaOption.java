package net.boostedbrightness.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.boostedbrightness.BoostedBrightness;

@Mixin(GameOptions.class)
public class MixinGammaOption {

    @Shadow public SimpleOption<Double> gamma;

    @Inject(method = "getGamma", at = @At("RETURN"), cancellable = true)
    private void onGetGamma(CallbackInfoReturnable<Double> cir) {
        double val = cir.getReturnValue();
        val = Math.min(BoostedBrightness.maxBrightness, Math.max(BoostedBrightness.minBrightness, val));
        cir.setReturnValue(val);
    }
}
