package qianxia.ballgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @Author: QianXia
 * @Description: 主界面绘制
 * @Date: 2021/01/22-10:07
 */
public class UI extends JFrame {
	private static final long serialVersionUID = 9107930218742074976L;
	private final int WIDTH = 580, HEIGHT = 560;
    public final String BASIC_TITLE = "Fake Magic Lines v1.2";

    public static UI INSTANCE;

    private List<Ball> balls = new ArrayList<>();
    private List<Ball> ballsToSpawn = new ArrayList<>();
    private Ball selectedBall = null;
    private int score = 0;
    private long gameStartTime = -1;

    private Random rm = new Random();
    private Graphics graphics;
	private boolean boomed;
    
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
                    UI.INSTANCE.moveBall(ball, position);
                } else {
                    UI.INSTANCE.selectedBall = ball;
                    UI.INSTANCE.drawBalls();
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

    public void moveBall(Ball ball, int[] position) {
        if (this.selectedBall != null) {
            if (this.getBallFromGamePosition(position) != null) {
                this.selectedBall = null;
                return;
            }

            this.selectedBall.setRow(position[0]);
            this.selectedBall.setColumn(position[1]);
            this.selectedBall = null;
            
        	if(!this.boomed) {
                randomNewBall(3);
        	}
            this.boomed = false;
        	

//            if (!this.ballsToSpawn.isEmpty()) {
//                for (Ball b : this.ballsToSpawn) {
//                    if (this.getBallFromGamePosition(new int[] { b.getRow(), b.getColumn() }) != null) {
//                        this.randomNewBall(1);
//                        continue;
//                    }
//                    this.newBall(b);
//                }
//                this.ballsToSpawn.clear();
//            }
        }

        this.repaint();
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

        for (Ball ball : this.balls) {
            this.drawBall(ball);
        }
    }

    public void updateBalls() {
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
                    balls.clear();
                    continue out;
                }
                balls.add(ball);
            }
            if (temp >= 5) {
                for (Ball needToRemoveBall : balls) {
                   	if(needToRemoves.contains(needToRemoveBall)) {
                		continue;
                	}
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
                    balls.clear();
                    continue out;
                }
                balls.add(ball);
            }
            if (temp >= 5) {
                for (Ball needToRemoveBall : balls) {
                   	if(needToRemoves.contains(needToRemoveBall)) {
                		continue;
                	}
                    needToRemoves.add(needToRemoveBall);
                }
            }
        }

        out: for (Ball ball : this.balls) {
            List<Ball> balls = new ArrayList<>();
            int[] ballPosition = new int[] { ball.getRow(), ball.getColumn() };
            for (int i = 1; i < 5; i++) {
                ballPosition = new int[] { ball.getRow() + i, ball.getColumn() + i };
                Ball newBall = this.getBallFromGamePosition(ballPosition);

                if (newBall == null) {
                    continue out;
                }
                if (newBall.getColor() == ball.getColor()) {
                    temp++;
                    balls.add(newBall);
                } else {
                    balls.clear();
                    continue out;
                }
                balls.add(ball);
            }
            if (temp >= 5) {
                for (Ball needToRemoveBall : balls) {
                   	if(needToRemoves.contains(needToRemoveBall)) {
                		continue;
                	}
                    needToRemoves.add(needToRemoveBall);
                }
            }
        }

        out: for (Ball ball : this.balls) {
            List<Ball> balls = new ArrayList<>();
            int[] ballPosition = new int[] { ball.getRow(), ball.getColumn() };
            for (int i = 1; i < 5; i++) {
                ballPosition = new int[] { ball.getRow() - i, ball.getColumn() + i };
                Ball newBall = this.getBallFromGamePosition(ballPosition);

                if (newBall == null) {
                    continue out;
                }
                if (newBall.getColor() == ball.getColor()) {
                    temp++;
                    balls.add(newBall);
                } else {
                    balls.clear();
                    continue out;
                }
                balls.add(ball);
            }
            if (temp >= 5) {
                for (Ball needToRemoveBall : balls) {
                	if(needToRemoves.contains(needToRemoveBall)) {
                		continue;
                	}
                    needToRemoves.add(needToRemoveBall);
                }
            }
        }
        
        if (needToRemoves.size() != 0) {
            for (Ball ball : needToRemoves) {
                this.balls.remove(ball);
            }
            
            this.score += (needToRemoves.size() - 4) * 5;
            this.boomed = true;
        }

        if (!this.hasEmpty()) {
            JOptionPane.showMessageDialog(null, "得分：" + this.score + "\n用时：" + this.getTime(), "游戏结束，按下鼠标滚轮重置游戏",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {

            
            
//            if (!this.ballsToSpawn.isEmpty()) {
//                this.ballsToSpawn.clear();
//            }
//            for (int i = 0; i < 3; i++) {
//                int row = rm.nextInt(9);
//                int column = rm.nextInt(9);
//                int colorIndex = rm.nextInt(EnumBallColor.values().length);
//
//                if (this.hasEmpty()) {
//                    if (this.getBallFromGamePosition(new int[] { row, column }) != null) {
//                        i--;
//                        continue;
//                    }
//                } else {
//                    return;
//                }
//                
//                EnumBallColor color = EnumBallColor.values()[colorIndex];
//                Ball ball = new Ball(color, row, column);
//                this.ballsToSpawn.add(ball);
  //          }
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
            int row = rm.nextInt(9);
            int column = rm.nextInt(9);
            int colorIndex = rm.nextInt(EnumBallColor.values().length);

            if (this.hasEmpty()) {
                if (this.getBallFromGamePosition(new int[] { row, column }) != null) {
                    i--;
                    continue;
                }
            } else {
                return;
            }

            this.newBall(EnumBallColor.values()[colorIndex], row, column);
        }
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

        graphics.fillRoundRect(realX, realY, 60, 60, 100, 100);
        //graphics.fillArc(realX, realY, 60, 60, 220, 300);
        
        if (this.selectedBall == ball) {
        	if(ball.getColor() == EnumBallColor.BLUE) {
                graphics.setColor(new Color(255, 255, 255));
        	}else {
                graphics.setColor(new Color(0, 0, 0));
        	}
            graphics.drawString("选中", realX + 20, realY + 35);
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
        update(this.getGraphics());
    }

    /**
     * 修复闪屏需要的 
     * 这里查了下CSDN 
     * 说是update方法清屏会导致闪屏 
     * 重写一下这个方法让他什么也不做就可以了
     */
    @Override
    public void update(Graphics g) {
    }

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
