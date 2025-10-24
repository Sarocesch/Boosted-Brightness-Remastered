package net.boostedbrightness.mixin.sodium;

import net.boostedbrightness.BoostedBrightness;
import net.boostedbrightness.compat.sodium.SodiumBrightnessFormatter;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl$Button")
public class MixinSodiumSliderButton {

    @Shadow(remap = false)
    @Final
    @Mutable
    private int min;

    @Shadow(remap = false)
    @Final
    @Mutable
    private int max;

    @Shadow(remap = false)
    @Final
    @Mutable
    private int range;

    @Shadow(remap = false)
    @Final
    @Mutable
    private net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter formatter;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void onInit(CallbackInfo ci) {
        try {
            java.lang.reflect.Field optionField = this.getClass().getDeclaredField("field_22786"); // option field
            optionField.setAccessible(true);
            Object option = optionField.get(this);

            if (option instanceof net.caffeinemc.mods.sodium.client.gui.options.Option) {
                net.caffeinemc.mods.sodium.client.gui.options.Option<?> sodiumOption =
                        (net.caffeinemc.mods.sodium.client.gui.options.Option<?>) option;

                Text name = sodiumOption.getName();
                if (name.getString().contains("gamma") || name.getString().contains("Brightness")) {
                    this.min = 0;
                    this.max = (int) (BoostedBrightness.maxBrightness * 100);
                    this.range = this.max - this.min;
                    this.formatter = SodiumBrightnessFormatter.createBrightnessFormatter();
                }
            }
        } catch (Exception e) {
            BoostedBrightness.LOGGER.warn("Failed to modify Sodium brightness slider: " + e.getMessage());
        }
    }
}