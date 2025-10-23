package net.boostedbrightness.mixin;

import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.Element;

import java.lang.reflect.Method;

@Mixin(VideoOptionsScreen.class)
public class MixinOptionsScreen {

    @Inject(method = "init", at = @At("RETURN"), require = 0)
    private void addBoostedBrightnessSlider(CallbackInfo ci) {
        SliderWidget customSlider = new SliderWidget(10, 10, 200, 20,
                Text.literal("Boosted Brightness"), 0.5d) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Brightness: " + String.format("%.2f", this.value)));
            }

            @Override
            protected void applyValue() {
                // hier den Wert anwenden
            }
        };

        // Versuche gängige öffentliche Methoden; fallback: Reflection oder children-Feld
        try {
            try {
                Method m = Screen.class.getMethod("addRenderableChild", Element.class);
                m.invoke(this, customSlider);
                return;
            } catch (NoSuchMethodException ignored) {}

            try {
                Method m = Screen.class.getMethod("addDrawableChild", Element.class);
                m.invoke(this, customSlider);
                return;
            } catch (NoSuchMethodException ignored) {}

            try {
                Method m = Screen.class.getMethod("addDrawable", Element.class);
                m.invoke(this, customSlider);
                return;
            } catch (NoSuchMethodException ignored) {}

            Method declared = Screen.class.getDeclaredMethod("addDrawableChild", Element.class);
            declared.setAccessible(true);
            declared.invoke(this, customSlider);
        } catch (Throwable t) {
            try {
                java.lang.reflect.Field childrenField = Screen.class.getDeclaredField("children");
                childrenField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.List<Element> children = (java.util.List<Element>) childrenField.get(this);
                children.add(customSlider);
            } catch (Throwable ignored) {
            }
        }
    }
}
