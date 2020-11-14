package eutros.coverseverywhere.api;

import net.minecraft.util.EnumFacing;

public enum GridSection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    CORNER,
    CENTER,
    ;

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
                sx = sy = 2;
        }
        return fromXY(sx, sy);
    }

    private static GridSection[][] GRID = {
            {CORNER, DOWN, DOWN, CORNER},
            {LEFT, CENTER, CENTER, RIGHT},
            {LEFT, CENTER, CENTER, RIGHT},
            {CORNER, UP, UP, CORNER},
    };

    public static GridSection fromXY(float x, float y) {
        return GRID[Math.floorMod((int) (y * 4), 4)][Math.floorMod((int) (x * 4), 4)];
    }
}
