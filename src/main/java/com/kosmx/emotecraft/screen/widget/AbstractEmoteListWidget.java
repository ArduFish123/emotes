package com.kosmx.emotecraft.screen.widget;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.math.Helper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractEmoteListWidget<E extends AbstractEmoteListWidget.AbstractEmoteEntry<E>> extends AlwaysSelectedEntryListWidget<E> {

    protected List<E> emotes = new ArrayList<>();
    private final Screen screen;

    public AbstractEmoteListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
        super(minecraftClient, i, j, k, l, m);
        this.centerListVertically = false;
        this.screen = screen;
    }



    @Override
    public int getRowWidth() {
        return this.width-5;
    }

    public abstract void setEmotes(List<EmoteHolder> list);

    public void filter(Supplier<String> string){
        this.clearEntries();
        for(E emote : this.emotes){
            if(emote.emote.name.getString().toLowerCase().contains(string.get()) || emote.emote.description.getString().toLowerCase().contains(string.get()) || emote.emote.author.getString().toLowerCase().equals(string.get())){
                this.addEntry(emote);
            }
        }
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.right - 6;
    }

    @Override
    protected boolean isFocused() {
        return screen.getFocused() == this;
    }

    public static abstract class AbstractEmoteEntry<T extends AbstractEmoteEntry<T>> extends AlwaysSelectedEntryListWidget.Entry<T> {
        protected final MinecraftClient client;
        public final EmoteHolder emote;

        public AbstractEmoteEntry(MinecraftClient client, EmoteHolder emote){
            this.client = client;
            this.emote = emote;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            if(this.client.options.touchscreen || hovered){
                RenderSystem.color4f(1, 1, 1, 1);
                DrawableHelper.fill(matrices, x - 1, y - 1, x + entryWidth - 9, y + entryHeight + 1, Helper.colorHelper(66, 66, 66, 128));
            }
            this.client.textRenderer.drawWithShadow(matrices, this.emote.name, x + 2, y + 1, 16777215);
            this.client.textRenderer.drawWithShadow(matrices, this.emote.description, x + 2, y + 12, 8421504);
            this.client.textRenderer.drawWithShadow(matrices, new LiteralText("Author: ").formatted(Formatting.GOLD).append((Text) this.emote.author), x + 2, y + 23, 8421504);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(button == 0){
                this.onPressed();
                return true;
            }
            else {
                return false;
            }
        }

        protected abstract void onPressed();
    }
}
