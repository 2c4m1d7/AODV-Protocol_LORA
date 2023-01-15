import model.ForwardRoute;
import model.Node;
import model.SendPacket;
import model.ReverseRoute;
import org.apache.commons.lang3.StringUtils;
import packets.RREP;
import packets.RREQ;
import utils.Converter;
import utils.MyArrayUtils;
import utils.Parser;
import utils.Timer;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class MessageHandler {


    public static SendPacket handle(byte[] bytes) {
        var s = StringUtils.substringBefore(new String(bytes), "\r");
        var addr = Optional.ofNullable(StringUtils.substringBetween(s, ",", ",")).orElse("");
        try {
            if (s.contains("LR")) {
                var arr = s.split(",");
                var prevHop = Parser.parseAddrToBytes(arr[1]);
                var data = arr[3];
                var decoded = Base64.getDecoder().decode(data.getBytes());
                var converted = Converter.convertDecoded(decoded);
                return switch (converted[0]) {
                    case 1 -> handleRREQ(converted, prevHop);
                    case 2 -> handleRREP(converted, prevHop);
                    case 0 -> handleUD(decoded);
                    default -> null;
                };
            } else if (addr.length() == 4 && s.contains("AT")) {
                Node.setADDR(Parser.parseAddrToBytes(addr));
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
        return null;
    }

    public static SendPacket handleUD(byte[] decoded) {
        var converted = Converter.userDataPacketDecode(decoded);
        var destAddr = MyArrayUtils.getRangeArray(converted, 1, 4);
        var message = new String(MyArrayUtils.getRangeArray(converted, 5, converted.length - 1));
        if (Arrays.equals(destAddr, Node.getADDR())) {
            System.out.println("Messge form " + Arrays.toString(destAddr) + ": \"" + message + "\"");
            return null;
        }

        var route = Node.findRoute(destAddr);
        if (route != null && route.isValid()) {
            return SendPacket.UD.setPacket(decoded).setNextHop(route.getNextHop());
        } else {
            var rreq = new RREQ(RREQ.Flags.U.getValue(), (byte) 0, Node.useREQid(), destAddr, (byte) 0, Node.getADDR(), Node.getSeqNum());
            return SendPacket.RREQ.setPacket(rreq.getBytes()).setNextHop(Parser.parseAddrToBytes("FFFF"));
        }
    }

    private static SendPacket handleRREQ(byte[] decodedPacket, byte[] prevHop) {
        RREQ rreq;
        try {
            rreq = new RREQ(decodedPacket);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

        rreq.incrementHopCount();
        byte hopCount = Optional.ofNullable(Node.findRoute(rreq.getDestAddr())).or(() -> Optional.of(new ForwardRoute(null, null, (byte) 0, (byte) 0, null, false))).get().getHopCount();
        var forwardRoute = new ForwardRoute(rreq.getDestAddr(), rreq.getOriAddr(), hopCount, rreq.getOriSeqNum(), null, (rreq.getFlag() == RREQ.Flags.U.getValue()));
        var reverseRoute = new ReverseRoute(rreq.getOriAddr(), rreq.getDestAddr(), rreq.getHopCount(), rreq.getDestSeqNum(), prevHop, true);

        if (Node.updateRouteEntry(forwardRoute)) {
            Node.updateRouteLifetimeRREQ(forwardRoute.getDestAddr());
        }
        if (Node.updateReverseRouteEntry(reverseRoute)) {
            Node.updateReverseRouteLifetimeRREQ(reverseRoute.getDestAddr());
        }

        //Update forward route to originator
        Optional.ofNullable(Node.findRoute(rreq.getOriAddr()))
                .ifPresentOrElse(r -> {
                    r.setHopCount(rreq.getHopCount());
                    r.setNextHop(prevHop);
                    if (Node.updateRouteEntry(r)) {
                        Node.updateRouteLifetimeRREQ(r.getDestAddr());
                    }
                }, () -> {
                    var r = new ForwardRoute(rreq.getOriAddr(), rreq.getDestAddr(), rreq.getHopCount(), rreq.getOriSeqNum(), prevHop, (rreq.getFlag() == RREQ.Flags.U.getValue()));
                    if (Node.updateRouteEntry(r)) {
                        Node.updateRouteLifetimeRREQ(r.getDestAddr());
                    }
                });

        if (Node.RREQWasProcessed(rreq)) {
            return null;
        }

        var route = Node.validRouteExists(rreq);
        if (Arrays.equals(rreq.getDestAddr(), Node.getADDR())) {
            forwardRoute.setDestAddr(reverseRoute.getDestAddr()).setSourceAddr(reverseRoute.getSourceAddr()).setHopCount(reverseRoute.getHopCount());
            forwardRoute.setNextHop(reverseRoute.getPrevHop());
            if (Node.updateRouteEntry(forwardRoute)) {
                Node.updateRouteLifetimeRREQ(forwardRoute.getDestAddr());
            }
            if ((Node.getSeqNum() + 1) == rreq.getDestSeqNum()) {
                Node.incrementSeqNum();
            }
            var packet = new RREP(Node.MY_ROUTE_TIMEOUT, rreq.getDestAddr(), Node.getSeqNum(), rreq.getOriAddr(), (byte) 0).getBytes();
            return SendPacket.RREP.setPacket(packet).setNextHop(reverseRoute.getPrevHop());
//            return new RREP(Node.MY_ROUTE_TIMEOUT, rreq.getDestAddr(), Node.getSeqNum(), rreq.getOriAddr(), (byte) 0).getBytes();
        } else if (route != null) {
            var packet = new RREP((int) Math.abs((route.getLifetime() - Timer.getCurrentTimestamp()) % 0x3ffff), rreq.getDestAddr(), route.getSeq(), rreq.getOriAddr(), route.getHopCount()).getBytes();
            return SendPacket.RREP.setPacket(packet).setNextHop(reverseRoute.getPrevHop());
//            return new RREP((int) Math.abs((route.getLifetime() - Timer.getCurrentTimestamp()) % 0x3ffff), rreq.getDestAddr(), route.getSeq(), rreq.getOriAddr(), route.getHopCount()).getBytes();
        }
        return SendPacket.RREQ.setPacket(rreq.getBytes()).setNextHop(Parser.parseAddrToBytes("FFFF"));
//        return rreq.getBytes();
    }

    private static SendPacket handleRREP(byte[] decodedPacket, byte[] prevHop) {
        RREP rrep;
        try {
            rrep = new RREP(decodedPacket);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

        rrep.incrementHopCount();
        var forwardRouteToPrevHop = new ForwardRoute(prevHop, null, (byte) 1, (byte) 0, prevHop, true);
        byte hopCount = Optional.ofNullable(Node.findRoute(rrep.getDestAddr())).or(() -> Optional.of(new ForwardRoute(null, null, (byte) 0, (byte) 0, null, false))).get().getHopCount();
//        var hopCount = Optional.ofNullable(Node.findRoute(rrep.getDestAddr()).getHopCount()).orElse(Node.OVER_MAX);
        var forwardRoute = new ForwardRoute(rrep.getDestAddr(), rrep.getOriAddr(), hopCount, rrep.getDestSeqNum(), prevHop, true);

        if (Node.updateRouteEntry(forwardRouteToPrevHop)) {
            Node.updateRouteLifetimeRREP(forwardRouteToPrevHop.getDestAddr(), rrep.getLifetime());
        }
        if (Node.updateRouteEntry(forwardRoute)) {
            Node.updateRouteLifetimeRREP(forwardRoute.getDestAddr(), rrep.getLifetime());
        }

//        //Update forward route to originator
//        Optional.ofNullable(Node.findRoute(rrep.getOriAddr()))
//                .ifPresentOrElse(r -> {
//                    r.setHopCount(rrep.getHopCount());
//                    r.setNextHop(prevHop);
//                    if (Node.updateRouteEntry(r)) {
//                        Node.updateRouteLifetimeRREP(r.getDestAddr(), rrep.getLifetime());
//                    }
//                }, () -> {
//                    var r = new ForwardRoute(rrep.getOriAddr(), rrep.getDestAddr(), Node.OVER_MAX, (byte) -1, prevHop, (Node.OVER_MAX == RREQ.Flags.U.getValue()));
//                    if (Node.updateRouteEntry(r)) {
//                        Node.updateRouteLifetimeRREP(r.getDestAddr(), rrep.getLifetime());
//                    }
//                });

        if (Arrays.equals(rrep.getDestAddr(), Node.getADDR())) {
            return null;
        }
        var reverseRoute = Node.findReverseRoute(rrep.getDestAddr());
        if (reverseRoute == null) {
            return null;
        }

        return SendPacket.RREP.setPacket(rrep.getBytes()).setNextHop(reverseRoute.getPrevHop());
//        return ArrayUtils.addAll(rrep.getBytes(), reverseRoute.getPrevHop());
    }


}
