package net.boostedbrightness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.OptionInstance;
import net.minecraft.resources.ResourceLocation;

@Mod(BoostedBrightness.MODID)
// Note: Not using @Mod.EventBusSubscriber since Forge 60 EventBus API changes
// Event registration is done manually in the constructor
public class BoostedBrightness {
    public static final String MODID = "boostedbrightness";
    public static final int MAX_BRIGHTNESSES = 5;
    private static final Gson GSON = new Gson();

    public static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(
            ResourceLocation.fromNamespaceAndPath(MODID, "keys"));

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

    private static BoostedBrightness instance;

    public BoostedBrightness() {
        instance = this;
        // Note: Forge 60 EventBus API has changed significantly
        // Config loading moved to static init
        loadConfig();
        client = Minecraft.getInstance();
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        loadConfig();
        client = Minecraft.getInstance();
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // Create key mappings with our category
        NEXT_BIND = new KeyMapping(
                "key.boosted-brightness.next",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KEY_CATEGORY);

        RAISE_BIND = new KeyMapping(
                "key.boosted-brightness.raise",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                KEY_CATEGORY);

        LOWER_BIND = new KeyMapping(
                "key.boosted-brightness.lower",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                KEY_CATEGORY);

        event.register(NEXT_BIND);
        event.register(RAISE_BIND);
        event.register(LOWER_BIND);

        for (int i = 0; i < MAX_BRIGHTNESSES; i++) {
            SELECT_BINDS[i] = new KeyMapping(
                    "key.boosted-brightness.select" + (i + 1),
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    KEY_CATEGORY);
            event.register(SELECT_BINDS[i]);
        }
    }

    public static int numBrightnesses() {
        return brightnesses.size();
    }

    public static int getBrightnessIndex() {
        return brightnessIndex;
    }

    public static void setBrightnessIndex(int index) {
        brightnessIndex = index;
        setGammaValue(getBrightness());
    }

    public static double getBrightness() {
        return brightnesses.get(brightnessIndex);
    }

    public static double getBrightness(int index) {
        return brightnesses.get(index);
    }

    public static void changeBrightness(double brightness) {
        brightnesses.set(getBrightnessIndex(), brightness);
        setGammaValue(getBrightness());
    }

    public static void setGammaValue(double value) {
        if (client == null || client.options == null)
            return;
        try {
            OptionInstance<Double> gamma = client.options.gamma();
            java.lang.reflect.Field valueField = OptionInstance.class.getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.set(gamma, value);
        } catch (Exception e) {
            try {
                client.options.gamma().set(Math.max(0.0, Math.min(1.0, value)));
            } catch (Exception ignored) {
            }
        }
    }

    public static void changeBrightness(int index, double brightness) {
        if (index == brightnessIndex)
            changeBrightness(brightness);
        else
            brightnesses.set(index, brightness);
    }

    private static void loadConfig() {
        try {
            JsonObject config = GSON.fromJson(new String(Files.readAllBytes(getConfigPath())), JsonObject.class);
            asDouble(config.get("min"), min -> minBrightness = min);
            asDouble(config.get("max"), max -> maxBrightness = max);
            asDouble(config.get("step"), step -> BoostedBrightness.step = step);

            brightnesses = new ArrayList<>();
            for (int i = 1; i <= MAX_BRIGHTNESSES && config.has(String.valueOf(i)); i++) {
                asDouble(config.get(String.valueOf(i)), brightness -> brightnesses.add(brightness));
            }

            asInt(config.get("selected"), selected -> brightnessIndex = selected - 1);
            brightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, brightnessIndex));

            if (config.has("last")) {
                asInt(config.get("last"), last -> lastBrightnessIndex = last - 1);
                lastBrightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, lastBrightnessIndex));
            } else {
                lastBrightnessIndex = 0;
            }
        } catch (IOException | JsonSyntaxException ex) {
            logException(ex, "Failed to load BoostedBrightness config");
        }

        if (brightnesses == null || brightnesses.size() < 2) {
            brightnesses = new ArrayList<>();
            brightnesses.add(1.0);
            brightnesses.add(maxBrightness);
            brightnessIndex = 0;
            lastBrightnessIndex = 0;
        }
    }

    public static void saveConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("min", minBrightness);
        config.addProperty("max", maxBrightness);
        config.addProperty("step", step);
        config.addProperty("selected", brightnessIndex + 1);
        config.addProperty("last", lastBrightnessIndex + 1);

        for (int i = 0; i < brightnesses.size(); i++) {
            config.addProperty(String.valueOf(i + 1), brightnesses.get(i));
        }

        try {
            Files.write(getConfigPath(), GSON.toJson(config).getBytes());
        } catch (IOException ex) {
            logException(ex, "Failed to save BoostedBrightness config");
        }
    }

    private static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("boostedbrightness.json");
    }

    private static void asDouble(JsonElement element, DoubleConsumer onSuccess) {
        if (element != null && element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber()) {
            onSuccess.accept(element.getAsDouble());
        }
    }

    private static void asInt(JsonElement element, IntConsumer onSuccess) {
        if (element != null && element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber()) {
            onSuccess.accept(element.getAsInt());
        }
    }

    private static void showOverlay(Minecraft client) {
        client.gui.setOverlayMessage(
                Component.translatable(
                        "overlay.boosted-brightness.change",
                        getBrightnessIndex() + 1,
                        Math.round(getBrightness() * 100)).withStyle(ChatFormatting.GREEN),
                false);
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[BoostedBrightness] %s (%s: %s)%n", message, ex.getClass().getSimpleName(),
                ex.getLocalizedMessage());
    }

    // TODO: Client tick event handling disabled - Forge 60 EventBus API changes
    // Keybindings for brightness control are currently not functional
    // Need to find correct SubscribeEvent annotation path for Forge 60.1.0
}