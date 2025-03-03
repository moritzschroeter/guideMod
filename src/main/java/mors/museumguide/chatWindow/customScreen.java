package mors.museumguide.chatWindow;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class customScreen extends Screen {
    public customScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        // Hello World button - keep at the top
        ButtonWidget buttonWidget = ButtonWidget.builder(Text.of("Hello World"), (btn) -> {
            // When the button is clicked, we can display a toast to the screen.
            assert this.client != null;
            this.client.getToastManager().add(
                    SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Hello World!"), Text.of("This is a toast."))
            );
        }).dimensions(40, 40, 120, 20).build();
        this.addDrawableChild(buttonWidget);

        // Text input field - place below the Hello World button
        TextFieldWidget chatInput = new TextFieldWidget(this.textRenderer, 40, 80, 120, 20, Text.of("Type here..."));
        this.addDrawableChild(chatInput);

        // Send to Console button - place below the text field
        ButtonWidget sendConsole = ButtonWidget.builder(Text.of("Send to Console"), (btn) -> {
            String message = chatInput.getText();
            if (!message.isEmpty())    {
                System.out.println(message);
            }
            else System.out.println("No message entered.");
        }).dimensions(40, 110, 120, 20).build();  // Changed Y from 40 to 110 and height from 60 to 20
        this.addDrawableChild(sendConsole);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Minecraft doesn't have a "label" widget, so we'll have to draw our own text.
        // We'll subtract the font height from the Y position to make the text appear above the button.
        // Subtracting an extra 10 pixels will give the text some padding.
        // textRenderer, text, x, y, color, hasShadow
        context.drawText(this.textRenderer, "Special Button", 40, 40 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
    }
}