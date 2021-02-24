package qianxia.ballgame;

public class Ball {
    /** 球的颜色 */
    private EnumBallColor color;
    /** 球的排数 */
    private int row;
    /** 球的列数 */
    private int column;

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
}
