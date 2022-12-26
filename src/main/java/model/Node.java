package model;

import packets.RREQ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node {
    public static final int ACTIVE_ROUTE_TIMEOUT = 3000;
    public static final int MY_ROUTE_TIMEOUT = 2 * ACTIVE_ROUTE_TIMEOUT;
    public static final int NODE_TRAVERSAL_TIME = 40;
    public static final int RREQ_RETRIES = 2;
    public static final int NET_DIAMETER = 35;
    public static final int NET_TRAVERSAL_TIME = 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER;
    public static final int PATH_DISCOVERY_TIME = 2 * NET_TRAVERSAL_TIME;

    private Node() {
    }

    private static class ProcessedRREQInfo {
        private final byte requestID;
        private final byte[] oriAddr;

        public ProcessedRREQInfo(byte requestID, byte[] oriAddr) {
            this.requestID = requestID;
            this.oriAddr = oriAddr;
        }
    }

    private static final Set<ProcessedRREQInfo> processedRREQ = new HashSet<>();
    private static final Map<byte[], RoutTableEntry> table = new HashMap<>();
    private static byte[] ADDR;
    private static byte SEQ_NUM = 0;

    public static void setADDR(byte[] addr) {
        ADDR = addr;
    }

    public static byte[] getADDR() {
        return ADDR;
    }

    public static byte getSeqNum(){
        return SEQ_NUM;
    }
    public static void incrementSeqNum(){
        SEQ_NUM = (byte) ((SEQ_NUM+1) & 0x3f);
    }

    public static boolean addTableEntry(RoutTableEntry control) {
        var entry = table.putIfAbsent(control.getDestAddr(), control.setValidRoute(false));
        if (entry != null) {  // https://github.com/2c4m1d7/AODV-Protocol_LORA#create-or-update-routes  3. ii.
            if (entry.getSeq() == 0 // todo ? the sequence number is unknown
                    || control.getSeq() > entry.getSeq()
                    || ((control.getSeq() == entry.getSeq()) && control.getHopCount() < entry.getHopCount())) {
                table.put(control.getDestAddr(), control);
                return true;
            } else return false;
        }
        return true;
    }

    public static void updateRouteLifetimeRREQ(byte[] destAddr) {
        table.get(destAddr).updateLifetimeRREQ();
    }

    public static void updateRouteLifetimeRREP(byte[] destAddr, long lifetime) {
        table.get(destAddr).updateLifetimeRREP(lifetime);
    }

    public static boolean RREQWasProcessed(RREQ rreq) {
        var check = processedRREQ.contains(new ProcessedRREQInfo(rreq.getReqId(), rreq.getOriAddr()));
        if (!check) {
            processedRREQ.add(new ProcessedRREQInfo(rreq.getReqId(), rreq.getOriAddr()));
        }
        return check;
    }

    public static RoutTableEntry validRouteExists(RREQ rreq) {
        var route = table.get(rreq.getDestAddr());
        if (route == null)
            return null;
        return (route.isValidRoute()
                && route.isActive()
                && (route.getSeq() >= rreq.getDestSeqNum())) ? route : null;
    }

}
