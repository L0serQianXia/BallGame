package qianxia.ballgame;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

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

    private final List<Ball> balls = new ArrayList<>();
    private final List<Ball> ballsToSpawn = new ArrayList<>();
    private final Random random = new Random();

    public static UI INSTANCE;

    private Ball selectedBall = null;
    private int score = 0;
    private long gameStartTime;

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
                    // game over
                    if (!hasEmpty()) {
                        return;
                    }
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

            // 上一次消除了球不应该继续生成球，下一次移动再生成
            if (!this.boomed && !this.ballsToSpawn.isEmpty()) {
                List<Ball> coolBalls = new ArrayList<>();
                Iterator<Ball> iterator = this.ballsToSpawn.iterator();
                while (iterator.hasNext()) {
                    Ball b = iterator.next();
                    if (this.getBallFromGamePosition(b.getRow(), b.getColumn()) != null) {
                        Ball ball = getRandomBallInfo(b.getColor());
                        coolBalls.add(ball);
                        this.newBall(ball);
                        iterator.remove();
                        continue;
                    }
                    this.newBall(b);
                }
                this.ballsToSpawn.addAll(coolBalls);
                doAnimation(this.ballsToSpawn, 60);
                // 检查是否有没有完全生成的球（是待生成的球的大小）
                for (Ball ball1 : ballsToSpawn) {
                    if (ball1.getAnimWidth() < 60 && ball1.getAnimHeight() < 60) {
                        doAnimation(Collections.singletonList(ball1), 60);
                    }
                }
                this.ballsToSpawn.clear();
            }
            this.boomed = false;
        }

        this.repaint();
    }

    private void checkGraphics() {
        if (graphics == null) {
            System.out.println("graphics == null, let's get it again!");
            graphics = image.getGraphics();
        }
    }

    private void doAnimation(List<Ball> balls, int ballBox) {
        if(balls.isEmpty()) return;
        int index = 0;
        int realX, realY;
        Ball ball = balls.get(index);
        checkGraphics();

        while (ball.getAnimWidth() < ballBox && ball.getAnimHeight() < ballBox) {
            setColor(ball);
            realX = 20 + ball.getRow() * 60;
            realY = 20 + ball.getColumn() * 60;
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

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j <= 80; j++) {
                if (mouseX <= j && row == -1) {
                    row = i;
                }
                if (mouseY <= j && column == -1) {
                    column = i;
                }
            }
            mouseX -= 60;
            mouseY -= 60;
        }
        return new int[] { row, column };
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
        drawBalls(this.ballsToSpawn);
        drawBalls(this.balls);
        doAnimation(this.ballsToSpawn, 30);
        doAnimation(this.balls, 60);
        drawBalls(this.ballsToSpawn);
        drawBalls(this.balls);
        applyImage();
        animating = false;
    }

    private void drawBalls(List<Ball> balls) {
        int realX, realY;
        for (Ball ball : balls) {
            realX = 20 + ball.getRow() * 60;
            realY = 20 + ball.getColumn() * 60;
            setColor(ball);
            graphics.fillRoundRect(realX + 30 - ball.getAnimWidth() / 2, realY + 30 - ball.getAnimHeight() / 2, ball.getAnimWidth(), ball.getAnimHeight(), 100, 100);
        }
        for (Ball ball : balls) {
            if (this.selectedBall == ball && !selectedBall.isMoving()) {
                realX = 20 + ball.getRow() * 60;
                realY = 20 + ball.getColumn() * 60;
                if (ball.getColor() == EnumBallColor.BLUE) {
                    graphics.setColor(new Color(255, 255, 255));
                } else {
                    graphics.setColor(new Color(0, 0, 0));
                }
                graphics.drawString("选中", realX + 20, realY + 35);
                applyImage();
            }
        }
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
            this.drawBalls(this.balls);
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
        return getRandomBallInfo(EnumBallColor.values()[random.nextInt(EnumBallColor.values().length)]);
    }

    private Ball getRandomBallInfo(EnumBallColor color) {
        int row = random.nextInt(9);
        int column = random.nextInt(9);

        while (this.hasEmpty() && this.hasBall(row, column)) {
            row = random.nextInt(9);
            column = random.nextInt(9);
        }
        return new Ball(color, row, column);
    }

    private boolean hasBall(int row, int column) {
        return this.getBallFromGamePosition(row, column) != null || this.ballsToSpawn.contains(new Ball(row, column));
    }

    public boolean hasEmpty() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Ball ball = this.getBallFromGamePosition(new int[] { i, j });
                if (ball == null && !hasBall(i, j)) {
                    return true;
                }
            }
        }
        return false;
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
