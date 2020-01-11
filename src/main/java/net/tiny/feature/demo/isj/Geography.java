package net.tiny.feature.demo.isj;

import java.util.function.ToDoubleBiFunction;

import net.tiny.mem.DistanceFunction;

/**
 * 地理位置及其距离计算
 */
public class Geography {

    // 球面赤道半径（米）
    final static double EARTH_RADIUS = 6378140.0d;
    final static double RADIUS = EARTH_RADIUS / 1000.0d; // 公里(KM)

    /** 丈量单位米 */
    public static final ToDoubleBiFunction<Geography,Geography> METER = meter();
    public static final DistanceFunction<float[]> DISTSNCE = function();

    /** 丈量单位公里 */
    public static final ToDoubleBiFunction<Geography,Geography> METER_KM = meterKm();

    /** 纬度 */
    public double latitude;
    /** 经度 */
    public double longitude;

    public Geography() {
        this(0.0d, 0.0d);
    }

    public Geography(double lat, double lng) {
        latitude = lat;
        longitude = lng;
    }

    private static DistanceFunction<float[]> function() {
        return new DistanceFunction<float[]>() {
            @Override
            public double distance(float[] a, float[] b) {
                return METER.applyAsDouble(new Geography((double)a[0], (double)a[1]), new Geography((double)b[0], (double)b[1]));
            }
            @Override
            public double area(float[] a, float[] b) {
                double area = 1.0d;
                for(int i=0; i<a.length; i++) {
                    area *= b[i] - a[i];
                }
                return area;
            }
        };
    }

    /**
     * 两点距离（公里）
     * @param g1 源点
     * @param g2 目标点
     * @param precision 小数位数(精度)
     * @return 两点距离（公里）
     */
    public static double distance(Geography g1, Geography g2, int precision) {
        return distance(g1.latitude, g1.longitude, g2.latitude, g2.longitude, precision);
    }

    /**
     * 两点距离（米）
     * @param g1 源点
     * @param g2 目标点
     * @return 两点距离（米）
     */
    public static double distance(Geography g1, Geography g2) {
        return distance(g1.latitude, g1.longitude, g2.latitude, g2.longitude);
    }

    /**
     * 计算两点的方位角（方向）
     * @param g1 源点
     * @param g2 目标点
     * @return 方位角
     */
    public static double direction(Geography g1, Geography g2) {
        return direction(g1.latitude, g1.longitude, g2.latitude, g2.longitude);
    }

    private static ToDoubleBiFunction<Geography,Geography> meter() {
        return new ToDoubleBiFunction<Geography,Geography>() {
            @Override
            public double applyAsDouble(Geography t, Geography u) {
                return distance(t, u);
            }
        };
    }

    private static ToDoubleBiFunction<Geography,Geography> meterKm() {
        return new ToDoubleBiFunction<Geography,Geography>() {
            @Override
            public double applyAsDouble(Geography t, Geography u) {
                return distance(t, u, 3);
            }
        };
    }

    /**
     * 通过球面三角法计算大圆距离
     * @param lat1 源点纬度
     * @param lng1 源点经度
     * @param lat2 目标点纬度
     * @param lng2 目标点经度
     * @return 两点距离（米）
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        // 将经度转换为弧度
        final double rlat1 = Math.toRadians(lat1);
        final double rlng1 = Math.toRadians(lng1);
        final double rlat2 = Math.toRadians(lat2);
        final double rlng2 = Math.toRadians(lng2);

        // 找出两点的中心角（弧度）
        final double a =
          Math.sin(rlat1) * Math.sin(rlat2) +
          Math.cos(rlat1) * Math.cos(rlat2) *
          Math.cos(rlng1 - rlng2);

        // 两点之间的距离（米）
        return EARTH_RADIUS * Math.acos(a);
    }

    /**
     * 计算两点的距离（公里）
     * @param lat1 源点纬度
     * @param lng1 源点经度
     * @param lat2 目标点纬度
     * @param lng2 目标点经度
     * @param precision 小数位数(精度)
     * @return  两点距离（公里）
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2, int precision) {
        final double lat = Math.toRadians(lat2 - lat1);
        final double lng = Math.toRadians(lng2 - lng1);
        final double A = Math.sin(lat / 2) * Math.sin(lat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lng / 2) * Math.sin(lng / 2);
        final double C = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1 - A));
        final double decimalNo = Math.pow(10, precision);
        final double distance = RADIUS * C;
        return Math.round(decimalNo * distance / 1) / decimalNo;
    }

    /**
     * 计算两点的方位角（方向）
     * @param latitude1 源点纬度
     * @param longitude1 源点经度
     * @param latitude2 目标点纬度
     * @param longitude2 目标点经度
     * @return 方位角
     */
    public static double direction(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double lng1 = Math.toRadians(longitude1);
        double lng2 = Math.toRadians(longitude2);
        double Y = Math.sin(lng2 - lng1) * Math.cos(lat2);
        double X = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);
        double deg = Math.toDegrees(Math.atan2(Y, X));
        double angle = (deg + 360) % 360;
        return Math.abs(angle) + (1.0d / 7200.0d);
    }
}
