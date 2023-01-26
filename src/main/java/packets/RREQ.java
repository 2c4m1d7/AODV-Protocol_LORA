package packets;

import org.apache.commons.lang3.ArrayUtils;
import utils.Converter;
import utils.MyArrayUtils;

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
        var addrPlusSeq = Converter.convertAddrPlusSeqNum(MyArrayUtils.getRangeArray(paket, 4, 11));

        this.flag = paket[1];
        this.hopCount = paket[2];
        this.reqId = paket[3];
        this.destAddr = MyArrayUtils.getRangeArray(addrPlusSeq, 0, 3);
        this.destSeqNum = addrPlusSeq[4];
        this.oriAddr = MyArrayUtils.getRangeArray(addrPlusSeq, 5, 8);
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
        return Converter.prepareForEncoding(ArrayUtils.addAll(new byte[]{type, flag, hopCount, reqId}, convertedAddrSeqNum));
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
