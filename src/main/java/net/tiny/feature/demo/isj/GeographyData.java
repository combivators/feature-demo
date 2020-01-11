package net.tiny.feature.demo.isj;

public class GeographyData {
    /** ID */
    String id;
    /** 纬度 */
    double latitude;
    /** 经度 */
    double longitude;
    /** 丁目区地名 */
    String address;

    @Override
    public String toString() {
        return String.format("%s, %.3f, %.3f, [%s]", id, latitude, longitude, address);
    }
}
