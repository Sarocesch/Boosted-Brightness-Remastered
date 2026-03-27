package net.boostedbrightness.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class BrightnessListWidget extends ContainerObjectSelectionList<BrightnessListWidget.BrightnessEntry> {

    public BrightnessListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);

        if (client.options.gamma().get() != BoostedBrightness.getBrightness()) {
            BoostedBrightness.changeBrightness(client.options.gamma().get());
        }

        for (int idx = 0; idx < BoostedBrightness.numBrightnesses(); idx++) {
            this.addEntry(BrightnessEntry.create(idx, this.width, this));
        }

        if (BoostedBrightness.numBrightnesses() < BoostedBrightness.MAX_BRIGHTNESSES) {
            this.addEntry(BrightnessEntry.create(-1, this.width, this));
        }
    }

    public void addBrightness() {
        List<BrightnessEntry> entries = new ArrayList<>(this.children());

        BoostedBrightness.brightnesses.add(1.0);
        int size = BoostedBrightness.numBrightnesses();

        this.clearEntries();
        for (int i = 0; i < size; i++) {
            this.addEntry(BrightnessEntry.create(i, this.width, this));
        }

        if (size < BoostedBrightness.MAX_BRIGHTNESSES) {
            this.addEntry(BrightnessEntry.create(-1, this.width, this));
        }
    }

    public void removeBrightness(int index) {
        int oldSize = BoostedBrightness.brightnesses.size();
        BoostedBrightness.brightnesses.remove(index);

        this.clearEntries();
        for (int i = 0; i < BoostedBrightness.numBrightnesses(); i++) {
            this.addEntry(BrightnessEntry.create(i, this.width, this));
        }

        if (BoostedBrightness.numBrightnesses() < BoostedBrightness.MAX_BRIGHTNESSES) {
            this.addEntry(BrightnessEntry.create(-1, this.width, this));
        }

        if (BoostedBrightness.getBrightnessIndex() >= BoostedBrightness.numBrightnesses()) {
            BoostedBrightness.setBrightnessIndex(BoostedBrightness.numBrightnesses() - 1);
        }
    }

    @Override
    public int getRowWidth() {
        return 300;
    }

    public Optional<AbstractWidget> getHoveredButton(double mouseX, double mouseY) {
        for (BrightnessEntry entry : this.children()) {
            for (AbstractWidget button : entry.buttons) {
                if (button.isMouseOver(mouseX, mouseY)) {
                    return Optional.of(button);
                }
            }
        }
        return Optional.empty();
    }

    protected boolean isSelectedEntry(int index) {
        return BoostedBrightness.getBrightnessIndex() == index;
    }

    public static class BrightnessEntry extends ContainerObjectSelectionList.Entry<BrightnessEntry> {
        final List<AbstractWidget> buttons;
        private final BrightnessListWidget listWidget;
        private final int index;

        private BrightnessEntry(List<AbstractWidget> buttons, int index, BrightnessListWidget listWidget) {
            this.buttons = buttons;
            this.listWidget = listWidget;
            this.index = index;
        }

        public static BrightnessEntry create(int index, int width, BrightnessListWidget listWidget) {
            ArrayList<AbstractWidget> widgets = new ArrayList<>();

            if (index >= 0) {
                widgets.add(new BrightnessSliderWidget(index, width / 2 - 120, 0, 240, 20,
                        BrightnessSliderWidget.sliderValue(BoostedBrightness.getBrightness(index))));

                if (index >= 2) {
                    widgets.add(Button.builder(Component.literal("X"), (button) -> {
                        listWidget.removeBrightness(index);
                    }).size(20, 20).pos(width / 2 + 120 + 5, 0).build());
                }
            } else {
                widgets.add(Button.builder(Component.literal("+"), (button) -> {
                    listWidget.addBrightness();
                }).size(240, 20).pos(width / 2 - 120, 0).build());
            }

            return new BrightnessEntry(widgets, index, listWidget);
        }

        public void updateValue() {
            for (AbstractWidget button : buttons)
                if (button instanceof BrightnessSliderWidget slider)
                    slider.updateValue();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor guiGraphics, int x, int y, boolean hovered, float partialTick) {
            int mouseX = (int) this.listWidget.minecraft.mouseHandler.xpos();
            int mouseY = (int) this.listWidget.minecraft.mouseHandler.ypos();
            for (AbstractWidget button : this.buttons) {
                button.setY(y);
                button.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            }

            if (this.index >= 0) {
                guiGraphics.text(listWidget.minecraft.font, String.valueOf(this.index + 1),
                        listWidget.width / 2 - 150 + 13, y + 6, 0xFFFFFF);
            }
        }

        public boolean handleClick(double mouseX, double mouseY, int mouseButton) {
            for (AbstractWidget button : this.buttons) {
                if (button.isMouseOver(mouseX, mouseY)) {
                    // Let the widget handle its own click
                    return true;
                }
            }
            if (this.index >= 0) {
                BoostedBrightness.setBrightnessIndex(this.index);
            }
            return true;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.buttons;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.buttons;
        }
    }
}
