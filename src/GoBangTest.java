import javax.swing.*;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
public class GoBangTest {//程序的主入口
    public static GoBangFrame frame;
    public static Music music;
    public static void main(String[] args) throws Exception {
        Thread threadOne = new Thread(new Runnable() {//使用双线程执行主框架与计时功能
            public void run() {
                try {
                    methodOne();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread threadTwo = new Thread(new Runnable() {
            public void run() {
                methodTwo();
            }
        });

        music=new Music(new File("back0.mid"));
        music.play();

        // 执行线程
        threadOne.start();
        threadOne.join();
        threadTwo.start();
    }


    public static void methodOne() throws Exception {
        frame = new GoBangFrame();
    }

    public static void methodTwo() {//计时功能
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (frame.panel.renrenDo == true)
                    if (frame.panel.isGameOver == false) {
                        frame.panel.time--;
                        frame.panel.repaint();
                        if (frame.panel.time == 0) {
                            frame.panel.isGameOver = true;
                            if (frame.panel.renrenDo == true) {
                                if (frame.panel.Player == 0)
                                    JOptionPane.showMessageDialog(frame, "下棋超时，黑棋获胜");
                                else JOptionPane.showMessageDialog(frame, "下棋超时，白棋获胜");
                            }
                            if (frame.panel.renjido == true) {
                                JOptionPane.showMessageDialog(frame, "电脑获胜");
                            }
                        }
                    }
            }
        }, 1000, 1000);


    }
}

