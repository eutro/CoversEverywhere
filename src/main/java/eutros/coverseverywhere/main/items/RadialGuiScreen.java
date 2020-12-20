package eutros.coverseverywhere.main.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.function.IntConsumer;

public class RadialGuiScreen extends GuiScreen {

    public static void prompt(NonNullList<ItemStack> options, IntConsumer selectionConsumer) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new RadialGuiScreen(options, selectionConsumer));
    }

    private final NonNullList<ItemStack> options;
    private final IntConsumer selectionConsumer;

    public RadialGuiScreen(NonNullList<ItemStack> options, IntConsumer selectionConsumer) {
        this.options = options;
        this.selectionConsumer = selectionConsumer;
    }

    @Override
    public void handleInput() throws IOException {
        super.handleInput();
        if (!shouldPersist()) {
            mc.displayGuiScreen(null);
            selectionConsumer.accept(getSelectedSlice());
        }
    }

    private boolean shouldPersist() {
        return GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private int mouseX, mouseY;

    private static final int ITEM_SIZE = 16;

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
                drawStackCentered(options.get(0), width / 2, height / 2);
                break;
            default:
                double sliceAngle = (Math.PI * 2) / optionsSize;
                double currentAngle = 0;

                for (int i = 0; i < optionsSize; i++) {
                    ItemStack option = options.get(i);
                    drawSlice(option, currentAngle, sliceAngle, selected == i);
                    currentAngle += sliceAngle;
                }
                break;
        }
    }

    private void drawSlice(ItemStack option, double angleMin, double sliceAngle, boolean isHovered) {
        float gray = isHovered ? HOVERED_COLOR : UNHOVERED_COLOR;
        double radius = isHovered ? HOVERED_RADIUS : UNHOVERED_RADIUS;
        double midX = width / 2.0 + CENTER_OFFSET * MathHelper.cos((float) (angleMin + sliceAngle / 2));
        double midY = height / 2.0 - CENTER_OFFSET * MathHelper.sin((float) (angleMin + sliceAngle / 2));
        drawArc(midX, midY, angleMin, sliceAngle, radius, gray);

        double sliceMidX = midX + (radius / 2) * MathHelper.cos((float) (angleMin + sliceAngle / 2));
        double sliceMidY = midY - (radius / 2) * MathHelper.sin((float) (angleMin + sliceAngle / 2));
        drawStackCentered(option, (int) sliceMidX, (int) sliceMidY);
    }

    private void drawStackCentered(ItemStack option, int midX, int midY) {
        itemRender.renderItemIntoGUI(option,
                midX - ITEM_SIZE / 2,
                midY - ITEM_SIZE / 2);
    }

    private void drawArc(double midX, double midY, double angleMin, double angleSize, double radius, float gray) {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        GlStateManager.disableTexture2D();

        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(midX, midY, 0).color(gray, gray, gray, gray).endVertex();
        int triangleCount = (int) (angleSize * TRIANGLES_PER_RADIAN);
        for (int i = 0; i < triangleCount - 1; i++) {
            arcPos(buf, midX, midY, angleMin + angleSize * i / triangleCount, radius)
                    .color(gray, gray, gray, gray).endVertex();
        }
        arcPos(buf, midX, midY, angleMin + angleSize, radius).color(gray, gray, gray, gray).endVertex();

        tes.draw();
        GlStateManager.enableTexture2D();
    }

    private BufferBuilder arcPos(BufferBuilder buf, double midX, double midY, double angle, double radius) {
        return buf.pos(
                midX + radius * MathHelper.cos((float) angle),
                midY - radius * MathHelper.sin((float) angle),
                0);
    }

    private int getSelectedSlice() {
        double dx = mouseX - (width / 2.0);
        double dy = (height / 2.0) - mouseY;
        double angle = (MathHelper.atan2(dy, dx) + Math.PI * 2) % (Math.PI * 2);
        return (int) (options.size() * angle / (Math.PI * 2));
    }

}
