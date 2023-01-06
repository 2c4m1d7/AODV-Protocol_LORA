package model;

import packets.RREP;
import packets.RREQ;

import java.util.*;

public class Node {
    public static final int ACTIVE_ROUTE_TIMEOUT = 3000;
    public static final int MY_ROUTE_TIMEOUT = 2 * ACTIVE_ROUTE_TIMEOUT;
    public static final int NODE_TRAVERSAL_TIME = 40;
    public static final int RREQ_RETRIES = 2;
    public static final int NET_DIAMETER = 35;
    public static final int NET_TRAVERSAL_TIME = 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER;
    public static final int PATH_DISCOVERY_TIME = 2 * NET_TRAVERSAL_TIME;

    public static final byte OVER_MAX = 0x40;

    private Node() {
    }

    public static class ProcessedRREQInfo  {
        private final byte requestID;
        private final byte[] oriAddr;

        public ProcessedRREQInfo(byte requestID, byte[] oriAddr) {
            this.requestID = requestID;
            this.oriAddr = oriAddr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProcessedRREQInfo that = (ProcessedRREQInfo) o;
            return requestID == that.requestID && Arrays.equals(oriAddr, that.oriAddr);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(requestID);
            result = 31 * result + Arrays.hashCode(oriAddr);
            return result;
        }

        @Override
        public String toString() {
            return "ProcessedRREQInfo{" +
                    "requestID=" + requestID +
                    ", oriAddr=" + Arrays.toString(oriAddr) +
                    '}';
        }
    }

    private static final Set<ProcessedRREQInfo> processedRREQ = new HashSet<>();
    private static final Map<Integer, ForwardRoute> ROUTE_TABLE = new HashMap<>();
    private static final Map<Integer, ReverseRoute> REVERSE_ROUTE_TABLE = new HashMap<>();
    private static byte[] ADDR;
    private static byte SEQ_NUM = 0;

    public static void setADDR(byte[] addr) {
        ADDR = addr;
    }

    public static byte[] getADDR() {
        return ADDR;
    }

    public static byte getSeqNum() {
        return SEQ_NUM;
    }

    public static void incrementSeqNum() {
        SEQ_NUM = (byte) ((SEQ_NUM + 1) % 0x3f);
    }

    public static boolean updateRouteEntry(ForwardRoute control) {
        var entry = ROUTE_TABLE.putIfAbsent(Arrays.hashCode(control.getDestAddr()), control);
        if (entry != null) {  // https://github.com/2c4m1d7/AODV-Protocol_LORA#create-or-update-routes  3. ii.
            if (!entry.isValidSeqNum() // todo ? the sequence number is unknown
                    || control.getSeq() > entry.getSeq()
                    || ((control.getSeq() == entry.getSeq()) && control.getHopCount() < entry.getHopCount())) {
                ROUTE_TABLE.put(Arrays.hashCode(control.getDestAddr()), control);
                return true;
            } else return false;
        }
        return true;
    }

    public static boolean updateReverseRouteEntry(ReverseRoute control) {
        var entry = REVERSE_ROUTE_TABLE.putIfAbsent(Arrays.hashCode(control.getDestAddr()), control);
        if (entry != null) {  // https://github.com/2c4m1d7/AODV-Protocol_LORA#create-or-update-routes  3. ii.
            if (entry.getSeq() == -1 // todo ? the sequence number is unknown
                    || control.getSeq() > entry.getSeq()
                    || ((control.getSeq() == entry.getSeq()) && control.getHopCount() < entry.getHopCount())) {
                REVERSE_ROUTE_TABLE.put(Arrays.hashCode(control.getDestAddr()), control);
                return true;
            } else return false;
        }
        return true;
    }

    public static void updateRouteLifetimeRREQ(byte[] destAddr) {
        ROUTE_TABLE.get(Arrays.hashCode(destAddr)).updateLifetimeRREQ();
    }

    public static void updateReverseRouteLifetimeRREQ(byte[] destAddr) {
        REVERSE_ROUTE_TABLE.get(Arrays.hashCode(destAddr)).updateLifetimeRREQ();
    }

    public static void updateRouteLifetimeRREP(byte[] destAddr, long lifetime) {
        ROUTE_TABLE.get(Arrays.hashCode(destAddr)).updateLifetimeRREP(lifetime);
    }

    public static void updateReverseRouteLifetimeRREP(byte[] oriAddr, long lifetime) {
        REVERSE_ROUTE_TABLE.get(Arrays.hashCode(oriAddr)).updateLifetimeRREP(lifetime);
    }

    public static boolean RREQWasProcessed(RREQ rreq) {
        var check = processedRREQ.contains(new ProcessedRREQInfo(rreq.getReqId(), rreq.getOriAddr()));
        if (!check) {
            processedRREQ.add(new ProcessedRREQInfo(rreq.getReqId(), rreq.getOriAddr()));
        }
        return check;
    }

    public static ForwardRoute findRoute(byte[] destAddr) {
        return ROUTE_TABLE.get(Arrays.hashCode(destAddr)); // destAddr
    }

    public static ReverseRoute findReverseRoute(byte[] destAddr) {
        return REVERSE_ROUTE_TABLE.get(Arrays.hashCode(destAddr));
    }

    public static ForwardRoute validRouteExists(RREQ rreq) {
        var route = ROUTE_TABLE.get(Arrays.hashCode(rreq.getDestAddr()));
        if (route == null)
            return null;
        return (route.isValid()
//                && route.isActive()
                && (route.getSeq() >= rreq.getDestSeqNum())) ? route : null;
    }

    /**
     * For Testing
     */
    public static String getInfo() {
        return "Node{processedRREQ: " + processedRREQ +
                "\nROUTE_TABLE: " + ROUTE_TABLE +
                "\nREVERSE_ROUTE_TABLE: " + REVERSE_ROUTE_TABLE +
                "\nSEQ_NUM: " + SEQ_NUM + "}";
    }
}
