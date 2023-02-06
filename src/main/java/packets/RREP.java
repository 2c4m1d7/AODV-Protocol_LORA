package packets;

import org.apache.commons.lang3.ArrayUtils;
import utils.Converter;
import utils.MyArrayUtils;
import utils.Parser;

import java.util.Arrays;

public class RREP extends Packet {
    private int lifetime;
    private byte[] destAddr;
    private int destSeqNum;
    private byte[] oriAddr;
    private byte hopCount;

    public RREP(int lifetime, byte[] destAddr, int destSeqNum, byte[] oriAddr, byte hopCount) {
        super((byte) 2);

        this.lifetime = lifetime;
        this.destAddr = destAddr;
        this.destSeqNum = destSeqNum;
        this.oriAddr = oriAddr;
        this.hopCount = hopCount;
    }


    public RREP(byte[] paket) {
        super((byte) 2);
        var converted = Converter.decodeAddrPlusSeqNum(MyArrayUtils.getRangeArray(paket, 4, 11));

        this.lifetime = (((int) paket[3]) | (((int) paket[2]) << 6) | ((((int) paket[1]) << 12)));
        this.destAddr = MyArrayUtils.getRangeArray(converted, 0, 3);
        this.destSeqNum = converted[4];
        this.oriAddr = MyArrayUtils.getRangeArray(converted, 5, 8);
        this.hopCount = converted[9];
    }

    public int getLifetime() {
        return lifetime;
    }

    public byte[] getDestAddr() {
        return destAddr;
    }

    public int getDestSeqNum() {
        return destSeqNum;
    }

    public byte[] getOriAddr() {
        return oriAddr;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void incrementHopCount() {
        this.hopCount++;
    }

    public byte[] getBytes() {
        var converted = Converter.prepareAddrPlusSeqNumToSend(new byte[]{destAddr[0], destAddr[1], destAddr[2], destAddr[3], (byte) destSeqNum,
                oriAddr[0], oriAddr[1], oriAddr[2], oriAddr[3], hopCount});

        return Converter.prepareForEncoding(ArrayUtils.addAll(new byte[]{type, (byte) ((lifetime >> 12) & 0x3f), (byte) ((lifetime >> 6) & 0x3f), (byte) (lifetime & 0x3f)}, converted));
    }

    @Override
    public String toString() {
        return "RREP{" +
                "type=" + type +
                ", lifeTime=" + lifetime +
                ", destAddr=" + Parser.parseBytesToAddr(destAddr) +
                ", destSeqNum=" + destSeqNum +
                ", oriAddr=" + Parser.parseBytesToAddr(oriAddr) +
                ", hopCount=" + hopCount +
                '}';
    }
}
