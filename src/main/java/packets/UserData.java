package packets;

import org.apache.commons.lang3.ArrayUtils;
import utils.Converter;
import utils.MyArrayUtils;
import utils.Parser;

import java.util.Arrays;

public class UserData {

    private static final byte type = 0;
    private byte[] destAddr;

    private String message;

    public UserData() {
    }

    public UserData(byte[] destAddr, String message) {
        this.destAddr = destAddr;
        this.message = message;
    }

    public UserData(byte[] decoded) {
        var converted = Converter.userDataPacketDecode(decoded);
        destAddr = MyArrayUtils.getRangeArray(converted, 1, 4);
        message = new String(MyArrayUtils.getRangeArray(converted, 5, converted.length - 1));
    }

    public byte[] getDestAddr() {
        return destAddr;
    }

    public UserData setDestAddr(byte[] destAddr) {
        this.destAddr = destAddr;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public UserData setMessage(String message) {
        this.message = message;
        return this;
    }

    public byte getType() {
        return type;
    }

    public byte[] getBytes() {
        return Converter.userDataPacketEncode(ArrayUtils.addAll(new byte[]{type, destAddr[0], destAddr[1], destAddr[2], destAddr[3]}, message.getBytes()));
    }

    @Override
    public String toString() {
        return "UserData{" +
                "destAddr=" + Parser.parseBytesToAddr(destAddr) +
                ", message='" + message + '\'' +
                '}';
    }
}
