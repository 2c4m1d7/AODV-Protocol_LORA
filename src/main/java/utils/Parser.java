package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {

    public static byte[] parseAddrToByte(String addr) {
        if (addr.length() != 4 ) throw new IllegalArgumentException("Wrong address: {" + addr + "}");

        var arr = addr.split("");
        var res = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = (byte) Integer.parseInt(arr[i], 16);
        }

        return res;

    }

}
