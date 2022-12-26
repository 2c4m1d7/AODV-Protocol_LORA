package packets;

import utils.Converter;

import java.util.Arrays;

public class RREP extends Packet {
    private  int lifeTime;
    private byte[] destAddr;
    private byte destSeqNum;
    private byte[] oriAddr;
    private byte hopCount;

//    public RREP(byte lifeTime, byte[] destAddr, byte destSeqNum, byte[] oriAddr, byte hopCount) {
//        super((byte) 2);
//        this.lifeTime = lifeTime;
//        this.destAddr = destAddr;
//        this.destSeqNum = destSeqNum;
//        this.oriAddr = oriAddr;
//        this.hopCount = hopCount;
//        updateArr();
//    }


    public RREP(byte... paket) {
        super((byte) 2);
        var converted = Converter.convertAddrPlusSeqNum(new byte[]{paket[4], paket[5], paket[6],  paket[7], paket[8], paket[9], paket[10],  paket[11]});

        this.lifeTime =  (((int) paket[3])  | (((int) paket[2]) << 6) | ((((int) paket[1]) << 12)));
        this.destAddr = new byte[]{converted[0], converted[1], converted[2], converted[3]};
        this.destSeqNum = converted[4];
        this.oriAddr = new byte[]{converted[5], converted[6], converted[7], converted[8]};
        this.hopCount = converted[9];
    }

    public int getLifeTime() {
        return lifeTime;
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

    public void increaseHopCount() {
        this.hopCount++;
    }

    public byte[] getBytes() {
        var converted = Converter.prepareAddrPlusSeqNumToSend(new byte[]{destAddr[0],destAddr[1],destAddr[2], destAddr[3], destSeqNum,
                oriAddr[0],oriAddr[1],oriAddr[2], oriAddr[3], hopCount});
        return new byte[]{type, (byte) ((lifeTime >> 12) & 0x3f), (byte) ((lifeTime >> 6) & 0x3f), (byte) (lifeTime & 0x3f),
                converted[0],converted[1],converted[2], converted[3],
                converted[4],converted[5],converted[6], converted[7]};
    }

    @Override
    public String toString() {
        return "RREP{" +
                "type=" + type +
                ", lifeTime=" + lifeTime +
                ", destAddr=" + Arrays.toString(destAddr) +
                ", destSeqNum=" + destSeqNum +
                ", oriAddr=" + Arrays.toString(oriAddr) +
                ", hopCount=" + hopCount +
                '}';
    }
}
