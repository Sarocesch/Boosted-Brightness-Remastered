package net.boostedbrightness;

import com.mojang.blaze3d.platform.InputConstants;
import com.google.gson.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class BoostedBrightness implements ClientModInitializer {
    public static final String MODID = "boostedbrightness";
    public static final int MAX_BRIGHTNESSES = 5;
    private static final Gson GSON = new Gson();

    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
            Identifier.tryParse(MODID + ":keys"));

    public static double minBrightness = -1.0;
    public static double maxBrightness = 12.0;
    public static double brightnessSliderInterval = 0.05;
    private static double step = 0.1;

    public static ArrayList<Double> brightnesses;
    private static int brightnessIndex = 0;
    private static int lastBrightnessIndex = 0;

    private static KeyMapping NEXT_BIND;
    private static KeyMapping RAISE_BIND;
    private static KeyMapping LOWER_BIND;
    private static final KeyMapping[] SELECT_BINDS = new KeyMapping[MAX_BRIGHTNESSES];

    public static Minecraft client;

    @Override
    public void onInitializeClient() {
        loadConfig();
        client = Minecraft.getInstance();

        NEXT_BIND = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.boosted-brightness.next",
                InputConstants.KEY_B,
                KEY_CATEGORY));

        RAISE_BIND = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.boosted-brightness.raise",
                InputConstants.KEY_RBRACKET,
                KEY_CATEGORY));

        LOWER_BIND = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.boosted-brightness.lower",
                InputConstants.KEY_LBRACKET,
                KEY_CATEGORY));

        for (int i = 0; i < MAX_BRIGHTNESSES; i++) {
            SELECT_BINDS[i] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    "key.boosted-brightness.select" + (i + 1),
                    InputConstants.UNKNOWN.getValue(),
                    KEY_CATEGORY));
        }

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (brightnesses == null || brightnesses.isEmpty()) return;

            while (NEXT_BIND.consumeClick()) {
                lastBrightnessIndex = getBrightnessIndex();
                setBrightnessIndex((lastBrightnessIndex + 1) % numBrightnesses());
                showOverlay(mc);
            }

            for (int i = 0; i < numBrightnesses() && i < SELECT_BINDS.length; i++) {
                while (SELECT_BINDS[i].consumeClick()) {
                    int nextIdx = (i != brightnessIndex) ? i : lastBrightnessIndex;
                    lastBrightnessIndex = getBrightnessIndex();
                    setBrightnessIndex(nextIdx);
                    showOverlay(mc);
                }
            }

            double offset = 0;
            while (RAISE_BIND.consumeClick()) offset += step;
            while (LOWER_BIND.consumeClick()) offset -= step;

            if (offset != 0) {
                double newBrightness = Math.max(minBrightness, Math.min(maxBrightness, getBrightness() + offset));
                changeBrightness(newBrightness);
                showOverlay(mc);
            }
        });
    }

    public static int numBrightnesses() {
        return brightnesses != null ? brightnesses.size() : 0;
    }

    public static int getBrightnessIndex() {
        return brightnessIndex;
    }

    public static void setBrightnessIndex(int index) {
        brightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, index));
        setGammaValue(getBrightness());
    }

    public static double getBrightness() {
        if (brightnesses == null || brightnesses.isEmpty()) return 1.0;
        return brightnesses.get(brightnessIndex);
    }

    public static double getBrightness(int index) {
        if (brightnesses == null || index < 0 || index >= brightnesses.size()) return 1.0;
        return brightnesses.get(index);
    }

    public static void changeBrightness(double brightness) {
        if (brightnesses == null || brightnesses.isEmpty()) return;
        brightnesses.set(getBrightnessIndex(), brightness);
        setGammaValue(getBrightness());
    }

    public static void setGammaValue(double value) {
        if (client == null || client.options == null) return;
        try {
            OptionInstance<Double> gamma = client.options.gamma();
            java.lang.reflect.Field valueField = OptionInstance.class.getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.set(gamma, value);
        } catch (Exception e) {
            try {
                client.options.gamma().set(Math.max(0.0, Math.min(1.0, value)));
            } catch (Exception ignored) {}
        }
    }

    public static void changeBrightness(int index, double brightness) {
        if (brightnesses == null || index < 0 || index >= brightnesses.size()) return;
        if (index == brightnessIndex) changeBrightness(brightness);
        else brightnesses.set(index, brightness);
    }

    public static void saveConfig() {
        try {
            JsonObject config = new JsonObject();
            config.addProperty("min", minBrightness);
            config.addProperty("max", maxBrightness);
            config.addProperty("step", step);
            config.addProperty("selected", brightnessIndex + 1);
            config.addProperty("last", lastBrightnessIndex + 1);
            if (brightnesses != null) {
                for (int i = 0; i < brightnesses.size(); i++)
                    config.addProperty(String.valueOf(i + 1), brightnesses.get(i));
            }
            Files.write(getConfigPath(), GSON.toJson(config).getBytes());
        } catch (IOException ex) {
            logException(ex, "Failed to save config");
        }
    }

    private static void loadConfig() {
        try {
            Path path = getConfigPath();
            if (!Files.exists(path)) {
                brightnesses = new ArrayList<>();
                brightnesses.add(1.0);
                brightnesses.add(maxBrightness);
                return;
            }
            JsonObject config = GSON.fromJson(new String(Files.readAllBytes(path)), JsonObject.class);
            asDouble(config.get("min"), v -> minBrightness = v);
            asDouble(config.get("max"), v -> maxBrightness = v);
            asDouble(config.get("step"), v -> BoostedBrightness.step = v);

            brightnesses = new ArrayList<>();
            for (int i = 1; i <= MAX_BRIGHTNESSES && config.has(String.valueOf(i)); i++) {
                asDouble(config.get(String.valueOf(i)), v -> brightnesses.add(v));
            }
            asInt(config.get("selected"), v -> brightnessIndex = v - 1);
            brightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, brightnessIndex));
            if (config.has("last")) {
                asInt(config.get("last"), v -> lastBrightnessIndex = v - 1);
                lastBrightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, lastBrightnessIndex));
            }
        } catch (IOException | JsonSyntaxException ex) {
            logException(ex, "Failed to load config");
        }
        if (brightnesses == null || brightnesses.size() < 2) {
            brightnesses = new ArrayList<>();
            brightnesses.add(1.0);
            brightnesses.add(maxBrightness);
            brightnessIndex = 0;
            lastBrightnessIndex = 0;
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("boostedbrightness.json");
    }

    private static void showOverlay(Minecraft mc) {
        if (mc.gui == null) return;
        mc.gui.hud.setOverlayMessage(
                Component.translatable("overlay.boosted-brightness.change",
                        getBrightnessIndex() + 1,
                        Math.round(getBrightness() * 100))
                        .withStyle(Style.EMPTY.withColor(net.minecraft.ChatFormatting.GREEN)),
                false);
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[BoostedBrightness] %s (%s: %s)%n", message,
                ex.getClass().getSimpleName(), ex.getLocalizedMessage());
    }

    private static void asDouble(JsonElement el, DoubleConsumer fn) {
        if (el != null && el.isJsonPrimitive() && ((JsonPrimitive) el).isNumber())
            fn.accept(el.getAsDouble());
    }

    private static void asInt(JsonElement el, IntConsumer fn) {
        if (el != null && el.isJsonPrimitive() && ((JsonPrimitive) el).isNumber())
            fn.accept(el.getAsInt());
    }
}
