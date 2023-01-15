package packets;

import utils.Converter;

import java.util.Arrays;

public class RREP extends Packet {
    private  int lifetime;
    private byte[] destAddr;
    private byte destSeqNum;
    private byte[] oriAddr;
    private byte hopCount;

    public RREP(int lifetime, byte[] destAddr, byte destSeqNum, byte[] oriAddr, byte hopCount) {
        super((byte) 2);

        this.lifetime =  lifetime;
        this.destAddr = destAddr;
        this.destSeqNum = destSeqNum;
        this.oriAddr = oriAddr;
        this.hopCount = hopCount;
    }


    public RREP(byte[] paket) {
        super((byte) 2);
        var converted = Converter.convertAddrPlusSeqNum(new byte[]{paket[4], paket[5], paket[6],  paket[7], paket[8], paket[9], paket[10],  paket[11]});

        this.lifetime =  (((int) paket[3])  | (((int) paket[2]) << 6) | ((((int) paket[1]) << 12)));
        this.destAddr = new byte[]{converted[0], converted[1], converted[2], converted[3]};
        this.destSeqNum = converted[4];
        this.oriAddr = new byte[]{converted[5], converted[6], converted[7], converted[8]};
        this.hopCount = converted[9];
    }

    public int getLifetime() {
        return lifetime;
    }

    public byte[] getDestAddr() {
        return destAddr;
    }

    public byte getDestSeqNum() {
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
        var converted = Converter.prepareAddrPlusSeqNumToSend(new byte[]{destAddr[0],destAddr[1],destAddr[2], destAddr[3], destSeqNum,
                oriAddr[0],oriAddr[1],oriAddr[2], oriAddr[3], hopCount});
        return Converter.prepareForEncoding(new byte[]{type, (byte) ((lifetime >> 12) & 0x3f), (byte) ((lifetime >> 6) & 0x3f), (byte) (lifetime & 0x3f),
                converted[0],converted[1],converted[2], converted[3],
                converted[4],converted[5],converted[6], converted[7]});
    }

    @Override
    public String toString() {
        return "RREP{" +
                "type=" + type +
                ", lifeTime=" + lifetime +
                ", destAddr=" + Arrays.toString(destAddr) +
                ", destSeqNum=" + destSeqNum +
                ", oriAddr=" + Arrays.toString(oriAddr) +
                ", hopCount=" + hopCount +
                '}';
    }
}
