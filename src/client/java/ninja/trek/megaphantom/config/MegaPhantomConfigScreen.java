package ninja.trek.megaphantom.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class MegaPhantomConfigScreen extends Screen {
    private final Screen parent;
    private EditBox thresholdField;

    public MegaPhantomConfigScreen(Screen parent) {
        super(Component.literal("MegaPhantom Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        thresholdField = new EditBox(this.font, centerX - 50, centerY - 20, 100, 20,
                Component.literal("Swoop Threshold"));
        thresholdField.setValue(String.valueOf(MegaPhantomConfig.get().swoopThreshold));
        thresholdField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        this.addRenderableWidget(thresholdField);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
            try {
                int value = Integer.parseInt(thresholdField.getValue());
                if (value > 0) {
                    MegaPhantomConfig.get().swoopThreshold = value;
                    MegaPhantomConfig.save();
                }
            } catch (NumberFormatException ignored) {
            }
            this.onClose();
        }).bounds(centerX - 50, centerY + 10, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            this.onClose();
        }).bounds(centerX - 50, centerY + 40, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "Swoop Threshold:", this.width / 2, this.height / 2 - 35, 0xAAAAAA);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
