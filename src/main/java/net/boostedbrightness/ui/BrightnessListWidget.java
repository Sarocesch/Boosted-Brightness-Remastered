package net.boostedbrightness.ui;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BrightnessListWidget extends ElementListWidget<BrightnessListWidget.BrightnessEntry> {

    public BrightnessListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom);

        if (client.options.getGamma().getValue() != BoostedBrightness.getBrightness()) {
            BoostedBrightness.changeBrightness(client.options.getGamma().getValue());
        }

        for (int idx = 0; idx < BoostedBrightness.numBrightnesses(); idx++) {
            this.addEntry(BrightnessEntry.create(idx, this.width, this));
        }

        if (BoostedBrightness.numBrightnesses() < BoostedBrightness.MAX_BRIGHTNESSES) {
            this.addEntry(BrightnessEntry.create(-1, this.width, this));
        }
    }

    public void addBrightness() {
        List<BrightnessEntry> entries = this.children();

        BoostedBrightness.brightnesses.add(1.0);
        int size = BoostedBrightness.numBrightnesses();

        entries.add(size - 1, BrightnessEntry.create(size - 1, this.width, this));

        if (size == BoostedBrightness.MAX_BRIGHTNESSES) {
            entries.remove(BoostedBrightness.MAX_BRIGHTNESSES);
        }
    }

    public void removeBrightness(int index) {
        List<BrightnessEntry> entries = this.children();

        int oldSize = BoostedBrightness.brightnesses.size();

        BoostedBrightness.brightnesses.remove(index);
        entries.remove(oldSize - 1);

        for (int i = index; i < oldSize - 1; i++) {
            entries.get(i).updateValue();
        }

        if (oldSize == BoostedBrightness.MAX_BRIGHTNESSES) {
            entries.add(BrightnessEntry.create(-1, this.width, this));
        }

        if (BoostedBrightness.getBrightnessIndex() == BoostedBrightness.numBrightnesses()) {
            BoostedBrightness.setBrightnessIndex(BoostedBrightness.getBrightnessIndex() - 1);
        }
    }

    @Override
    public int getRowWidth() {
        return 300;
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft() + 32;
    }

    public Optional<ClickableWidget> getHoveredButton(double mouseX, double mouseY) {
        for (BrightnessEntry entry : this.children()) {
            for (ClickableWidget button : entry.buttons) {
                if (button.isMouseOver(mouseX, mouseY)) return Optional.of(button);
            }
        }
        return Optional.empty();
    }

    protected boolean isSelectedEntry(int index) {
        return BoostedBrightness.getBrightnessIndex() == index;
    }

    public static class BrightnessEntry extends ElementListWidget.Entry<BrightnessEntry> {
        private final List<ClickableWidget> buttons;
        private final BrightnessListWidget listWidget;
        private int index;

        private BrightnessEntry(List<ClickableWidget> buttons, int index, BrightnessListWidget listWidget) {
            this.buttons = buttons;
            this.listWidget = listWidget;
            this.index = index;
        }

        public static BrightnessEntry create(int index, int width, BrightnessListWidget listWidget) {
            List<ClickableWidget> widgets = new ArrayList<>();

            if (index >= 0) {
                widgets.add(new BrightnessSliderWidget(index, width / 2 - 120, 0, 240, 20,
                        BrightnessSliderWidget.sliderValue(BoostedBrightness.getBrightness(index))));

                if (index >= 2) {
                    widgets.add(ButtonWidget.builder(Text.literal("X"), b -> listWidget.removeBrightness(index))
                            .size(20, 20).position(width / 2 + 125, 0).build());
                }
            } else {
                widgets.add(ButtonWidget.builder(Text.literal("+"), b -> listWidget.addBrightness())
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }

            return new BrightnessEntry(widgets, index, listWidget);
        }

        public void updateValue() {
            for (ClickableWidget button : buttons)
                if (button instanceof BrightnessSliderWidget slider)
                    slider.updateValue();
        }

        @Override
        public void render(DrawContext context, int index, int y, boolean hovered, float tickDelta) {
            for (ClickableWidget button : buttons) {
                button.setY(y);
                button.render(context, 0, 0, tickDelta);
            }

            if (this.index >= 0) {
                context.drawTextWithShadow(listWidget.client.textRenderer,
                        String.valueOf(this.index + 1),
                        listWidget.width / 2 - 150 + 13,
                        y + 6,
                        0xFFFFFF);
            }
        }

        @Override
        public List<? extends Element> children() {
            return buttons;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return buttons;
        }
    }

}
