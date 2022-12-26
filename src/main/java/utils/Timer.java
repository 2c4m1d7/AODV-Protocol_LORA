package utils;

public class Timer {
    public static long getCurrentTimestamp() {
//        return (int) (System.currentTimeMillis() % 0x3ffff);
        return System.currentTimeMillis();
    }
}
