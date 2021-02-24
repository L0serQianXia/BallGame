package qianxia.ballgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

/**
 * @Author: QianXia
 * @Description: 主界面绘制
 * @Date: 2021/01/22-10:07
 */
public class UI extends JFrame {
    private final int WIDTH = 580, HEIGHT = 560;

    public static UI INSTANCE;
    private Graphics graphics;
    private List<Ball> balls = new ArrayList<>();
    private Random rm = new Random();

    private Ball selectedBall = null;

    public UI() {
        UI.INSTANCE = this;

        this.setTitle("Fake Magic Lines v1.0");
        this.setResizable(false);
        this.setBounds(0, 0, WIDTH, HEIGHT);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                if (e.getButton() == 3) {
                    UI.INSTANCE.balls.clear();
                    UI.INSTANCE.repaint();
                    return;
                }

                Ball ball = UI.INSTANCE.getBallFromMousePosition(mouseX, mouseY);
                int[] position = UI.INSTANCE.getGamePositionByMousePosition(mouseX, mouseY);

                if (UI.INSTANCE.selectedBall != null) {
                    UI.INSTANCE.moveBall(ball, position);
                } else {
                    UI.INSTANCE.selectedBall = ball;
                    UI.INSTANCE.drawBalls();
                }
            }
        });

        this.graphics = this.getGraphics();
    }

    public void moveBall(Ball ball, int[] position) {
        if (this.selectedBall != null) {
            if (this.getBallFromGamePosition(position) != null) {
                this.selectedBall = null;
                return;
            }
            this.selectedBall.setRow(position[0]);
            this.selectedBall.setColumn(position[1]);
            this.selectedBall = null;
            this.randomNewBall(3);
        }

        this.repaint();
        this.drawBalls();
    }

    public int[] getGamePositionByMousePosition(int mouseX, int mouseY) {
        int row = -1;
        int column = -1;

        mainLooping: for (int i = 0; i < 9; i++) {
            for (int j = 0; j <= 80; j++) {
                if (mouseX <= j) {
                    row = i;
                    break mainLooping;
                }
            }
            mouseX -= 60;
        }

        mainLooping: for (int i = 0; i < 9; i++) {
            for (int j = 0; j <= 80; j++) {
                if (mouseY <= j) {
                    column = i;
                    break mainLooping;
                }
            }
            mouseY -= 60;
        }
        return new int[] { row, column };
    }

    public void newBall(int mouseX, int mouseY) {
        int[] position = getGamePositionByMousePosition(mouseX, mouseY);
        this.newBall(EnumBallColor.BLUE, position);
    }

    /**
     * 绘制背景
     */
    public void drawBackground(Graphics g) {
        // 绘制背景的蓝色底色
        g.setColor(new Color(0, 3, 161));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制灰色的盘
        g.setColor(new Color(118, 112, 112));
        g.fillRect(20, 0, 540, HEIGHT);

        // 开始绘制竖线
        int startY = 20;
        g.setColor(new Color(0, 0, 0));
        for (int i = 0; i < 9; i++) {
            g.drawLine(20, startY, 560, startY);
            startY += 60;
        }

        // 开始绘制横线
        int startX = 80;
        for (int i = 0; i <= 8; i++) {
            g.drawLine(startX, 0, startX, HEIGHT);
            startX += 60;
        }
    }

    public void newBall(EnumBallColor color, int[] position) {
        this.newBall(color, position[0], position[1]);
    }

    public void newBall(EnumBallColor color, int row, int column) {
        Ball ball = new Ball(color, row, column);
        this.balls.add(ball);
    }

    public void drawBalls() {
        if (this.balls.size() == 0) {
            this.randomNewBall(3);
        }

        for (Ball ball : this.balls) {
            this.drawBall(ball);
        }
        this.updateBalls();
    }

    public void updateBalls() {
        boolean canBoom = false;
        int temp = 0;

        List<Ball> needToRemoves = new ArrayList<>();

        out: for (Ball ball : this.balls) {
            List<Ball> balls = new ArrayList<>();
            int[] ballPosition = new int[] { ball.getRow(), ball.getColumn() };
            for (int i = 1; i < 5; i++) {
                ballPosition = new int[] { ball.getRow() + i, ball.getColumn() };
                Ball newBall = this.getBallFromGamePosition(ballPosition);

                if (newBall == null) {
                    continue out;
                }
                if (newBall.getColor() == ball.getColor()) {
                    temp++;
                    balls.add(newBall);
                } else {
                    continue out;
                }
                balls.add(ball);
            }
            if (temp >= 5) {
                for (Ball needToRemoveBall : balls) {
                    needToRemoves.add(needToRemoveBall);
                }
            }
        }

        out: for (Ball ball : this.balls) {
            List<Ball> balls = new ArrayList<>();
            int[] ballPosition = new int[] { ball.getRow(), ball.getColumn() };
            for (int i = 1; i < 5; i++) {
                ballPosition = new int[] { ball.getRow(), ball.getColumn() + i };
                Ball newBall = this.getBallFromGamePosition(ballPosition);

                if (newBall == null) {
                    continue out;
                }
                if (newBall.getColor() == ball.getColor()) {
                    temp++;
                    balls.add(newBall);
                } else {
                    continue out;
                }
                balls.add(ball);
            }
            if (temp >= 5) {
                for (Ball needToRemoveBall : balls) {
                    needToRemoves.add(needToRemoveBall);
                }
            }
        }
        for (Ball ball : needToRemoves) {
            this.balls.remove(ball);
        }
    }

    public void randomNewBall(int numbers) {
        for (int i = 0; i < numbers; i++) {
            int row = rm.nextInt(9);
            int column = rm.nextInt(9);
            int colorIndex = rm.nextInt(EnumBallColor.values().length);

            if (this.getBallFromGamePosition(new int[] { row, column }) != null) {
                i--;
                continue;
            }

            this.newBall(EnumBallColor.values()[colorIndex], row, column);
        }
    }

    public void drawBall(Ball ball) {
        int realX = 20;
        int realY = 20;

        switch (ball.getColor()) {
            case BLUE:
                graphics.setColor(new Color(0, 3, 161));
                break;
            case GREEN:
                graphics.setColor(new Color(0, 128, 0));
                break;
            case WHITE:
                graphics.setColor(new Color(255, 255, 255));
                break;
            default:
                graphics.setColor(new Color(0, 0, 0));
                break;
        }

        for (int i = 0; i < ball.getRow(); i++) {
            realX += 60;
        }

        for (int i = 0; i < ball.getColumn(); i++) {
            realY += 60;
        }

        graphics.fillRoundRect(realX, realY, 60, 60, 100, 100);

        if (this.selectedBall == ball) {
            graphics.setColor(new Color(0, 0, 0));
            graphics.drawString("选中", realX + 20, realY + 35);
        }
    }

    /**
     * 通过鼠标坐标取球
     * 
     * @param mouseX
     * @param mouseY
     * @return 坐标位置无球 则返回null
     */
    public Ball getBallFromMousePosition(int mouseX, int mouseY) {
        int[] position = this.getGamePositionByMousePosition(mouseX, mouseY);
        return this.getBallFromGamePosition(position);
    }

    public Ball getBallFromGamePosition(int[] position) {
        for (Ball ball : this.balls) {
            boolean flag = ball.getRow() == position[0] && ball.getColumn() == position[1];
            if (flag) {
                return ball;
            }
        }
        return null;
    }

    /**
     * 根据传入的横纵来判断该位置是否有球
     * 
     * @param row
     * @param column
     * @return
     */
    public boolean isHaveBall(int row, int column) {
        for (Ball ball : this.balls) {
            boolean flag = ball.getRow() == row && ball.getColumn() == column;

            if (flag) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void paint(Graphics g) {
        this.drawBackground(g);
        this.drawBalls();
    }
}
