//package net.boostedbrightness.mixin.sodium;
//
//import net.boostedbrightness.compat.sodium.SodiumBrightnessFormatter;
//import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(ControlValueFormatter.class)
//public interface MixinControlValueFormatter {
//
//    @Inject(method = "brightness()Lnet/caffeinemc/mods/sodium/client/gui/options/control/ControlValueFormatter;",
//            at = @At("HEAD"),
//            cancellable = true,
//            remap = false)
//    private static void onBrightnessFormat(CallbackInfoReturnable<ControlValueFormatter> cir) {
//        cir.setReturnValue(SodiumBrightnessFormatter.createBrightnessFormatter());
//    }
//}