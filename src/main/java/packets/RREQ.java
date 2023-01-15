package packets;

import utils.Converter;

import java.util.Arrays;

import static packets.RREQ.Flags.U;

public class RREQ extends Packet {

    public enum Flags {
        U((byte) 0x20);

        private byte value;

        Flags(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    private final byte flag;
    private byte hopCount;
    private final byte reqId;
    private final byte[] destAddr;
    private byte destSeqNum;
    private final byte[] oriAddr;
    private byte oriSeqNum;

    public RREQ(byte flag, byte hopCount, byte reqId, byte[] destAddr, byte destSeqNum, byte[] oriAddr, byte oriSeqNum) {
        super((byte) 1);
        this.flag = flag;
        this.hopCount = hopCount;
        this.reqId = reqId;
        this.destAddr = destAddr;
        this.destSeqNum = destSeqNum;
        this.oriAddr = oriAddr;
        this.oriSeqNum = oriSeqNum;
    }

    public RREQ(byte[] paket) {
        super((byte) 1);
        var addrPlusSeq = Converter.convertAddrPlusSeqNum(new byte[]{paket[4], paket[5], paket[6], paket[7], paket[8], paket[9], paket[10], paket[11]});

        this.flag = paket[1];
        this.hopCount = paket[2];
        this.reqId = paket[3];
        this.destAddr = new byte[]{addrPlusSeq[0], addrPlusSeq[1], addrPlusSeq[2], addrPlusSeq[3]};
        this.destSeqNum = addrPlusSeq[4];
        this.oriAddr = new byte[]{addrPlusSeq[5], addrPlusSeq[6], addrPlusSeq[7], addrPlusSeq[8]};
        this.oriSeqNum = addrPlusSeq[9];
    }

    public byte getFlag() {
        return flag;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public byte getReqId() {
        return reqId;
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

    public byte getOriSeqNum() {
        if (flag == U.value)
            return oriSeqNum = 0;
        return oriSeqNum;
    }


    public byte[] getBytes() {
        var convertedAddrSeqNum = Converter.prepareAddrPlusSeqNumToSend(new byte[]{destAddr[0], destAddr[1], destAddr[2], destAddr[3], destSeqNum,
                oriAddr[0], oriAddr[1], oriAddr[2], oriAddr[3], oriSeqNum});
        return Converter.prepareForEncoding(new byte[]{type, flag, hopCount, reqId, convertedAddrSeqNum[0], convertedAddrSeqNum[1], convertedAddrSeqNum[2], convertedAddrSeqNum[3],
                convertedAddrSeqNum[4], convertedAddrSeqNum[5], convertedAddrSeqNum[6], convertedAddrSeqNum[7]});
    }

    public void incrementHopCount() {
        this.hopCount++;
    }

    @Override
    public String toString() {
        return "RREQ{" +
                "type=" + type +
                ", flag=" + flag +
                ", hopCount=" + hopCount +
                ", reqId=" + reqId +
                ", destAddr=" + Arrays.toString(destAddr) +
                ", destSeqNum=" + destSeqNum +
                ", oriAddr=" + Arrays.toString(oriAddr) +
                ", oriSeqNum=" + oriSeqNum +
                '}';
    }
}
