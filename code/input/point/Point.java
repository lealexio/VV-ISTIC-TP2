public class Point {
    private int x;
    private int y;
    private int z;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public double whatIsThat(Point point) {
        x=x*2+y;
        return x*point.x + y*point.y+ point.z;
    }

    public Point sub(Point p) {
        return new Point(x - p.x, y - p.y);
    }

}