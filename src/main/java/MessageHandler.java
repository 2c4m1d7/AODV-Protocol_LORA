import model.*;
import org.apache.commons.lang3.StringUtils;
import packets.RREP;
import packets.RREQ;
import utils.Converter;
import utils.MyArrayUtils;
import utils.MyLogger;
import utils.Parser;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class MessageHandler {

    public static SendPacket handle(byte[] bytes) {

        var s = StringUtils.substringBefore(new String(bytes), "\r");
        var addr = Optional.ofNullable(StringUtils.substringBetween(s, ",", ",")).orElse("");
        byte[] prevHop = new byte[4];
        try {
            if (s.contains("LR")) {
                MyLogger.info("GOT TO HANDLE: " + new String(bytes));
                var arr = s.split(",");
                prevHop = Parser.parseAddrToBytes(arr[1]);
                var data = arr[3];
                var decoded = Base64.getDecoder().decode(data.getBytes());
                var converted = Converter.convertDecoded(decoded);
                return switch (converted[0]) {
                    case 1 -> handleRREQ(converted, prevHop);
                    case 2 -> handleRREP(converted, prevHop);
                    case 0 -> handleUD(decoded, prevHop);
                    default -> null;
                };
            } else if (addr.length() == 4 && s.contains("AT")) {
                Node.setADDR(Parser.parseAddrToBytes(addr));
            }
        } catch (IllegalArgumentException e) {
            MyLogger.warn("from " + Arrays.toString(prevHop) + " " + e.getMessage());
        } catch (Exception e) {
            MyLogger.warn(e.getMessage());
        }
        return null;
    }

    public static SendPacket handleUD(byte[] decoded, byte[] prevHop) {
        var converted = Converter.userDataPacketDecode(decoded);
        var destAddr = MyArrayUtils.getRangeArray(converted, 1, 4);
        var message = new String(MyArrayUtils.getRangeArray(converted, 5, converted.length - 1));
        if (Arrays.equals(destAddr, Node.getADDR())) {
            System.out.println("Message from " + Arrays.toString(prevHop) + ": \"" + message + "\"");
            return null;
        }

        var route = Node.validRouteExists(destAddr);
        if (route != null) {
            return SendPacket.UD.setPacket(decoded).setNextHop(route.getNextHop());
        } else {
            var rreq = new RREQ(RREQ.Flags.U.getValue(), (byte) 0, Node.useREQid(), destAddr, (byte) 0, Node.getADDR(), Node.getSeqNum());
//            var sendPacketWithoutNextHop = SendPacket.UD.setPacket(decoded);
            return SendPacket.RREQ.setPacket(rreq.getBytes()).setNextHop(Parser.parseAddrToBytes("FFFF"));
        }
    }

    private static SendPacket handleRREQ(byte[] decodedPacket, byte[] prevHop) {
        RREQ rreq;
        try {
            rreq = new RREQ(decodedPacket);
            MyLogger.info("\n\nFrom " + Parser.parseBytesToAddr(prevHop) + "\n" + rreq + "\n");
        } catch (Exception e) {
            MyLogger.warn(e.getMessage());
            return null;
        }

        rreq.incrementHopCount();
        var forwardRouteToPrevHop = new ForwardRoute(prevHop, null, (byte) 1, (byte) 0x40, prevHop, false);
        byte hopCount;
        var r = Node.findRoute(rreq.getDestAddr());
        if (r != null) {
            hopCount = r.getHopCount();
        } else hopCount = 0x40;
        var forwardRoute = new ForwardRoute(rreq.getDestAddr(), rreq.getOriAddr(), hopCount, rreq.getDestSeqNum(), null, (rreq.getFlag() == RREQ.Flags.U.getValue()));
        var forwardRouteReverse = new ForwardRoute(rreq.getOriAddr(), rreq.getDestAddr(), rreq.getHopCount(), rreq.getOriSeqNum(), prevHop, true);
        var reverseRoute = new ReverseRoute(rreq.getDestAddr(), rreq.getOriAddr(), rreq.getHopCount(), rreq.getDestSeqNum(), prevHop, true);

        if (Node.updateRouteEntry(forwardRouteToPrevHop)) {
            Node.updateRouteLifetimeRREQ(forwardRouteToPrevHop.getDestAddr());
        }
        if (Node.updateRouteEntry(forwardRoute)) {
            Node.updateRouteLifetimeRREQ(forwardRoute.getDestAddr());
        }
        if (Node.updateRouteEntry(forwardRouteReverse)) {
            Node.updateRouteLifetimeRREQ(forwardRouteReverse.getDestAddr());
        }

        if (Node.RREQWasProcessed(rreq)) {
            return null;
        }

        var route = Node.validRouteExists(rreq.getDestAddr());
        if (Arrays.equals(rreq.getDestAddr(), Node.getADDR())) {
            forwardRoute.setDestAddr(reverseRoute.getDestAddr()).setSourceAddr(reverseRoute.getSourceAddr()).setHopCount(reverseRoute.getHopCount());
            forwardRoute.setNextHop(reverseRoute.getPrevHop());
            if (Node.updateRouteEntry(forwardRoute)) {
                Node.updateRouteLifetimeRREQ(forwardRoute.getDestAddr());
            }
            if ((Node.getSeqNum() + 1) == rreq.getDestSeqNum()) {
                Node.incrementSeqNum();
            }
            var packet = new RREP(Node.MY_ROUTE_TIMEOUT, rreq.getDestAddr(), Node.getSeqNum(), rreq.getOriAddr(), (byte) 0);
            return SendPacket.RREP.setPacket(packet.getBytes()).setNextHop(reverseRoute.getPrevHop());
        } else if (route != null) {
            var packet = new RREP((int) Math.abs((route.getLifetime() - System.currentTimeMillis()) % 0x3ffff), rreq.getDestAddr(), route.getSeq(), rreq.getOriAddr(), route.getHopCount());
            return SendPacket.RREP.setPacket(packet.getBytes()).setNextHop(reverseRoute.getPrevHop());
        }
        if (Node.updateReverseRouteEntry(reverseRoute)) {
            Node.updateReverseRouteLifetimeRREQ(reverseRoute.getSourceAddr());
        }
        return SendPacket.RREQ.setPacket(rreq.getBytes()).setNextHop(Parser.parseAddrToBytes("FFFF"));
    }

    private static SendPacket handleRREP(byte[] decodedPacket, byte[] prevHop) {
        RREP rrep;
        try {
            rrep = new RREP(decodedPacket);
            MyLogger.info("\n\nFrom " + Parser.parseBytesToAddr(prevHop) + "\n" + rrep + "\n");
        } catch (Exception e) {
            MyLogger.warn(e.getMessage());
            return null;
        }

        rrep.incrementHopCount();
        var forwardRouteToPrevHop = new ForwardRoute(prevHop, null, (byte) 1, (byte) 0x40, prevHop, false);
        var forwardRoute = new ForwardRoute(rrep.getDestAddr(), rrep.getOriAddr(), rrep.getHopCount(), rrep.getDestSeqNum(), prevHop, true);

        if (Node.updateRouteEntry(forwardRouteToPrevHop)) {
            Node.updateRouteLifetimeRREP(forwardRouteToPrevHop.getDestAddr(), rrep.getLifetime());
        }
        if (Node.updateRouteEntry(forwardRoute)) {
            Node.updateRouteLifetimeRREP(forwardRoute.getDestAddr(), rrep.getLifetime());
        }


        if (Arrays.equals(rrep.getOriAddr(), Node.getADDR())) {
            return null;
        }
        var reverseRoute = Node.findReverseRoute(rrep.getOriAddr());
        if (reverseRoute == null) {
            return null;
        }
        return SendPacket.RREP.setPacket(rrep.getBytes()).setNextHop(reverseRoute.getPrevHop());
    }


}
