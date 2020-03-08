import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import java.lang.Math;

//repaint（）是重画函数
public class GoBangPanel extends JPanel {//创建左侧棋盘控件
    public static final int EMPTY = 0;//代表数组颜色为空
    private final int BLACK = 1;//代表黑色
    private final int WHITE = 2;//代表白色
    public int time = 20;
    ChessBean[][] chessbean = new ChessBean[GoBangUtil.LINE_COUNT][GoBangUtil.LINE_COUNT];//初始化数组
    public int Player = 1;//1代表黑子，2代表白子
    private int Count = 1;//棋子的计数器
    private boolean showNum = true; //是否显示数字
    private boolean first = true;//人机对战先手顺序
    private int cx = GoBangUtil.LINE_COUNT / 2;//预选框x行数
    private int cy = GoBangUtil.LINE_COUNT / 2;//预选框y行数
    public boolean isGameOver = true;//判断游戏是否结束
    public boolean renrenDo = true;//判断人人
    public boolean renjido = true;//判断人机
    private boolean intel = true;//是估值函数还是估值函数加搜索树
    public boolean mu = true; //控制音乐
    public boolean shuangjido = true;//判断是否为双机对战
    public boolean treetag = false;
    private int deep = 0;
    private int pointNum = 0;
    private JTextArea area;
    private ChessBean chessBeanForTree = new ChessBean(0, 0, 0, 0);
    private int gx = GoBangUtil.LINE_COUNT / 2;//双机x
    private int gy = GoBangUtil.LINE_COUNT / 2;//双机y
    final int MAX = 99999;

    public GoBangPanel() {

        this.setPreferredSize(new Dimension(650, 700));//棋盘宽度设置
        this.setBackground(Color.ORANGE);//设置背景颜色
        this.addMouseMotionListener(mouseMotionListener);//鼠标移动映射
        this.addMouseListener(mouseListener);//鼠标点击映射
        for (int i = 0; i < GoBangUtil.LINE_COUNT; i++)
            for (int j = 0; j < GoBangUtil.LINE_COUNT; j++) {
                chessbean[i][j] = new ChessBean(i, j, 0, EMPTY);
            }
    }


    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            int x = e.getX();
            int y = e.getY();
            cx = (x - GoBangUtil.OFFSET / 2) / GoBangUtil.LINE_WIDTH;
            cy = (y - GoBangUtil.OFFSET / 2) / GoBangUtil.LINE_WIDTH;
            if (cx >= 0 && cx < GoBangUtil.LINE_COUNT && cy >= 0 && cy < GoBangUtil.LINE_COUNT)
                repaint();
        }
    };
    private final MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {//鼠标左键
                if (isGameOver) { //游戏没有开始的判断句
                    JOptionPane.showMessageDialog(GoBangPanel.this, "请开始新游戏");
                } else {
                    Music music = null;
                    try {
                        music = new Music(new File("022.wav"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    int x = e.getX();
                    int y = e.getY();
                    x = (x - GoBangUtil.OFFSET / 2) / GoBangUtil.LINE_WIDTH;
                    y = (y - GoBangUtil.OFFSET / 2) / GoBangUtil.LINE_WIDTH;
                    if (x >= 0 && x < GoBangUtil.LINE_COUNT && y >= 0 && y < GoBangUtil.LINE_COUNT)
                        if (renrenDo) {//人人对战
                            super.mouseClicked(e);
                            {
                                gx = x;
                                gy = y;
                                if (chessbean[x][y].isEmpty()) {
                                    if (mu == true)
                                        music.play();
                                    chessbean[x][y] = new ChessBean(x, y, Count, Player);   //单击设置数组的属性  重画出棋子
                                    checkWin(x, y, chessbean[x][y].getColor());
                                    Count++;
                                    Player = 3 - Player;
                                    List<ChessBean> list = getSortList(chessbean[x][y].getColor(), chessbean);
                                    time = 20;

                                }
                            }
                            if (Count == GoBangUtil.LINE_COUNT * GoBangUtil.LINE_COUNT + 1 && isGameOver == false)
                                JOptionPane.showMessageDialog(GoBangPanel.this, "平局");
                        } else if (renjido) {//人机对战
                            if (intel)//估值函数
                            {
                                gx = x;
                                gy = y;
                                if (chessbean[x][y].isEmpty()) {
                                    if (mu == true)
                                        music.play();
                                    if (first) {
                                        chessbean[x][y] = new ChessBean(x, y, Count, BLACK);
                                        Player = BLACK;
                                    } else {
                                        chessbean[x][y] = new ChessBean(x, y, Count, WHITE);
                                        Player = WHITE;
                                    }
                                    Count++;
                                    repaint();
                                    if (checkWin(x, y, chessbean[x][y].getColor())) {
                                        if (mu == true)
                                            music.play();
                                        List<ChessBean> list = getSortList(3 - Player, chessbean);
                                        ChessBean Bean = list.get(0);
                                        x = Bean.getX();
                                        y = Bean.getY();
                                        Bean.setOrderNumber(Count);
                                        if (first) {
                                            Bean.setColor(WHITE);
                                            Player = WHITE;
                                        } else {
                                            Bean.setColor(BLACK);
                                            Player = BLACK;
                                        }
                                        chessbean[x][y] = Bean;
                                        Count++;
                                        List<ChessBean> list1 = getSortList(chessbean[x][y].getColor(), chessbean);
                                        checkWin(x, y, Player);
                                        if (Count == GoBangUtil.LINE_COUNT * GoBangUtil.LINE_COUNT + 1 && isGameOver == false)
                                            JOptionPane.showMessageDialog(GoBangPanel.this, "平局");
                                    }
                                    time = 20;
                                }
                            } else {//估值函数加搜索树
                                if (chessbean[x][y].isEmpty()) {
                                    if (mu == true)
                                        music.play();
                                    if (first) {
                                        chessbean[x][y] = new ChessBean(x, y, Count, BLACK);
                                        Player = BLACK;
                                    } else {
                                        chessbean[x][y] = new ChessBean(x, y, Count, WHITE);
                                        Player = WHITE;
                                    }
                                    Count++;
                                    repaint();
                                    if (checkWin(x, y, chessbean[x][y].getColor())) {
                                        if (mu == true)
                                            music.play();
                                        List<ChessBean> list2 = getSortList(3 - Player, chessbean);
                                        if (Count < 5) {
                                            Random random = new Random();
                                            boolean flag = true;
                                            while (flag) {
                                                int i1 = random.nextInt(5) + 5;
                                                int i2 = random.nextInt(5) + 5;
                                                if (chessbean[i1][i2].isEmpty()) {
                                                    chessBeanForTree.setX(i1);
                                                    chessBeanForTree.setY(i2);
                                                    chessBeanForTree.setColor(BLACK);
                                                    flag = false;
                                                }
                                            }
                                        } else {
                                            getTree(0, 3 - Player, chessbean, -Integer.MAX_VALUE, Integer.MAX_VALUE, false);
                                            treetag = false;
                                        }
                                        ChessBean Bean = chessBeanForTree;
                                        x = Bean.getX();
                                        y = Bean.getY();
                                        Bean.setOrderNumber(Count);
                                        if (first) {
                                            Bean.setColor(WHITE);
                                            Player = WHITE;
                                        } else {
                                            Bean.setColor(BLACK);
                                            Player = BLACK;
                                        }
                                        chessbean[x][y] = Bean;
                                        Count++;
                                        List<ChessBean> list1 = getSortList(Player, chessbean);
                                        checkWin(x, y, Player);
                                        if (Count == GoBangUtil.LINE_COUNT * GoBangUtil.LINE_COUNT + 1 && isGameOver == false)
                                            JOptionPane.showMessageDialog(GoBangPanel.this, "平局");
                                    }
                                    time = 20;
                                }
                            }
                        }
                }
                repaint();
            } else if (e.getButton() == MouseEvent.BUTTON3) {//鼠标右键
                int x = e.getX();
                int y = e.getY();
                x = (x - GoBangUtil.OFFSET / 2) / GoBangUtil.LINE_WIDTH;
                y = (y - GoBangUtil.OFFSET / 2) / GoBangUtil.LINE_WIDTH;
                System.out.println(chessbean[x][y].getBuffer());
                area.append(chessbean[x][y].getBuffer());
            }
        }
    };

    private int getTree(int d, int player, ChessBean[][] chessbean, int alpha, int beta, boolean flag) { //搜索树
        ChessBean[][] tmp = clone(chessbean);
        List<ChessBean> list = getSortList(player, chessbean);
        chessBeanForTree = new ChessBean(0, 0, 0, 0);
        chessBeanForTree = list.get(0);
        if (d == deep) {
            return list.get(0).getSumScore(); //如果搜索到最大深度，输出估值最高的点
        }
        for (int i = 0; i < pointNum; i++) {
            ChessBean bean = list.get(i);
            int score;
            if ((bean.getX() >= 4 && bean.getY() >= 4 && bean.getX() <= 10 && bean.getY() <= 10) || Count > 50 || flag) {
                {
                    if (bean.getDefScore() >= Level.CON_5.score) {
                        score = MAX; //找不到必赢的点，寻找自己必赢的点
                        chessBeanForTree = bean;
                        treetag = true;
                        return MAX;
                    } else if (bean.getAtkScore() >= Level.CON_5.score) {
                        score = MAX / 2; //找不到必赢的点，则找阻止对方必赢的点
                        chessBeanForTree = bean;
                        treetag = true;
                        return MAX / 2;
                    } else if (bean.getDefScore() >= Level.ALIVE_4.score) {
                        score = MAX / 3; //找不到必赢的点，寻找自己必赢的点
                        chessBeanForTree = bean;
                        treetag = true;
                        return MAX / 3;
                    } else if (bean.getAtkScore() >= Level.ALIVE_4.score) {
                        score = MAX / 4; //找不到必赢的点，则找阻止对方必赢的点
                        chessBeanForTree = bean;
                        treetag = true;
                        return MAX / 4;
                    } else if (bean.getSumScore() >= Level.ALIVE_3.score) {
                        score = bean.getSumScore(); //若都找不到，则获取总分大于活三的点
                    } else {
                        //模拟下棋，建立新得模拟棋盘
                        tmp[bean.getX()][bean.getY()].setColor(player);
                        score = getTree(d + 1, 3 - player, tmp, alpha, beta, true);
                    }
                }
                if (d % 2 == 0) {
                    //对自己找最大值
                    if (score > alpha) {
                        alpha = score;
                        if (d == 0) {
                            chessBeanForTree = bean;
                        }
                    }
                    if (alpha >= beta) {
                        //剪枝
                        score = alpha;
                        return score;
                    }
                } else {
                    if (score < beta) {
                        beta = score;
                        if (d == 0) {
                            chessBeanForTree = bean;
                        }
                    }
                    if (alpha >= beta) {
                        //剪枝
                        score = beta;
                        return score;
                    }
                    if (treetag == true) return score;
                }
            } else {
                score = bean.getSumScore();
                treetag = true;
                return score;
            }
        }
        return d % 2 == 0 ? alpha : beta;
    }

    private ChessBean[][] clone(ChessBean[][] chessbean) {
        ChessBean[][] tmp = new ChessBean[GoBangUtil.LINE_COUNT][GoBangUtil.LINE_COUNT];
        for (int i = 0; i < GoBangUtil.LINE_COUNT; i++)
            for (int j = 0; j < GoBangUtil.LINE_COUNT; j++) {
                tmp[i][j] = new ChessBean(chessbean[i][j].getX(), chessbean[i][j].getY(), chessbean[i][j].getOrderNumber(), chessbean[i][j].getColor());//克隆棋子
            }
        return tmp;
    }

    private List<ChessBean> getSortList(int Player, ChessBean[][] tmp) {
        List<ChessBean> list = new ArrayList<>();
        for (ChessBean[] chessBeans2 : tmp)
            for (ChessBean chessBean : chessBeans2) {
                //找到空点
                if (chessBean.isEmpty()) {
                    chessBean.clearDetail();
                    chessBean.append("================================\n");
                    int a = getValue(chessBean.getX(), chessBean.getY(), 3 - Player);//获取空点价值
                    chessBean.append("\n");
                    int d = getValue(chessBean.getX(), chessBean.getY(), Player);
//                    if (d >= Level.CON_5.score)
//                        d = d * 10;
//                    else if (a >= Level.CON_5.score)
//                        a = a * 5;
//                    else if (d >= Level.ALIVE_4.score)
//                        d = d * 10;
                    chessBean.append("\n");
                    chessBean.setAtkScore(a);
                    chessBean.setDefScore(d);
                    chessBean.append("【" + (char) (chessBean.getX() + 65) + (GoBangUtil.LINE_COUNT - chessBean.getY()) + "】" + "    " + "进攻分：" + chessBean.getAtkScore() + "    " + "防守分：" + chessBean.getDefScore() + '\n');
                    chessBean.setSumScore((int) (a + d * 1.2));
                    list.add(chessBean);
                }
            }
        Collections.sort(list);
        return list;
    }

    private int getValue(int x, int y, int player) {//获得空点的价值
        chessbean[x][y].append("-");
        Level level1 = getLevel(x, y, Direction.HENG, player);
        chessbean[x][y].append("|");
        Level level2 = getLevel(x, y, Direction.SHU, player);
        chessbean[x][y].append("/");
        Level level3 = getLevel(x, y, Direction.PIE, player);
        chessbean[x][y].append("\\");
        Level level4 = getLevel(x, y, Direction.NA, player);
        return getLevelScore(level1, level2, level3, level4) + position[x][y];
    }

    private int getLevelScore(Level level1, Level level2, Level level3, Level level4) {
        int levelCount[] = new int[Level.values().length];
        for (int i = 0; i < Level.values().length; i++)
            levelCount[i] = 0;
        levelCount[level1.index]++;//对应棋形的索引增加
        levelCount[level2.index]++;
        levelCount[level3.index]++;
        levelCount[level4.index]++;
        int score = 0;
        if (levelCount[Level.GO_4.index] >= 2 || (levelCount[Level.GO_4.index] >= 1
                && levelCount[Level.ALIVE_3.index] >= 1))// 双活4，冲4活三
            score = 10000;
        else if (levelCount[Level.ALIVE_3.index] >= 2)// 双活3
            score = 4000;
        else if ((levelCount[Level.SLEEP_3.index] >= 1
                && levelCount[Level.ALIVE_3.index] >= 1) || ((levelCount[Level.GO_4.index] >= 1
                && levelCount[Level.ALIVE_3.index] >= 1)))// 活3眠3,冲四活二
            score = 3000;
        else if (levelCount[Level.ALIVE_2.index] >= 2)// 双活2
            score = 100;
        else if (levelCount[Level.SLEEP_2.index] >= 1
                && levelCount[Level.ALIVE_2.index] >= 1)// 活2眠2
            score = 10;
        return Math.max(score, Math.max(Math.max(level1.score, level2.score), Math.max(level3.score, level4.score)));
    }

    private Level getLevel(int x, int y, Direction dir, int player) {
        String left = "";
        String right = "";
        if (dir == Direction.HENG) {
            left = getStringSeq(x, y, -1, 0, player);
            right = getStringSeq(x, y, 1, 0, player);
        } else if (dir == Direction.SHU) {
            left = getStringSeq(x, y, 0, -1, player);
            right = getStringSeq(x, y, 0, 1, player);
        } else if (dir == Direction.PIE) {
            left = getStringSeq(x, y, -1, 1, player);
            right = getStringSeq(x, y, 1, -1, player);
        } else if (dir == Direction.NA) {
            left = getStringSeq(x, y, -1, -1, player);
            right = getStringSeq(x, y, 1, 1, player);
        }
        String str = left + player + right;//获取棋形中的字符串
        String rstr = new StringBuffer(str).reverse().toString();//获取倒置棋形
        chessbean[x][y].append('\t' + str + '\t');
        //根据棋形匹配level中的棋形获得分值
        for (Level level : Level.values()) {
            Pattern pattern = Pattern.compile(level.regex[player - 1]);
            Matcher matcher = pattern.matcher(str);
            boolean r1 = matcher.find();
            matcher = pattern.matcher(rstr);
            boolean r2 = matcher.find();
            if (r1 || r2) {
                chessbean[x][y].append(level.name).append("\n");
                return level;
            }
        }
        return Level.NULL;
    }

    private String getStringSeq(int x, int y, int dx, int dy, int player) {
        String str = "";
        boolean res = false;//判断需要倒置的四条线
        if (dx < 0 || (dx == 0 && dy < 0)) {
            res = true;
        }
        for (int i = 0; i < 5; i++) {
            x += dx;
            y += dy;
            if (x >= 0 && x < GoBangUtil.LINE_COUNT && y >= 0 && y < GoBangUtil.LINE_COUNT) {
                if (res) {
                    str = chessbean[x][y].getColor() + str;
                } else {
                    str = str + chessbean[x][y].getColor();
                }
            }
        }
        return str;
    }


    private boolean checkWin(int x, int y, int player) {
        boolean win = false;
        if (check(x, y, 1, 0, player) + check(x, y, -1, 0, player) + 1 >= 5)//横向判断
            win = true;
        else if (check(x, y, 0, 1, player) + check(x, y, 0, -1, player) + 1 >= 5)//纵向判断
            win = true;
        else if (check(x, y, 1, 1, player) + check(x, y, -1, -1, player) + 1 >= 5)//右下和左上判断
            win = true;
        else if (check(x, y, 1, -1, player) + check(x, y, -1, 1, player) + 1 >= 5)//右上和左上判断
            win = true;
        if (win == true) {
            if ((renjido && Player == BLACK && first == false) || (first == true && renjido && Player == WHITE)) {
                try {
                    if (mu == true)
                        GoBangTest.music = new Music(new File("Female_jueshagameover.wav"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    if (mu == true)
                        GoBangTest.music = new Music(new File("Female_jueshagamewin.wav"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            GoBangTest.music.play();
            JOptionPane.showMessageDialog(GoBangPanel.this, Player == BLACK ? "黑棋赢了" : "白棋赢了");
            isGameOver = true;
        }
        return !win;
    }

    private int check(int x, int y, int dx, int dy, int player) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            x += dx;
            y += dy;
            if (x >= 0 && x <= 14 && y >= 0 && y <= 14)
                if (chessbean[x][y].getColor() == player) {
                    count++;
                } else break;
        }
        return count;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;//2D画线
        drawLine(g2d);//画线
        drawStar(g2d);//画天元和星
        drawPreSel(g2d, cx, cy);//预设框
        drawString(g2d);//横轴，竖轴字符
        drawChess(g2d);//画棋子
        drawChessNumber(g2d);//画出棋子的落子顺序
        drawTime(g2d);//画出倒计时
    }

    private void drawTime(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));//设置线的宽度
        g2d.drawLine(GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET - GoBangUtil.LINE_WIDTH / 2, 0,
                GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET / 4 * 3);
        g2d.drawLine(GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH / 2, 0,
                GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET / 4 * 3);
        g2d.drawLine(GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET - GoBangUtil.LINE_WIDTH / 2, 0,
                GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH / 2, 0);
        g2d.drawLine(GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET / 4 * 3,
                GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET / 4 * 3);

        FontMetrics fm = g2d.getFontMetrics();
        int height = fm.getAscent();
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("宋体", Font.BOLD, 20));
        int width = fm.stringWidth("" + time);//获取字符的宽度
        g2d.drawString("" + time, GoBangUtil.LINE_COUNT / 2 * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET - width / 2, height * 2);
    }

    private void drawChessNumber(Graphics2D g2d) {//棋子数字的标志
        g2d.setColor(Color.RED);
        if (showNum == true) {//画出数组
            FontMetrics fm = g2d.getFontMetrics();
            int height = fm.getAscent();
            for (int i = 0; i < GoBangUtil.LINE_COUNT; i++)
                for (int j = 0; j < GoBangUtil.LINE_COUNT; j++) {
                    String str = (chessbean[i][j].getOrderNumber()) + "";
                    int weight = fm.stringWidth(str);
                    int x = GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * i - weight / 2;
                    int y = GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * j + height / 2;
                    if (chessbean[i][j].getOrderNumber() != 0)//判断数字是否为空
                        g2d.drawString(str, x, y);
                }
        } else if (showNum == false) {//画出当前下棋的棋子
            ChessBean Last = getLast(chessbean);
            int x = GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * Last.getX() - GoBangUtil.LINE_WIDTH / 10;
            int y = GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * Last.getY() - GoBangUtil.LINE_WIDTH / 10;
            g2d.fillRect(x, y, GoBangUtil.LINE_WIDTH / 5, GoBangUtil.LINE_WIDTH / 5);
        }
        g2d.setColor(Color.BLACK);
    }

    private ChessBean getLast(ChessBean[][] chessbean) {//获取最后一个棋子。
        ChessBean Last = null;
        for (int i = 0; i < GoBangUtil.LINE_COUNT; i++)
            for (int j = 0; j < GoBangUtil.LINE_COUNT; j++) {
                if (Last == null) Last = chessbean[i][j];
                else if (Last.getOrderNumber() < chessbean[i][j].getOrderNumber()) {
                    Last = chessbean[i][j];
                }
            }
        return Last;
    }


    private void drawChess(Graphics2D g2d) {//棋子的显示
        for (ChessBean[] chessBeans2 : chessbean)//二维数组的遍历
            for (ChessBean chessBean : chessBeans2) {
                if (!chessBean.isEmpty()) {//判断二维数组是否为空
                    if (chessBean.getColor() == BLACK) {
                        g2d.setColor(Color.BLACK);
                    } else if (chessBean.getColor() == WHITE) {
                        g2d.setColor(Color.WHITE);
                    }
                    int x = GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * chessBean.getX() - GoBangUtil.CHESS_WIDTH / 2;
                    int y = GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * chessBean.getY() - GoBangUtil.CHESS_WIDTH / 2;
                    g2d.fillOval(x, y, GoBangUtil.CHESS_WIDTH, GoBangUtil.CHESS_WIDTH);
                }
            }
    }

    private void drawString(Graphics2D g2d) {//写上横轴与纵轴
        FontMetrics fm = g2d.getFontMetrics();
        int height = fm.getAscent();//获取字符的高度
        for (int i = 0; i < GoBangUtil.LINE_COUNT; i++) {
            String str = (char) (i + 65) + ""; //asc码转化
            int width = fm.stringWidth(str);//获取字符的宽度
            g2d.drawString("" + (GoBangUtil.LINE_COUNT - i), GoBangUtil.LINE_WIDTH - GoBangUtil.OFFSET, GoBangUtil.LINE_WIDTH * (i + 1) + height / 2);
            g2d.drawString(str, GoBangUtil.LINE_WIDTH * (i + 1) - width / 2, GoBangUtil.LINE_WIDTH * GoBangUtil.LINE_COUNT + GoBangUtil.OFFSET);
        }
    }

    private void drawPreSel(Graphics2D g2d, int x, int y) {//预选框的设置
        g2d.setColor(Color.RED);//设为红色
        //左上向右
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 4, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 2);
        //左上向下
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 4);
        //左下向上
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 4);
        //左下向右
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x - GoBangUtil.LINE_WIDTH / 4, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 2);
        //右上向下
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 4);
        //右上向左
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 4, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y - GoBangUtil.LINE_WIDTH / 2);
        //右下向上
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 4);
        //右下向左
        g2d.drawLine(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 2, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 2
                , GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * x + GoBangUtil.LINE_WIDTH / 4, GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * y + GoBangUtil.LINE_WIDTH / 2);

        g2d.setColor(Color.BLACK);//设回黑色
    }


    private void drawStar(Graphics2D g2d) {//天元与星的绘制
        g2d.fillOval(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT / 2) - GoBangUtil.STAR_WIDTH / 2,//中心的绘制
                GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT / 2) - GoBangUtil.STAR_WIDTH / 2, GoBangUtil.STAR_WIDTH, GoBangUtil.STAR_WIDTH);
        g2d.fillOval(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT / 4) - GoBangUtil.STAR_WIDTH / 2,//左上的绘制
                GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT / 4) - GoBangUtil.STAR_WIDTH / 2, GoBangUtil.STAR_WIDTH, GoBangUtil.STAR_WIDTH);
        g2d.fillOval(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT * 3 / 4) - GoBangUtil.STAR_WIDTH / 2,//右下的绘制
                GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT * 3 / 4) - GoBangUtil.STAR_WIDTH / 2, GoBangUtil.STAR_WIDTH, GoBangUtil.STAR_WIDTH);
        g2d.fillOval(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT / 4) - GoBangUtil.STAR_WIDTH / 2,//左下
                GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT * 3 / 4) - GoBangUtil.STAR_WIDTH / 2, GoBangUtil.STAR_WIDTH, GoBangUtil.STAR_WIDTH);
        g2d.fillOval(GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT * 3 / 4) - GoBangUtil.STAR_WIDTH / 2,//右上
                GoBangUtil.OFFSET + GoBangUtil.LINE_WIDTH * (GoBangUtil.LINE_COUNT / 4) - GoBangUtil.STAR_WIDTH / 2, GoBangUtil.STAR_WIDTH, GoBangUtil.STAR_WIDTH);
    }

    private void drawLine(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));//设置线的宽度
        for (int i = 0; i < GoBangUtil.LINE_COUNT; i++)//画线操作
            g2d.drawLine(GoBangUtil.OFFSET, i * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET, GoBangUtil.OFFSET + (GoBangUtil.LINE_COUNT - 1) * GoBangUtil.LINE_WIDTH,
                    i * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET);
        for (int i = 0; i < GoBangUtil.LINE_COUNT; i++)//画线操作
            g2d.drawLine(i * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET, GoBangUtil.OFFSET,
                    i * GoBangUtil.LINE_WIDTH + GoBangUtil.OFFSET, GoBangUtil.OFFSET + (15 - 1) * GoBangUtil.LINE_WIDTH);
    }

    public void undo() {
        ChessBean tmp = getLast(chessbean);
        if (tmp.getOrderNumber() != 0) {
            Player = chessbean[tmp.getX()][tmp.getY()].getColor();
            chessbean[tmp.getX()][tmp.getY()].setColor(EMPTY);  //使二位数组中最后一个落子初始化
            chessbean[tmp.getX()][tmp.getY()].setOrderNumber(0);
            chessbean[tmp.getX()][tmp.getY()].reset();
            Count--;//计数器减1
            repaint();
            if (renjido) {
                ChessBean tmp1 = getLast(chessbean);
                Player = chessbean[tmp1.getX()][tmp1.getY()].getColor();
                chessbean[tmp1.getX()][tmp1.getY()].setColor(EMPTY);  //使二位数组中最后一个落子初始化
                chessbean[tmp1.getX()][tmp1.getY()].setOrderNumber(0);
                chessbean[tmp1.getX()][tmp1.getY()].reset();
                Count--;//计数器减1
            }
        }
    }


    public void showNum(boolean selected) {
        showNum = selected;
        repaint();
    }

    private void doshuangji() {
        Music music = null;
        try {
            music = new Music(new File("022.wav"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (intel)//估值函数
        {
            if (checkWin(gx, gy, chessbean[gx][gy].getColor())) {
                if (mu == true)
                    music.play();
                List<ChessBean> list = getSortList(chessbean[gx][gy].getColor(), chessbean);
                if (Count < 5) {
                    Random random = new Random();
                    boolean flag = true;
                    ChessBean Bean;
                    while (flag) {
                        int i1 = random.nextInt(5) + 5;
                        int i2 = random.nextInt(5) + 5;
                        if (chessbean[i1][i2].isEmpty()) {
                            Bean = new ChessBean(i1, i2, 1, 1);
                            gx = Bean.getX();
                            gy = Bean.getY();
                            Bean.setOrderNumber(Count);
                            Bean.setColor(Player);
                            chessbean[gx][gy] = Bean;
                            Count++;
                            repaint();
                            checkWin(gx, gy, Player);
                            Player = 3 - Player;
                            flag = false;
                        }
                    }
                } else {
                    ChessBean Bean = list.get(0);
                    gx = Bean.getX();
                    gy = Bean.getY();
                    Bean.setOrderNumber(Count);
                    Bean.setColor(Player);
                    chessbean[gx][gy] = Bean;
                    Count++;
                    repaint();
                    checkWin(gx, gy, Player);
                    Player = 3 - Player;
                }
            }
        } else {//估值函数加搜索树
            if (checkWin(gx, gy, chessbean[gx][gy].getColor())) {
                if (Count <= GoBangUtil.LINE_COUNT * GoBangUtil.LINE_COUNT - pointNum) {
                    if (mu == true)
                        music.play();
                    if (Count < 5) {
                        Random random = new Random();
                        boolean flag = true;
                        while (flag) {
                            int i1 = random.nextInt(5) + 5;
                            int i2 = random.nextInt(5) + 5;
                            if (chessbean[i1][i2].isEmpty()) {
                                chessBeanForTree = new ChessBean(i1, i2, Count, 1);
                                flag = false;
                            }
                        }
                    } else {
                        getTree(0, 3 - Player, chessbean, -Integer.MAX_VALUE, Integer.MAX_VALUE, false);
                        treetag = false;
                    }
                    ChessBean Bean = chessBeanForTree;
                    gx = Bean.getX();
                    gy = Bean.getY();
                    Bean.setOrderNumber(Count);
                    Bean.setColor(Player);
                    chessbean[gx][gy] = Bean;
                    Count++;
                    repaint();
                    checkWin(gx, gy, Player);
                    Player = 3 - Player;
                } else {
                    if (mu == true)
                        music.play();
                    List<ChessBean> list = getSortList(chessbean[gx][gy].getColor(), chessbean);
                    ChessBean Bean = list.get(0);
                    gx = Bean.getX();
                    gy = Bean.getY();
                    Bean.setOrderNumber(Count);
                    Bean.setColor(Player);
                    chessbean[gx][gy] = Bean;
                    Count++;
                    repaint();
                    checkWin(gx, gy, Player);
                    Player = 3 - Player;
                }
            }
        }
        repaint();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (isGameOver == false)
                    doshuangji();//延时执行下棋操作
            }
        }, 250);        //延时0.5s
        if (Count == GoBangUtil.LINE_COUNT * GoBangUtil.LINE_COUNT + 1) {
            JOptionPane.showMessageDialog(GoBangPanel.this, "平局");
            timer.cancel();
        }
    }

    public void newGame(boolean showSortSelected, boolean renrendo, boolean intelDo, int deeptmp,
                        int pointNum, JTextArea area, boolean shuangjido, boolean renjido, boolean firstdo) throws InterruptedException { //新游戏的实现 传入selected控制是否显示落子顺序

        chessbean = new ChessBean[GoBangUtil.LINE_COUNT][GoBangUtil.LINE_COUNT];
        JOptionPane.showMessageDialog(GoBangPanel.this, "游戏开始");
        this.renrenDo = renrendo;
        this.intel = intelDo;
        this.deep = deeptmp;
        this.pointNum = pointNum;
        this.showNum = showSortSelected;
        this.area = area;
        this.shuangjido = shuangjido;
        this.renjido = renjido;
        this.first = firstdo;
        time = 20;
        gx = GoBangUtil.LINE_COUNT / 2;
        gy = GoBangUtil.LINE_COUNT / 2;
        treetag = false;
        this.area.setText("");
        isGameOver = false;//判断游戏是否开始
        Count = 1;//初始化计数器
        showNum = showSortSelected;
        for (int i = 0; i < GoBangUtil.LINE_COUNT; i++)
            for (int j = 0; j < GoBangUtil.LINE_COUNT; j++) {
                chessbean[i][j] = new ChessBean(i, j, 0, EMPTY); //初始化数组
                chessbean[i][j].reset();
            }
        if (renrenDo) {
            Player = BLACK;//初始化颜色
        } else if (renjido) {
            if (first == true) {
                Player = BLACK;
            } else {
                Player = WHITE;//初始化颜色
                chessbean[GoBangUtil.LINE_COUNT / 2][GoBangUtil.LINE_COUNT / 2].setColor(BLACK);
                chessbean[GoBangUtil.LINE_COUNT / 2][GoBangUtil.LINE_COUNT / 2].setOrderNumber(Count);//打印第一个棋子
                Count++;
                repaint();
            }
        } else {
            Player = WHITE;//初始化颜色
            chessbean[GoBangUtil.LINE_COUNT / 2][GoBangUtil.LINE_COUNT / 2].setColor(BLACK);
            chessbean[GoBangUtil.LINE_COUNT / 2][GoBangUtil.LINE_COUNT / 2].setOrderNumber(Count);//打印第一个棋子
            Count++;
            doshuangji();
        }

    }


    // 棋型信息
    public static enum Level {
        CON_5("长连", 0, new String[]{"11111", "22222"}, 100000),
        ALIVE_4("活四", 1, new String[]{"011110", "022220"}, 10000),
        GO_4("冲四", 2, new String[]{"211110|011112|0101110|0110110",
                "122220|022221|0202220|0220220"}, 500),
        DEAD_4("死四", 3, new String[]{"211112", "122221"}, -5),
        ALIVE_3("活三", 4, new String[]{"0011100", "0022200"}, 2000),//更改点
        SLEEP_3("眠三", 5, new String[]{
                "001112|010112|011012|10011|10101|2011102|011102",
                "002221|020221|022021|20022|20202|1022201|022201"}, 50),
        DEAD_3("死三", 6, new String[]{"21112", "12221"}, -5),
        ALIVE_2("活二", 7, new String[]{"00110|01010|010010|010110", "00220|02020|020020|020220"}, 100),
        SLEEP_2("眠二", 8, new String[]{
                "000112|001012|010012|10001|2010102|2011002",
                "000221|002021|020021|20002|1020201|1022001"}, 3),
        DEAD_2("死二", 9, new String[]{"2112", "1221"}, -5),
        NULL("null", 10, new String[]{"", ""}, 0);
        private String name;
        private int index;
        private String[] regex;// 正则表达式
        int score;// 分值

        // 构造方法
        private Level(String name, int index, String[] regex, int score) {
            this.name = name;
            this.index = index;
            this.regex = regex;
            this.score = score;
        }

        // 覆盖方法
        @Override
        public String toString() {
            return this.name;
        }
    }

    ;

    // 方向
    private static enum Direction {
        HENG, SHU, PIE, NA
    }

    ;

    // 位置分
    private static int[][] position = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0},
            {0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0},
            {0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0},
            {0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0},
            {0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1, 0},
            {0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1, 0},
            {0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1, 0},
            {0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0},
            {0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0},
            {0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0},
            {0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

}




