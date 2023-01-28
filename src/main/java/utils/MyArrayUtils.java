package utils;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;

public class MyArrayUtils {

    public static byte[] getRangeArray(byte[] arr, int from, int to) {
        var res = new byte[to - from + 1];
        for (int i = 0; i <= to - from; i++) {
            res[i] = arr[i + from];
        }
        return res;
    }
}
