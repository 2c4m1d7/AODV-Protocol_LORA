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
        var addrPlusSeq = Converter.convertAddrPlusSeqNum(new byte[]{paket[4], paket[5], paket[6],  paket[7], paket[8], paket[9], paket[10],  paket[11]});


        this.lifeTime =  (((int) paket[3])  | (((int) paket[2]) << 6) | ((((int) paket[1]) << 12)));
        this.destAddr = new byte[]{addrPlusSeq[0], addrPlusSeq[1], addrPlusSeq[2], addrPlusSeq[3]};
        this.destSeqNum = addrPlusSeq[4];
        this.oriAddr = new byte[]{addrPlusSeq[5], addrPlusSeq[6], addrPlusSeq[7], addrPlusSeq[8]};
        this.hopCount = addrPlusSeq[9];
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
        var convertedAddrSeqNum = Converter.prepareAddrPlusSeqNumToSend(new byte[]{destAddr[0],destAddr[1],destAddr[2], destAddr[3], destSeqNum,
                oriAddr[0],oriAddr[1],oriAddr[2], oriAddr[3], hopCount});
        return new byte[]{type, (byte) (lifeTime >> 12), (byte) ((lifeTime >> 6) & 0x3f), (byte) (lifeTime & 0x3f),  convertedAddrSeqNum[0],convertedAddrSeqNum[1],convertedAddrSeqNum[2], convertedAddrSeqNum[3],
                convertedAddrSeqNum[4],convertedAddrSeqNum[5],convertedAddrSeqNum[6], convertedAddrSeqNum[7]};
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
