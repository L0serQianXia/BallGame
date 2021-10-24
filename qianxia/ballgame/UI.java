package qianxia.ballgame;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;

/**
 * @Author: QianXia
 * @Description: 主界面绘制
 * @Date: 2021/01/22-10:07
 */
public class UI extends JFrame {
	private static final long serialVersionUID = 9107930218742074976L;
	public final int WIDTH = 580, HEIGHT = 560;
    public final String BASIC_TITLE = "Fake Magic Lines v1.3";

    public static UI INSTANCE;

    private List<Ball> balls = new ArrayList<>();
    private List<Ball> ballsToSpawn = new ArrayList<>();
    private Ball selectedBall = null;
    private int score = 0;
    private long gameStartTime = -1;

    private Random rm = new Random();
    private Graphics graphics;
	private boolean boomed;
    private boolean animating;

    public UI() {
        UI.INSTANCE = this;

        this.setTitle(BASIC_TITLE);
        this.setResizable(false);
        this.setBounds(0, 0, WIDTH, HEIGHT);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                if (animating) {
                    return;
                }

                if (e.getButton() == 2) {
                    int result = JOptionPane.showConfirmDialog(null, "确定重置游戏？", "提示：", JOptionPane.YES_NO_OPTION);

                    if (result == 0) {
                        UI.INSTANCE.resetGame();
                    }
                    return;
                }

                Ball ball = UI.INSTANCE.getBallFromMousePosition(mouseX, mouseY);
                int[] position = UI.INSTANCE.getGamePositionByMousePosition(mouseX, mouseY);

                if (UI.INSTANCE.selectedBall == ball) {
                    UI.INSTANCE.selectedBall = null;
                    UI.INSTANCE.drawBalls();
                    return;
                }

                if (UI.INSTANCE.selectedBall != null && ball == null) {
                    UI.INSTANCE.moveSelectedBall(position);
                } else {
                    UI.INSTANCE.selectedBall = ball;
                    UI.INSTANCE.drawBalls();
                    applyImage();
                } 
            }
        });

        this.gameStartTime = System.currentTimeMillis();
        this.graphics = this.getGraphics();
        (new UpdateScore()).start();
    }

    public void resetGame() {
        this.score = 0;
        this.selectedBall = null;
        this.balls.clear();
        this.repaint();
        this.gameStartTime = System.currentTimeMillis();
    }

    public void moveSelectedBall(int[] position) {
        if (this.selectedBall != null) {
            if (this.getBallFromGamePosition(position) != null) {
                this.selectedBall = null;
                return;
            }

            List<FindPathUtils.Node> path = FindPathUtils.findPath(new int[]{selectedBall.getRow(), selectedBall.getColumn()}, position);
            // path == null 说明这个位置无法到达，抖动表示错误的位置移动
            if (path == null) {
                shakeTheWindow();
                return;
            }

            selectedBall.setMoving(true);
            for (FindPathUtils.Node node : path) {
                this.selectedBall.setRow(node.x);
                this.selectedBall.setColumn(node.y);
                paint(getGraphics());
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            selectedBall.setMoving(false);
            paint(getGraphics());
            this.selectedBall = null;

            if (!this.boomed) {
                if (!this.ballsToSpawn.isEmpty()) {
                    for (Ball b : this.ballsToSpawn) {
                        if (this.getBallFromGamePosition(new int[] { b.getRow(), b.getColumn() }) != null) {
                            this.randomNewBall(1);
                            continue;
                        }
                        this.newBall(b);
                    }
                    int index = 0;
                    Ball ball = this.ballsToSpawn.get(index);
                    if (graphics == null) {
                        System.out.println("graphics == null, let's get it again!");
                        graphics = image.getGraphics();
                    }
                    int realX = 20;
                    int realY = 20;
                    setColor(ball);
                    for (int i = 0; i < ball.getRow(); i++) {
                        realX += 60;
                    }
                    for (int i = 0; i < ball.getColumn(); i++) {
                        realY += 60;
                    }
                    while (ball.getAnimWidth() < 60 && ball.getAnimHeight() < 60) {
                        realX = 20;
                        realY = 20;
                        setColor(ball);
                        for (int i = 0; i < ball.getRow(); i++) {
                            realX += 60;
                        }
                        for (int i = 0; i < ball.getColumn(); i++) {
                            realY += 60;
                        }
                        graphics.fillRoundRect(realX + 30 - ball.getAnimWidth() / 2, realY + 30 - ball.getAnimHeight() / 2, ball.getAndAddAnimWidth(), ball.getAndAddAnimHeight(), 100, 100);
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ball = ballsToSpawn.get(index++);
                        if (index >= ballsToSpawn.size()) {
                            index = 0;
                        }
                        applyImage();
                    }
                    this.ballsToSpawn.clear();
                }
            }
            this.boomed = false;
        }

        this.repaint();
    }

    private void shakeTheWindow() {
        int x = this.getX();
        int y = this.getY();
        for (int i = 1; i < 10; i++) {
            this.setLocation(this.getX() - new Random().nextInt(20), this.getY() + new Random().nextInt(20));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.setLocation(x, y);
        }
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
        this.newBall(ball);
    }

    public void newBall(Ball ball) {
        this.balls.add(ball);
    }

    public void drawBalls() {
        if (this.balls.size() == 0) {
            this.randomNewBall(3);
        }

        this.updateBalls();

        animating = true;
        int index = 0;
        Ball ball = this.balls.get(index);
        if (graphics == null) {
            System.out.println("graphics == null, let's get it again!");
            graphics = image.getGraphics();
        }
        int realX = 20;
        int realY = 20;
        setColor(ball);
        for (int i = 0; i < ball.getRow(); i++) {
            realX += 60;
        }
        for (int i = 0; i < ball.getColumn(); i++) {
            realY += 60;
        }
        while (ball.getAnimWidth() < 60 && ball.getAnimHeight() < 60) {
            realX = 20;
            realY = 20;
            setColor(ball);
            for (int i = 0; i < ball.getRow(); i++) {
                realX += 60;
            }
            for (int i = 0; i < ball.getColumn(); i++) {
                realY += 60;
            }
            graphics.fillRoundRect(realX + 30 - ball.getAnimWidth() / 2, realY + 30 - ball.getAnimHeight() / 2, ball.getAndAddAnimWidth(), ball.getAndAddAnimHeight(), 100, 100);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ball = balls.get(index++);
            if (index >= balls.size()) {
                index = 0;
            }
            applyImage();
        }
        for (Ball ball1 : balls) {
            realX = 20;
            realY = 20;
            setColor(ball1);
            for (int i = 0; i < ball1.getRow(); i++) {
                realX += 60;
            }
            for (int i = 0; i < ball1.getColumn(); i++) {
                realY += 60;
            }
            graphics.fillRoundRect(realX + 30 - ball1.getAnimWidth() / 2, realY + 30 - ball1.getAnimHeight() / 2, ball1.getAnimWidth(), ball1.getAnimHeight(), 100, 100);
        }
        for (Ball ball1 : balls) {
            if (this.selectedBall == ball1 && !selectedBall.isMoving()) {
                realX = 20;
                realY = 20;
                setColor(ball1);
                for (int i = 0; i < ball1.getRow(); i++) {
                    realX += 60;
                }
                for (int i = 0; i < ball1.getColumn(); i++) {
                    realY += 60;
                }
                if (ball1.getColor() == EnumBallColor.BLUE) {
                    graphics.setColor(new Color(255, 255, 255));
                } else {
                    graphics.setColor(new Color(0, 0, 0));
                }
                graphics.drawString("选中", realX + 20, realY + 35);
                applyImage();
            }
        }


        index = 0;
        ball = this.ballsToSpawn.get(index);
        realX = 20;
        realY = 20;
        setColor(ball);
        for (int i = 0; i < ball.getRow(); i++) {
            realX += 60;
        }
        for (int i = 0; i < ball.getColumn(); i++) {
            realY += 60;
        }
        while (ball.getAnimWidth() < 30 && ball.getAnimHeight() < 30) {
            realX = 20;
            realY = 20;
            setColor(ball);
            for (int i = 0; i < ball.getRow(); i++) {
                realX += 60;
            }
            for (int i = 0; i < ball.getColumn(); i++) {
                realY += 60;
            }
            graphics.fillRoundRect(realX + 30 - ball.getAnimWidth() / 2, realY + 30 - ball.getAnimHeight() / 2, ball.getAndAddAnimWidth(), ball.getAndAddAnimHeight(), 100, 100);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ball = ballsToSpawn.get(index++);
            if (index >= ballsToSpawn.size()) {
                index = 0;
            }
            applyImage();
        }
        for (Ball ball1 : ballsToSpawn) {
            realX = 20;
            realY = 20;
            setColor(ball1);
            for (int i = 0; i < ball1.getRow(); i++) {
                realX += 60;
            }
            for (int i = 0; i < ball1.getColumn(); i++) {
                realY += 60;
            }
            graphics.fillRoundRect(realX + 30 - ball1.getAnimWidth() / 2, realY + 30 - ball1.getAnimHeight() / 2, ball1.getAnimWidth(), ball1.getAnimHeight(), 100, 100);
        }
        applyImage();
        animating = false;
//        for (Ball ball1 : this.balls) {
//            this.drawBall(ball1, false);
//        }
    }

    private void setColor(Ball ball) {
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
            case RED:
                graphics.setColor(new Color(205, 92, 92));
                break;
            case CYAN:
                graphics.setColor(new Color(0, 139, 139));
                break;
            default:
                graphics.setColor(new Color(0, 0, 0));
                break;
        }
    }

    private List<Ball> checkBalls(List<Ball> needToRemoves, int rowOffset, int columnOffset) {
        out: for (Ball ball : this.balls) {
            List<Ball> balls = new ArrayList<>();
            int[] ballPosition;
            for (int i = 1; i < 5; i++) {
                ballPosition = new int[] { ball.getRow() + rowOffset * i, ball.getColumn() + columnOffset * i };
                Ball newBall = this.getBallFromGamePosition(ballPosition);

                if (newBall == null) {
                    continue out;
                }
                if (newBall.getColor() == ball.getColor()) {
                    balls.add(newBall);
                } else {
                    balls.clear();
                    continue out;
                }
                balls.add(ball);
            }
            if (balls.size() >= 5) {
                for (Ball needToRemoveBall : balls) {
                    if(needToRemoves.contains(needToRemoveBall)) {
                        continue;
                    }
                    needToRemoves.add(needToRemoveBall);
                }
            }
        }
        return needToRemoves;
    }

    public void updateBalls() {
        if (selectedBall != null && selectedBall.isMoving()) {
            return;
        }

        List<Ball> needToRemoves = new ArrayList<>();
        checkBalls(needToRemoves,1, 0);
        checkBalls(needToRemoves,0, 1);
        checkBalls(needToRemoves, 1, 1);
        checkBalls(needToRemoves,-1, 1);

        if (needToRemoves.size() != 0) {
            for (Ball ball : needToRemoves) {
                this.balls.remove(ball);
            }
            
            this.score += 5 + (needToRemoves.size() - 5) * 10;
            this.boomed = true;
        }

        if (!this.hasEmpty()) {
            for (Ball ball : balls) {
                this.drawBall(ball);
            }
            JOptionPane.showMessageDialog(null, "得分：" + this.score + "\n用时：" + this.getTime(), "游戏结束，按下鼠标滚轮重置游戏",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            if (!ballsToSpawn.isEmpty()) {
                return;
            }
            for (int i = 0; i < 3; i++) {
                this.ballsToSpawn.add(getRandomBallInfo());
            }
        }
    }

    public String getTime() {
        long nowTime = System.currentTimeMillis();

        int seconds = (int) (nowTime - this.gameStartTime) / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        if (minutes == 0) {
            return seconds + "秒";
        }

        seconds -= minutes * 60;

        if (hours == 0) {
            return minutes + "分" + seconds + "秒";
        }

        minutes -= hours * 60;

        return hours + "小时" + minutes + "分" + seconds + "秒";
    }

    public void randomNewBall(int numbers) {
        for (int i = 0; i < numbers; i++) {
            Ball ball = getRandomBallInfo();
            this.newBall(ball);
        }
    }

    private Ball getRandomBallInfo() {
        return getRandomBallInfo(rm.nextInt(EnumBallColor.values().length));
    }

    private Ball getRandomBallInfo(int colorIndex) {
        int row = rm.nextInt(9);
        int column = rm.nextInt(9);

        while (this.hasEmpty() && this.getBallFromGamePosition(new int[] { row, column }) != null) {
            row = rm.nextInt(9);
            column = rm.nextInt(9);
        }
        return new Ball(EnumBallColor.values()[colorIndex], row, column);
    }

    public boolean hasEmpty() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Ball ball = this.getBallFromGamePosition(new int[] { i, j });
                if (ball == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public void drawBall(Ball ball) {
        drawBall(ball, false);
    }

    public void drawBall(Ball ball, boolean notSpawned) {
        if (graphics == null) {
            System.out.println("graphics == null, let's get it again!");
            graphics = this.getGraphics();
        }
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
            case RED:
                graphics.setColor(new Color(205, 92, 92));
                break;
            case CYAN:
                graphics.setColor(new Color(0, 139, 139));
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

        if (notSpawned) {
            graphics.fillRoundRect(realX + 15, realY + 15, 30, 30, 100, 100);
        } else {
            graphics.fillRoundRect(realX, realY, 60, 60, 100, 100);
        }

        if (this.selectedBall == ball && !selectedBall.isMoving()) {
            if (ball.getColor() == EnumBallColor.BLUE) {
                graphics.setColor(new Color(255, 255, 255));
            } else {
                graphics.setColor(new Color(0, 0, 0));
            }
            graphics.drawString("选中", realX + 20, realY + 35);
            applyImage();
        }
    }

    /**
     * 通过鼠标坐标取球
     * 
     * @param mouseX
     * @param mouseY
     * 
     * @return 坐标位置无球 则返回null
     */
    public Ball getBallFromMousePosition(int mouseX, int mouseY) {
        int[] position = this.getGamePositionByMousePosition(mouseX, mouseY);
        return this.getBallFromGamePosition(position);
    }

    public Ball getBallFromGamePosition(int[] position) {
        return getBallFromGamePosition(position[0], position[1]);
    }

    public Ball getBallFromGamePosition(int x, int y) {
        for (Ball ball : this.balls) {
            boolean flag = ball.getRow() == x && ball.getColumn() == y;
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

    Image image;

    @Override
    public void paint(Graphics g) {
        doubleBuffer();
        this.drawBackground(graphics);
        this.drawBalls();
        applyImage();
    }

    private void applyImage() {
        getGraphics().drawImage(image, 0, 0, this);
    }

    private void doubleBuffer() {
        image = createImage(this.getWidth(), this.getHeight());
        graphics = image.getGraphics();
    }

    @Override
    public void update(Graphics g) { }

    class UpdateScore extends Thread {
        @Override
        public void run() {
            while (true) {
                UI.INSTANCE.setTitle(BASIC_TITLE + " - Score:" + UI.INSTANCE.score);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
