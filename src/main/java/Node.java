import java.util.HashMap;
import java.util.Map;

public class Node {

    private Node() {}

    private static final Map<byte[], RoutTableEntry> table = new HashMap<>();

    private static byte[] ADDR = new byte[4];

    public static void setADDR(byte[] addr) {
        ADDR = addr;
    }

    public static byte[] getADDR() {
        return ADDR;
    }

    public static void addTableEntry(byte[] oriAddr, byte[] prevHop, byte hopCount, byte oriSeq){
        var entry = new RoutTableEntry(oriAddr, prevHop, hopCount, oriSeq);
        table.put(entry.getDestAddr(), entry);
        System.out.println(entry);
    }


    public static class RoutTableEntry {
        private byte hopCount;
        private byte oriSeq;
        private byte[] destAddr;
        private byte[] nextAddr;

        public RoutTableEntry(byte[] destAddr, byte[] nextAddr, byte hopCount, byte oriSeq) {
            this.hopCount = hopCount;
            this.oriSeq = oriSeq;
            this.destAddr = destAddr;
            this.nextAddr = nextAddr;
        }

        public byte getHopCount() {
            return hopCount;
        }

        public void setHopCount(byte hopCount) {
            this.hopCount = hopCount;
        }

        public byte getOriSeq() {
            return oriSeq;
        }

        public void setOriSeq(byte oriSeq) {
            this.oriSeq = oriSeq;
        }

        public byte[] getDestAddr() {
            return destAddr;
        }

        public void setDestAddr(byte[] destAddr) {
            this.destAddr = destAddr;
        }

        public byte[] getNextAddr() {
            return nextAddr;
        }

        public void setNextAddr(byte[] nextAddr) {
            this.nextAddr = nextAddr;
        }
    }
}
