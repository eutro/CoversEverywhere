package eutros.coverseverywhere.api;

import net.minecraft.util.EnumFacing;

/**
 * An enum of the sections that are shown on the grid revealed by {@link ICoverRevealer}.
 *
 * Covers Everywhere may display a grid on the faces of the selected block, if the player is holding a
 * suitable {@link ICoverRevealer cover revealer}. This grid is what Covers Everywhere items use
 * to decide which face of the block to affect.
 *
 * The grid looks like such:
 *
 *  CORNER |   UP   |   UP   | CORNER
 * -----------------------------------
 *   LEFT  | CENTER | CENTER | RIGHT
 * -----------------------------------
 *   LEFT  | CENTER | CENTER | RIGHT
 * -----------------------------------
 *  CORNER |  DOWN  |  DOWN  | CORNER
 *
 *  CORNER means the opposite side should be selected,
 *  UP means the top side should be selected, or north when on the top/bottom side.
 *  DOWN means the bottom side should be selected, or south when on the top/bottom side.
 *  LEFT means the side to the left should be selected, or west when on the top/bottom side.
 *  RIGHT means the side to the right should be selected, or east when on the top/bottom side.
 */
public enum GridSection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    CORNER,
    CENTER,
    ;

    /**
     * Offset the given side with this grid section.
     *
     * When a side is selected with the grid, this is used to get the side that should be affected.
     *
     * @param side The side the grid is on.
     * @return The side that should be affected.
     */
    public EnumFacing offset(EnumFacing side) {
        switch(this) {
            case UP:
                if(side.getAxis() != EnumFacing.Axis.Y) return EnumFacing.UP;
                else return EnumFacing.NORTH;
            case DOWN:
                if(side.getAxis() != EnumFacing.Axis.Y) return EnumFacing.DOWN;
                else return EnumFacing.SOUTH;
            case LEFT:
                if(side.getAxis() == EnumFacing.Axis.Y) return EnumFacing.WEST;
                return side.rotateY();
            case RIGHT:
                if(side.getAxis() == EnumFacing.Axis.Y) return EnumFacing.EAST;
                return side.rotateYCCW();
            case CORNER:
                return side.getOpposite();
            default:
                return side;
        }
    }

    /**
     * Get the grid section from the hit side and the position of the hit.
     *
     * All coordinates should be between 0 and 1.
     *
     * @param side The side of the block that was selected.
     * @param x The x coordinate of the selection, relative to the corner of the block closest to the origin.
     * @param y The y coordinate of the selection, relative to the corner of the block closest to the origin.
     * @param z The z coordinate of the selection, relative to the corner of the block closest to the origin.
     * @return The selected {@link GridSection}.
     */
    public static GridSection fromXYZ(EnumFacing side, float x, float y, float z) {
        float sx, sy;
        switch(side) {
            case DOWN:
                sx = x;
                sy = z;
                break;
            case UP:
                sx = x;
                sy = 1 - z;
                break;
            case NORTH:
                sy = y;
                sx = 1 - x;
                break;
            case SOUTH:
                sy = y;
                sx = x;
                break;
            case WEST:
                sy = y;
                sx = z;
                break;
            case EAST:
                sy = y;
                sx = 1 - z;
                break;
            default:
                sx = sy = 0.5F;
        }
        return fromXY(sx, sy);
    }

    private static final GridSection[][] GRID = {
            {CORNER, DOWN, DOWN, CORNER},
            {LEFT, CENTER, CENTER, RIGHT},
            {LEFT, CENTER, CENTER, RIGHT},
            {CORNER, UP, UP, CORNER},
    };

    /**
     * Get the grid section from the selected x and y coordinates of a face.
     *
     * All coordinates should be between 0 and 1.
     *
     * @param x The x coordinate of the selection, relative to the corner of the block closest to the origin.
     * @param y The y coordinate of the selection, relative to the corner of the block closest to the origin.
     * @return The selected {@link GridSection}.
     */
    public static GridSection fromXY(float x, float y) {
        return GRID[Math.floorMod((int) (y * 4), 4)][Math.floorMod((int) (x * 4), 4)];
    }
}
