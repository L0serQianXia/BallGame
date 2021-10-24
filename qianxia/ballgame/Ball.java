package qianxia.ballgame;

public class Ball {
    /** 球的颜色 */
    private EnumBallColor color;
    /** 球的排数 */
    private int row;
    /** 球的列数 */
    private int column;
    private boolean moving;
    private int animWidth;
    private int animHeight;

    public Ball(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public Ball(EnumBallColor color, int row, int column) {
        this.color = color;
        this.row = row;
        this.column = column;
    }

    public EnumBallColor getColor() {
        return color;
    }

    public void setColor(EnumBallColor color) {
        this.color = color;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public int getAnimWidth() {
        return animWidth;
    }

    public void setAnimWidth(int animWidth) {
        this.animWidth = animWidth;
    }

    public int getAnimHeight() {
        return animHeight;
    }

    public void setAnimHeight(int animHeight) {
        this.animHeight = animHeight;
    }

    public int getAndAddAnimWidth() {
        return animWidth++;
    }

    public int getAndAddAnimHeight() {
        return animHeight++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ball ball = (Ball) o;

        if (row != ball.row) return false;
        return column == ball.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }
}
