package net.boostedbrightness;

import com.google.gson.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import static net.minecraft.util.Formatting.GREEN;

public class BoostedBrightness implements ClientModInitializer {
    public static final int MAX_BRIGHTNESSES = 5;
    private static final Gson GSON = new Gson();

    public static double minBrightness = -1.0;
    public static double maxBrightness = 15.0; // Erweiterter Bereich für Sodium-Kompatibilität
    public static double brightnessSliderInterval = 0.05;
    private static double step = 0.1;

    public static ArrayList<Double> brightnesses;
    private static int brightnessIndex = 0;
    private static int lastBrightnessIndex = 0;

    private static final KeyBinding.Category BOOSTED_CATEGORY =
            KeyBinding.Category.create(Identifier.of(FabricLoader.getInstance().getModContainer("boostedbrightness").get().getMetadata().getId() + ".main"));

    private static final KeyBinding NEXT_BIND = new KeyBinding(
            "key.boosted-brightness.next",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            BOOSTED_CATEGORY
    );

    private static final KeyBinding RAISE_BIND = new KeyBinding(
            "key.boosted-brightness.raise",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            BOOSTED_CATEGORY
    );

    private static final KeyBinding LOWER_BIND = new KeyBinding(
            "key.boosted-brightness.lower",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            BOOSTED_CATEGORY
    );

    private static final KeyBinding[] SELECT_BINDS = new KeyBinding[MAX_BRIGHTNESSES];

    public static MinecraftClient client;

    // Sodium Kompatibilitäts-Flag
    public static boolean isSodiumLoaded = false;

    public static void changeBrightness(double brightness) {
        if (brightnesses == null || brightnesses.isEmpty()) {
            return;
        }

        double clampedBrightness = Math.min(maxBrightness, Math.max(minBrightness, brightness));
        brightnesses.set(getBrightnessIndex(), clampedBrightness);

        if (client != null && client.options != null) {
            try {
                client.options.getGamma().setValue(clampedBrightness);
            } catch (Exception e) {
                LOGGER.warn("Failed to set gamma value: " + e.getMessage());
            }
        }
    }

    public static int numBrightnesses() {
        return brightnesses != null ? brightnesses.size() : 0;
    }

    public static int getBrightnessIndex() {
        return brightnessIndex;
    }

    public static void setBrightnessIndex(int index) {
        if (brightnesses == null || brightnesses.isEmpty()) {
            return;
        }

        brightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, index));

        if (client != null && client.options != null) {
            try {
                double brightness = getBrightness();
                client.options.getGamma().setValue(brightness);
            } catch (Exception e) {
                LOGGER.warn("Failed to set gamma value: " + e.getMessage());
            }
        }
    }

    public static double getBrightness() {
        if (brightnesses == null || brightnesses.isEmpty()) {
            return 1.0; // Default fallback
        }
        return brightnesses.get(brightnessIndex);
    }

    public static double getBrightness(int index) {
        if (brightnesses == null || brightnesses.isEmpty() || index < 0 || index >= brightnesses.size()) {
            return 1.0; // Default fallback
        }
        return brightnesses.get(index);
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
                for (int i = 0; i < brightnesses.size() && i < MAX_BRIGHTNESSES; i++) {
                    config.addProperty(String.valueOf(i + 1), brightnesses.get(i));
                }
            }

            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            Files.write(configPath, GSON.toJson(config).getBytes());
        } catch (IOException ex) {
            logException(ex, "Failed to save BoostedBrightness config");
        } catch (Exception ex) {
            logException(ex, "Unexpected error while saving config");
        }
    }

    public static void changeBrightness(int index, double brightness) {
        if (brightnesses == null || index < 0 || index >= brightnesses.size()) {
            return;
        }

        if (index == brightnessIndex) {
            changeBrightness(brightness);
        } else {
            double clampedBrightness = Math.min(maxBrightness, Math.max(minBrightness, brightness));
            brightnesses.set(index, clampedBrightness);
        }
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[BoostedBrightness] %s (%s: %s)\n", message, ex.getClass().getSimpleName(), ex.getLocalizedMessage());
    }

    @Override
    public void onInitializeClient() {
        // Sodium Kompatibilität prüfen
        isSodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");

        KeyBindingHelper.registerKeyBinding(NEXT_BIND);
        KeyBindingHelper.registerKeyBinding(RAISE_BIND);
        KeyBindingHelper.registerKeyBinding(LOWER_BIND);

        for (int i = 0; i < MAX_BRIGHTNESSES; i++) {
            SELECT_BINDS[i] = new KeyBinding(
                    "key.boosted-brightness.select" + (i + 1),
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    BOOSTED_CATEGORY
            );

            KeyBindingHelper.registerKeyBinding(SELECT_BINDS[i]);
        }

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        loadConfig();
        client = MinecraftClient.getInstance();

        LOGGER.info("Boosted Brightness initialized" + (isSodiumLoaded ? " with Sodium compatibility" : ""));
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("boostedbrightness.json");
    }

    private void asDouble(JsonElement element, DoubleConsumer onSuccess) {
        if (element != null && element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber()) {
            onSuccess.accept(element.getAsDouble());
        }
    }

    private void asInt(JsonElement element, IntConsumer onSuccess) {
        if (element != null && element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber()) {
            onSuccess.accept(element.getAsInt());
        }
    }

    private void loadConfig() {
        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            // Erstelle Standardkonfiguration
            brightnesses = new ArrayList<>();
            brightnesses.add(1.0);
            brightnesses.add(maxBrightness);
            brightnessIndex = 0;
            lastBrightnessIndex = 0;
            saveConfig(); // Speichere die Standardkonfiguration
            return;
        }

        try {
            String configContent = new String(Files.readAllBytes(configPath));
            JsonObject config = GSON.fromJson(configContent, JsonObject.class);

            if (config == null) {
                throw new JsonSyntaxException("Config file is empty or invalid");
            }

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
            // Fallback auf Standardwerte
            brightnesses = new ArrayList<>();
            brightnesses.add(1.0);
            brightnesses.add(maxBrightness);
            brightnessIndex = 0;
            lastBrightnessIndex = 0;
        }

        // Sicherstellen, dass wir mindestens 2 Werte haben
        if (brightnesses == null || brightnesses.size() < 2) {
            brightnesses = new ArrayList<>();
            brightnesses.add(1.0);
            brightnesses.add(maxBrightness);
            if (brightnessIndex >= brightnesses.size()) {
                brightnessIndex = 0;
            }
            if (lastBrightnessIndex >= brightnesses.size()) {
                lastBrightnessIndex = 0;
            }
        }
    }

    private void showOverlay(MinecraftClient client) {
        if (client.inGameHud == null) return;

        double currentBrightness = getBrightness();
        int percentage;

        if (isSodiumLoaded) {
            // Für Sodium: Umrechnung in den erweiterten Bereich
            percentage = (int) Math.round((currentBrightness / maxBrightness) * 1500);
        } else {
            // Für Vanilla: Normale Prozentberechnung
            percentage = (int) Math.round(currentBrightness * 100);
        }

        client.inGameHud.setOverlayMessage(
                Text.translatable(
                        "overlay.boosted-brightness.change",
                        getBrightnessIndex() + 1,
                        percentage
                ).styled(s -> s.withColor(GREEN)),
                false
        );
    }

    private void onEndTick(MinecraftClient client) {
        if (brightnesses == null || brightnesses.isEmpty()) {
            return;
        }

        while (NEXT_BIND.wasPressed()) {
            lastBrightnessIndex = getBrightnessIndex();
            setBrightnessIndex((lastBrightnessIndex + 1) % numBrightnesses());
            showOverlay(client);
        }

        for (int i = 0; i < numBrightnesses(); i++) {
            while (SELECT_BINDS[i].wasPressed()) {
                int nextBrightnessIndex = (i != brightnessIndex) ? i : lastBrightnessIndex;
                lastBrightnessIndex = getBrightnessIndex();
                setBrightnessIndex(nextBrightnessIndex);
                showOverlay(client);
            }
        }

        double offset = 0;
        while (RAISE_BIND.wasPressed()) offset += step;
        while (LOWER_BIND.wasPressed()) offset -= step;

        if (offset != 0) {
            double currentBrightness = getBrightness();
            double newBrightness = Math.max(minBrightness, Math.min(maxBrightness, currentBrightness + offset));
            changeBrightness(newBrightness);
            showOverlay(client);
        }
    }

    // Logger für die Mod
    public static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger("BoostedBrightness");
}