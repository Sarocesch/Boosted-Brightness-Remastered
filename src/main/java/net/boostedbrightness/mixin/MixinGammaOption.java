package net.boostedbrightness.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.boostedbrightness.BoostedBrightness;

@Mixin(Options.class)
public class MixinGammaOption {

    @Shadow
    private OptionInstance<Double> gamma;

    @Inject(method = "gamma", at = @At("RETURN"), cancellable = true)
    private void onGetGamma(CallbackInfoReturnable<OptionInstance<Double>> cir) {
        OptionInstance<Double> opt = cir.getReturnValue();
        if (opt == null)
            return;

        try {
            double current = opt.get();
            double clamped = Math.min(BoostedBrightness.maxBrightness,
                    Math.max(BoostedBrightness.minBrightness, current));
            if (Double.compare(current, clamped) != 0) {
                opt.set(clamped);
            }
        } catch (Throwable ignored) {
        }
        cir.setReturnValue(opt);
    }
}
