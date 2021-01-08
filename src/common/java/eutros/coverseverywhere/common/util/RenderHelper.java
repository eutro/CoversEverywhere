package eutros.coverseverywhere.common.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderHelper {

    public static void side(BufferBuilder buff, TextureAtlasSprite sprite, BlockPos pos, EnumFacing side) {
        float
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3,
                x4, y4, z4;
        switch (side) {
            case DOWN:
                x3 = x4 = pos.getX();
                x1 = x2 = pos.getX() + 1;
                z1 = z4 = pos.getZ();
                z2 = z3 = pos.getZ() + 1;
                y1 = y2 = y3 = y4 = pos.getY() - 0.01F;
                break;
            case UP:
                x1 = x2 = pos.getX();
                x3 = x4 = pos.getX() + 1;
                z1 = z4 = pos.getZ();
                z2 = z3 = pos.getZ() + 1;
                y1 = y2 = y3 = y4 = pos.getY() + 1 + 0.01F;
                break;
            case NORTH:
                y1 = y4 = pos.getY();
                y2 = y3 = pos.getY() + 1;
                x1 = x2 = pos.getX();
                x3 = x4 = pos.getX() + 1;
                z1 = z2 = z3 = z4 = pos.getZ() - 0.01F;
                break;
            case SOUTH:
                y1 = y4 = pos.getY();
                y2 = y3 = pos.getY() + 1;
                x3 = x4 = pos.getX();
                x1 = x2 = pos.getX() + 1;
                z1 = z2 = z3 = z4 = pos.getZ() + 1 + 0.01F;
                break;
            case WEST:
                y1 = y4 = pos.getY();
                y2 = y3 = pos.getY() + 1;
                z3 = z4 = pos.getZ();
                z1 = z2 = pos.getZ() + 1;
                x1 = x2 = x3 = x4 = pos.getX() - 0.01F;
                break;
            case EAST:
                y1 = y4 = pos.getY();
                y2 = y3 = pos.getY() + 1;
                z1 = z2 = pos.getZ();
                z3 = z4 = pos.getZ() + 1;
                x1 = x2 = x3 = x4 = pos.getX() + 1 + 0.01F;
                break;
            default:
                x1 = y1 = z1 = x2 = y2 = z2 = x3 = y3 = z3 = x4 = y4 = z4 = 0;
        }

        buff.pos(x1, y1, z1)
                .color(1, 1, 1, 1)
                .tex(sprite.getMinU(), sprite.getMaxV())
                .lightmap(0xFFFF, 0xFFFF)
                .endVertex();

        buff.pos(x2, y2, z2)
                .color(1, 1, 1, 1)
                .tex(sprite.getMinU(), sprite.getMinV())
                .lightmap(0xFFFF, 0xFFFF)
                .endVertex();

        buff.pos(x3, y3, z3)
                .color(1, 1, 1, 1)
                .tex(sprite.getMaxU(), sprite.getMinV())
                .lightmap(0xFFFF, 0xFFFF)
                .endVertex();

        buff.pos(x4, y4, z4)
                .color(1, 1, 1, 1)
                .tex(sprite.getMaxU(), sprite.getMaxV())
                .lightmap(0xFFFF, 0xFFFF)
                .endVertex();
    }

    public static void sideDouble(BufferBuilder buff, TextureAtlasSprite sprite, BlockPos pos, EnumFacing side) {
        side(buff, sprite, pos, side);
        side(buff, sprite, pos.offset(side), side.getOpposite());
    }

    public static void icon(int x, int y, int z, TextureAtlasSprite sprite) {
        int w = sprite.getIconWidth();
        x -= w / 2;
        int h = sprite.getIconHeight();
        y -= h / 2;
        int O = 0;

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buf.pos(x + O, y + O, z).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        buf.pos(x + O, y + h, z).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buf.pos(x + w, y + h, z).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buf.pos(x + w, y + O, z).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();

        tes.draw();
    }

}
