package eutros.coverseverywhere.main.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

public class RadialGuiScreen extends GuiScreen {

    public static void prompt(List<ItemStack> stacks, IntConsumer selectionConsumer) {
        Minecraft mc = Minecraft.getMinecraft();
        List<Option> options = new ArrayList<>();
        for (ItemStack stack : stacks) options.add(new StackOption(stack));
        mc.displayGuiScreen(new RadialGuiScreen(true, options, selectionConsumer));
    }

    public static void prompt(IntConsumer selectionConsumer, Option... options) {
        prompt(true, selectionConsumer, options);
    }

    public static void prompt(boolean closeOnChoice, IntConsumer selectionConsumer, Option... options) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new RadialGuiScreen(closeOnChoice, Arrays.asList(options), selectionConsumer));
    }

    private final boolean closeOnChoice;
    private final List<Option> options;
    private final IntConsumer selectionConsumer;

    private RadialGuiScreen(boolean closeOnChoice, List<Option> options, IntConsumer selectionConsumer) {
        this.closeOnChoice = closeOnChoice;
        this.options = ImmutableList.copyOf(options);
        this.selectionConsumer = selectionConsumer;
    }

    private boolean hasPressed = GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindUseItem);

    @Override
    public void handleInput() throws IOException {
        super.handleInput();
        if (!shouldPersist()) {
            if (closeOnChoice) mc.displayGuiScreen(null);
            else hasPressed = false;
            int selected = getSelectedSlice();
            if (selected != -1) selectionConsumer.accept(selected);
        }
    }

    private boolean shouldPersist() {
        if (!hasPressed) {
            hasPressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem);
            return true;
        }
        return GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private int mouseX, mouseY;

    private static final double HOVERED_RADIUS = 110;
    private static final double UNHOVERED_RADIUS = 100;
    private static final float UNHOVERED_COLOR = 0.5F;
    private static final float HOVERED_COLOR = 0.8F;
    private static final double CENTER_OFFSET = 2;
    private static final int TRIANGLES_PER_RADIAN = 512;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int selected = getSelectedSlice();
        int optionsSize = options.size();

        switch (optionsSize) {
            case 0:
                drawArc(width / 2.0,
                        height / 2.0,
                        0,
                        Math.PI * 2,
                        UNHOVERED_RADIUS,
                        UNHOVERED_COLOR);
                break;
            case 1:
                drawArc(width / 2.0,
                        height / 2.0,
                        0,
                        Math.PI * 2,
                        HOVERED_RADIUS,
                        HOVERED_COLOR);
                options.get(0).renderAt(width / 2, height / 2);
                break;
            default:
                double sliceAngle = (Math.PI * 2) / optionsSize;
                double currentAngle = 0;

                for (int i = 0; i < optionsSize; i++) {
                    Option option = options.get(i);
                    drawSlice(option, currentAngle, sliceAngle, selected == i);
                    currentAngle += sliceAngle;
                }
                break;
        }

        int slice = getSelectedSlice();
        if (slice != -1) drawHoveringText(options.get(slice).getTooltip(), mouseX, mouseY);
    }

    private void drawSlice(Option option, double angleMin, double sliceAngle, boolean isHovered) {
        float gray = isHovered ? HOVERED_COLOR : UNHOVERED_COLOR;
        double radius = isHovered ? HOVERED_RADIUS : UNHOVERED_RADIUS;
        double midX = width / 2.0 + CENTER_OFFSET * MathHelper.cos((float) (angleMin + sliceAngle / 2));
        double midY = height / 2.0 - CENTER_OFFSET * MathHelper.sin((float) (angleMin + sliceAngle / 2));
        drawArc(midX, midY, angleMin, sliceAngle, radius, gray);

        double sliceMidX = midX + (radius / 2) * MathHelper.cos((float) (angleMin + sliceAngle / 2));
        double sliceMidY = midY - (radius / 2) * MathHelper.sin((float) (angleMin + sliceAngle / 2));
        option.renderAt((int) sliceMidX, (int) sliceMidY);
    }

    private void drawArc(double midX, double midY, double angleMin, double angleSize, double radius, float gray) {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(midX, midY, 0).color(gray, gray, gray, gray).endVertex();
        int triangleCount = (int) (angleSize * TRIANGLES_PER_RADIAN);
        for (int i = 0; i < triangleCount - 1; i++) {
            arcPos(buf, midX, midY, angleMin + angleSize * i / triangleCount, radius)
                    .color(gray, gray, gray, gray).endVertex();
        }
        arcPos(buf, midX, midY, angleMin + angleSize, radius).color(gray, gray, gray, gray).endVertex();

        tes.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private BufferBuilder arcPos(BufferBuilder buf, double midX, double midY, double angle, double radius) {
        return buf.pos(
                midX + radius * MathHelper.cos((float) angle),
                midY - radius * MathHelper.sin((float) angle),
                0);
    }

    private int getSelectedSlice() {
        if (options.size() == 0) return -1;
        double dx = mouseX - (width / 2.0);
        double dy = (height / 2.0) - mouseY;
        double angle = (MathHelper.atan2(dy, dx) + Math.PI * 2) % (Math.PI * 2);
        return (int) (options.size() * angle / (Math.PI * 2));
    }

}
