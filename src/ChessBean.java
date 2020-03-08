public class ChessBean implements Comparable<ChessBean>{
    private int x;//x的行数
    private int y;//y的行数
    private int OrderNumber;//落子顺序
    private int Color;//颜色
    private int atkScore;
    private int defScore;
    private int sumScore;
    public StringBuffer buffer;

    public ChessBean(int x, int y, int OrderNumber, int Color) {
        this.x = x;
        this.y = y;
        this.OrderNumber = OrderNumber;
        this.Color = Color;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getOrderNumber() {
        return OrderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        OrderNumber = orderNumber;
    }

    public int getColor() {
        return Color;
    }

    public void setColor(int color) {
        Color = color;
    }

    public boolean isEmpty() {//判断颜色的赋值
        return this.Color == GoBangPanel.EMPTY;
    }

    public int getDefScore() {
        return defScore;
    }

    public void setDefScore(int defScore) {
        this.defScore = defScore;
    }

    public int getSumScore() {
        return sumScore;
    }

    public void setSumScore(int sumScore) {
        this.sumScore = sumScore;
    }

    public int getAtkScore() {
        return atkScore;
    }

    public void setAtkScore(int atkScore) {
        this.atkScore = atkScore;
    }

    public StringBuffer append(String more) {
        return this.buffer.append(more);
    }//清空
    public void reset() {
        clearDetail();
        atkScore = defScore = sumScore = 0;
        buffer = null;
    }

    public void clearDetail() {
        buffer = new StringBuffer();
    }

    @Override
    public int compareTo(ChessBean o) {
        if(this.getSumScore()>o.getSumScore()) return -1;
        else if(this.getSumScore()<o.getSumScore()) return 1;
        else return 0;
    }

    public String getBuffer() {
        return buffer.toString();
    }

    public void setBuffer(StringBuffer buffer) {
        this.buffer = buffer;
    }
}
