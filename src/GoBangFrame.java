import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class GoBangFrame extends JFrame {
    public boolean music = true;
    JCheckBox showSort = new JCheckBox("落子顺序");   //设置为全局变量方便控制
    JCheckBox first = new JCheckBox("先手");
    JButton back = new JButton("悔棋");
    JButton newGame = new JButton("新游戏");
    JButton mus = new JButton("音乐");
    GoBangPanel panel = new GoBangPanel();
    JComboBox deep1 = new JComboBox<Integer>(new Integer[]{1, 3, 5});
    JComboBox pointNum1 = new JComboBox<Integer>(new Integer[]{3, 5, 10});
    JRadioButton intel = new JRadioButton("估值函数");
    JRadioButton intel1 = new JRadioButton("估值函数+搜索树");
    JRadioButton renren = new JRadioButton("人人");
    JRadioButton renji = new JRadioButton("人机");
    JRadioButton shuangji = new JRadioButton("双机");
    JTextArea area = new JTextArea();//文件域
    public GoBangFrame() throws Exception {
        start();
    }
    public void start() {
        //设置主窗口

        //棋盘添加进入左侧 西侧
        this.add(panel, BorderLayout.WEST);

        //建立右侧功能区
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));


        //建立功能区的显示框
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.setBorder(new TitledBorder("在棋盘单击鼠标右键，查看各点估值"));//显示
        area.setEditable(false);
        JScrollPane area2 = new JScrollPane(area);
        panel1.add(area2);
        rightPanel.add(panel1);
        this.add(rightPanel);

        //按Y轴顺序排列
        JPanel optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));
        optPanel.setBorder(new TitledBorder("游戏设置"));

        //建立人人 人机单选框
        JPanel panel2 = new JPanel();//建立框架
        panel2.setBorder(new TitledBorder("模式"));//显示
        renren.setSelected(true);
        ButtonGroup group = new ButtonGroup();//radiogroup  的组类
        group.add(renren);
        group.add(renji);
        group.add(shuangji);
        panel2.add(renren);
        panel2.add(renji);
        panel2.add(shuangji);
        optPanel.add(panel2);//插入框架

        //建立智能等级选择框
        JPanel panel3 = new JPanel();
        panel3.setBorder(new TitledBorder("智能"));//显示
        ButtonGroup group1 = new ButtonGroup();
        intel.setSelected(true);
        group1.add(intel);
        group1.add(intel1);
        panel3.add(intel);
        panel3.add(intel1);
        optPanel.add(panel3);

        //建立搜索树框
        JPanel panel4 = new JPanel();
        panel4.setBorder(new TitledBorder("搜索树"));//显示
        JLabel deep = new JLabel("搜索深度");
        JLabel pointNum = new JLabel("每层节点数");
        panel4.add(deep);
        panel4.add(deep1);
        panel4.add(pointNum);
        panel4.add(pointNum1);
        optPanel.add(panel4);
        JPanel panel6 = new JPanel();
        panel6.add(newGame);
        panel6.add(mus);
        optPanel.add(panel6);
        //建立其他设置框
        JPanel panel5 = new JPanel();
        panel5.setBorder(new TitledBorder("其他"));//显示
        panel5.add(showSort);
        panel5.add(first);
        panel5.add(back);
        optPanel.add(panel5);
        rightPanel.add(optPanel);

        //消息映射事件的添加
        back.addMouseListener(mouseListener);
        newGame.addMouseListener(mouseListener);
        showSort.addMouseListener(mouseListener);
        first.addMouseListener(mouseListener);
        mus.addMouseListener(mouseListener);

        this.setSize(GoBangUtil.GAME_WIDTH, GoBangUtil.GAME_HEIGHT);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);//关闭程序
        this.setTitle("五子棋");//设置标题
        this.setResizable(false);//不可拖拉
        this.setVisible(true);//可视化

    }

    //对悔棋，显示落子顺序，，开始新游戏进行添加按键映射
    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            Object obj = e.getSource();  //获取控件的信息
            boolean renrendo = renren.isSelected() ? true : false;
            boolean renjido = renji.isSelected() ? true : false;
            boolean inteldo = intel.isSelected() ? true : false;
            boolean shuangjido = intel.isSelected() ? true : false;
            boolean firstdo = first.isSelected() ? true : false;
            int deeptmp = (int) deep1.getSelectedItem();
            int pointNum = (int) pointNum1.getSelectedItem();
            if (obj == back) {
                panel.undo(); //悔棋的实现
            }
            if (obj == showSort) {      //落子顺序的实现
                panel.showNum(showSort.isSelected());
            }
            if (obj == newGame) {       //新游戏的实现
                {
                    {
                        try {
                            panel.newGame(showSort.isSelected(), renrendo, inteldo, deeptmp, pointNum, area, shuangjido, renjido,firstdo);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            if (obj == mus) {
                if(music==true)
                {
                    GoBangTest.music.stop();
                }
                else{
                    try {
                        GoBangTest.music=new Music(new File("back0.mid"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    GoBangTest.music.play();
                }
                panel.mu = !panel.mu;
                music=!music;
            }
        }
    };
}

