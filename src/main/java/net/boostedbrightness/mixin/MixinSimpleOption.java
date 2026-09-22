package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.boostedbrightness.misc.BoostedSliderCallbacks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.client.OptionInstance;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the gamma OptionInstance to support brightness beyond 0-100%.
 * Uses Unsafe as fallback if AT/reflection fails on final fields in Java 25.
 */
@Mixin(OptionInstance.class)
public class MixinSimpleOption<T> {

    @Shadow @Final
    Component caption;

    private static Object UNSAFE;
    private static Method PUT_OBJECT;
    private static Method OBJECT_FIELD_OFFSET;

    static {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = f.get(null);
            OBJECT_FIELD_OFFSET = unsafeClass.getMethod("objectFieldOffset", Field.class);
            try {
                PUT_OBJECT = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
            } catch (NoSuchMethodException e) {
                PUT_OBJECT = unsafeClass.getMethod("putReference", Object.class, long.class, Object.class);
            }
        } catch (Exception e) {
            System.err.println("[BoostedBrightness] Unsafe not available: " + e);
        }
    }

    /** Sets a field value, trying Field.set first, then Unsafe as fallback. */
    private static void forceSetField(Object target, Field field, Object value) throws Exception {
        field.setAccessible(true);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            // Field.set failed (likely still final) — use Unsafe
            if (UNSAFE != null && PUT_OBJECT != null && OBJECT_FIELD_OFFSET != null) {
                long offset = (long) OBJECT_FIELD_OFFSET.invoke(UNSAFE, field);
                PUT_OBJECT.invoke(UNSAFE, target, offset, value);
            } else {
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At("RETURN"), method = "<init>*")
    private void init(CallbackInfo info) {
        if (this.caption == null) return;

        var content = this.caption.getContents();
        if (!(content instanceof TranslatableContents translatable)) return;
        if (!translatable.getKey().equals("options.gamma")) return;

        System.out.println("[BoostedBrightness] Found gamma OptionInstance, modifying...");

        try {
            Field valuesField = OptionInstance.class.getDeclaredField("values");
            System.out.println("[BoostedBrightness] values field final=" + Modifier.isFinal(valuesField.getModifiers())
                    + " public=" + Modifier.isPublic(valuesField.getModifiers()));

            forceSetField(this, valuesField, BoostedSliderCallbacks.INSTANCE);

            Field codecField = OptionInstance.class.getDeclaredField("codec");
            forceSetField(this, codecField, BoostedSliderCallbacks.INSTANCE.codec());

            Field toStringField = OptionInstance.class.getDeclaredField("toString");
            forceSetField(this, toStringField, (Function<T, Component>) (Function<Double, Component>) v -> textGetter(v));

            Field onValueUpdateField = OptionInstance.class.getDeclaredField("onValueUpdate");
            forceSetField(this, onValueUpdateField, (Consumer<T>) (Consumer<Double>) v -> changeCallback(v));

            System.out.println("[BoostedBrightness] Gamma option modified: "
                    + BoostedBrightness.minBrightness + " to " + BoostedBrightness.maxBrightness);
        } catch (Exception e) {
            System.err.println("[BoostedBrightness] Failed to modify gamma option: " + e);
            e.printStackTrace();
        }
    }

    private Component textGetter(Double gamma) {
        long brightness = Math.round(gamma * 100);
        String display;
        if (brightness < 0) display = brightness + "%";
        else if (brightness == 0) display = "0%";
        else if (brightness == 100) display = "100%";
        else if (brightness > 100) display = "+" + brightness + "%";
        else display = brightness + "%";
        return Component.translatable("options.gamma").append(": ").append(Component.literal(display));
    }

    private void changeCallback(Double gamma) {
        BoostedBrightness.changeBrightness(gamma);
    }
}
