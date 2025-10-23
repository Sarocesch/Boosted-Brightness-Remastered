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

    @Shadow private SimpleOption<Double> gamma;

    @Inject(method = "getGamma", at = @At("RETURN"), cancellable = true)
    private void onGetGamma(CallbackInfoReturnable<SimpleOption<Double>> cir) {
        SimpleOption<Double> opt = cir.getReturnValue();
        if (opt == null) return;

        try {
            double current = opt.getValue();
            double clamped = Math.min(BoostedBrightness.maxBrightness, Math.max(BoostedBrightness.minBrightness, current));
            if (Double.compare(current, clamped) != 0) {
                opt.setValue(clamped);
            }
        } catch (Throwable ignored) {
        }
        cir.setReturnValue(opt);
    }
}
