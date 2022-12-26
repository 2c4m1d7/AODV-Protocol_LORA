package utils;

import java.util.function.Function;


public class Converter {
    public static byte[] prepareForEncoding(byte[] bs) {
        byte[] b;
        if (((bs.length / 2 % 2 != 0) || (bs.length % 2 == 1))) {
            int n = bs.length;
            do {
                n++;
            } while (n / 2 % 2 != 0);
            b = new byte[n];
            for (int i = 0; i < n; i++) {
                if (i <= bs.length - 1)
                    b[i] = bs[i];
                else
                    b[i] = 0;
            }
        } else b = bs;

        var convert = new Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] b) {
                byte b1 = (byte) (((b[0] & 0xff) << 2) | (b[1] & 0xf0) >>> 4);
                byte b2 = (byte) (((b[1] & 0x0f) << 4) | (b[2] & 0xff) >>> 2);
                byte b3 = (byte) (((b[2] & 0x0f) << 6) | (b[3] & 0xff));
                return new byte[]{b1, b2, b3};
            }
        };

        var arr = new byte[b.length * 3 / 4];
        var g = 0;
        for (int i = 0; i < b.length; i += 4) {
            var tmp = new byte[4];
            for (int j = 0; j < 4; j++) {
                tmp[j] = b[i + j];
            }
            var c = 3 * g;
            for (byte b6 : convert.apply(tmp)) {
                arr[c] = b6;
                c++;
            }
            g++;
        }
        return arr;
    }

    public static byte[] convertDecoded(byte[] bs) {
        byte[] b;
        if (bs.length % 3 != 0) {
            int n = bs.length;
            do {
                n++;
            } while (n % 3 != 0);
            b = new byte[n];
            for (int i = 0; i < n; i++) {
                if (i <= bs.length - 1)
                    b[i] = bs[i];
                else
                    b[i] = 0;
            }
        } else b = bs;

        var convert = new Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] b) {
                byte value1 = (byte) ((b[0] & 0xff) >> 2);
                byte value2 = (byte) (((b[0] & 0x03) << 4) | ((b[1] & 0xf0) >> 4));
                byte value3 = (byte) (((b[1] & 0x0f) << 2) | ((b[2] & 0xf0) >> 6));
                byte value4 = (byte) (b[2] & 0x3f);

                return new byte[]{value1, value2, value3, value4};
            }
        };

        var arr = new byte[b.length * 4 / 3];
        var g = 0;
        for (int i = 0; i < b.length; i += 3) {
            var tmp = new byte[3];
            for (int j = 0; j < 3; j++) {
                tmp[j] = b[i + j];
            }
            var c = 4 * g;
            for (byte b8 : convert.apply(tmp)) {
                arr[c] = b8;
                c++;
            }
            g++;
        }
        return arr;
    }

    public static byte[] convertAddrPlusSeqNum(byte[] bs) {
        byte[] b;
        if (((bs.length / 2 % 2 != 0) || (bs.length % 2 == 1))) {
            int n = bs.length;
            do {
                n++;
            } while (n / 2 % 2 != 0);
            b = new byte[n];
            for (int i = 0; i < n; i++) {
                if (i <= bs.length - 1)
                    b[i] = bs[i];
                else
                    b[i] = 0;
            }
        } else b = bs;

        var convert = new Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] b) {
                byte b1 = (byte) ((b[0] & 0x3f) >> 2);
                byte b2 = (byte) (((b[0] & 0x03) << 2) | ((b[1] & 0x30) >> 4));
                byte b3 = (byte) (b[1] & 0x0f);
                byte b4 = (byte) ((b[2] & 0x3c) >> 2);
                byte b5 = (byte) ((b[3] & 0x3f) | ((b[2] & 0x03) << 6));
                return new byte[]{b1, b2, b3, b4, b5};
            }
        };

        var arr = new byte[b.length * 5 / 4];
        var g = 0;
        for (int i = 0; i < b.length; i += 4) {
            var tmp = new byte[4];
            for (int j = 0; j < 4; j++) {
                tmp[j] = b[i + j];
            }
            var c = 5 * g;
            for (byte b4 : convert.apply(tmp)) {
                arr[c] = b4;
                c++;
            }
            g++;
        }
        return arr;
    }


    public static byte[] prepareAddrPlusSeqNumToSend(byte[] bs) {
        byte[] b;
        if (bs.length % 5 != 0) {
            int n = bs.length;
            do {
                n++;
            } while (n % 5 != 0);
            b = new byte[n];
            for (int i = 0; i < n; i++) {
                if (i <= bs.length - 1)
                    b[i] = bs[i];
                else
                    b[i] = 0;
            }
        } else b = bs;

        var convert = new Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] b) {
                byte b1 = (byte) ((b[0] << 2) | ((b[1] & 0x0c) >> 2));
                byte b2 = (byte) (((b[1] & 0x03) << 4) | ((b[2] & 0x0f)));
                byte b3 = (byte) (((b[3] & 0x0f) << 2) | ((b[4] & 0xc0) >> 6));
                byte b4 = (byte) (b[4] & 0x3f);
                return new byte[]{b1, b2, b3, b4};
            }
        };

        var arr = new byte[b.length * 4 / 5];
        var g = 0;
        for (int i = 0; i < b.length; i += 5) {
            var tmp = new byte[5];
            for (int j = 0; j < 5; j++) {
                tmp[j] = b[i + j];
            }
            var c = 4 * g;
            for (byte b8 : convert.apply(tmp)) {
                arr[c] = b8;
                c++;
            }
            g++;
        }
        return arr;
    }

}
