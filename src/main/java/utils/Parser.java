package utils;

public class Parser {

    public static byte[] parseAddrToBytes(String addr) {
        if (addr.length() != 4) throw new IllegalArgumentException("Wrong address: {" + addr + "}");

        var arr = addr.split("");
        var res = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = (byte) Integer.parseInt(arr[i], 16);
        }
        return res;
    }

    public static String parseBytesToAddr(byte[] bytes) {
        if (bytes == null){
            return null;
        }
        String res = "";
        for (byte b : bytes) {
            res += Integer.toHexString(b);
        }
        return res.toUpperCase();
    }

}
