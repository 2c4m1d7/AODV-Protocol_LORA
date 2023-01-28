package utils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.Function;


public class Converter {

    private static final int PACKET_DECODED_SIZE = 9;
    private static final int PACKET_CONVERTED_SIZE = 12;

    public static byte[] userDataPacketEncode(byte[] udPacket) {

        var a = MyArrayUtils.getRangeArray(udPacket, 0, 4);
        var fB = (byte) ((a[1] & 0x0c) >> 2);
        var sB = (byte) (((a[1] & 0x03) << 6) | ((a[2] & 0x0f) << 2) | ((a[3] & 0x0c) >> 2));
        var tB = (byte) (((a[3] & 0x03) << 6) | ((a[4] & 0x0f) << 2));
        var b = MyArrayUtils.getRangeArray(udPacket, 5, udPacket.length - 1);
        var res = new byte[b.length];
        for (int i = 0; i < res.length; i++) {
            if (i == 0) {
                tB = (byte) (tB | ((b[i] & 0xc0) >> 6));
            } else if (i == res.length - 1) {
                res[i] = (byte) (b[i] << 2);
                continue;
            }
            res[i] = (byte) ((b[i] << 2) | ((b[i + 1] & 0xc0) >> 6));

        }
        return ArrayUtils.addAll(new byte[]{fB, sB, tB}, res);
    }

    public static byte[] userDataPacketDecode(byte[] udPacket) {
        var a = MyArrayUtils.getRangeArray(udPacket, 0, 2);
        var type = (byte) (a[0] >> 2);
        var addr0 = (byte) (((a[0] & 0x03) << 2) | ((a[1] & 0xc0) >> 6));
        var addr1 = (byte) ((a[1] & 0x3C) >> 2);
        var addr2 = (byte) (((a[1] & 0x03) << 2) | ((a[2] & 0xc0) >> 6));
        var addr3 = (byte) ((a[2] & 0x3C) >> 2);
        var b = MyArrayUtils.getRangeArray(udPacket, 3, udPacket.length - 1);
        b = ArrayUtils.addAll(new byte[]{(byte) (a[2] & 0x3)}, b);
        var res = new byte[b.length - 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) (((b[i] & 0x03) << 6) | ((b[i + 1] & 0xfc) >> 2));
        }
        return ArrayUtils.addAll(new byte[]{type, addr0, addr1, addr2, addr3}, res);
    }

    public static byte[] prepareForEncoding(byte[] bs) {
        if (bs.length % PACKET_CONVERTED_SIZE != 0) {
            throw new RuntimeException("wrong data " + Arrays.toString(bs));
        }

        var convert = new Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] b) {
                byte b1 = (byte) (((b[0] & 0xff) << 2) | (b[1] & 0xf0) >>> 4);
                byte b2 = (byte) (((b[1] & 0x0f) << 4) | (b[2] & 0xff) >>> 2);
                byte b3 = (byte) (((b[2] & 0x0f) << 6) | (b[3] & 0xff));
                return new byte[]{b1, b2, b3};
            }
        };

        var arr = new byte[bs.length * 3 / 4];
        var g = 0;
        for (int i = 0; i < bs.length; i += 4) {
            var tmp = new byte[4];
            for (int j = 0; j < 4; j++) {
                tmp[j] = bs[i + j];
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
        if (bs.length % PACKET_DECODED_SIZE != 0) {
            throw new RuntimeException("wrong data " + Arrays.toString(bs));
        }
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

        var arr = new byte[bs.length * 4 / 3];
        var g = 0;
        for (int i = 0; i < bs.length; i += 3) {
            var tmp = new byte[3];
            for (int j = 0; j < 3; j++) {
                tmp[j] = bs[i + j];
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

    public static byte[] decodeAddrPlusSeqNum(byte[] bs) {
        if (bs.length % 4 != 0) {
            throw new RuntimeException("wrong data " + Arrays.toString(bs));
        }
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

        var arr = new byte[bs.length * 5 / 4];
        var g = 0;
        for (int i = 0; i < bs.length; i += 4) {
            var tmp = new byte[4];
            for (int j = 0; j < 4; j++) {
                tmp[j] = bs[i + j];
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
        if (bs.length % 5 != 0) {
            throw new RuntimeException("wrong data " + Arrays.toString(bs));
        }
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

        var arr = new byte[bs.length * 4 / 5];
        var g = 0;
        for (int i = 0; i < bs.length; i += 5) {
            var tmp = new byte[5];
            for (int j = 0; j < 5; j++) {
                tmp[j] = bs[i + j];
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
