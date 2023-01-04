import model.ForwardRoute;
import model.Node;
import model.ReverseRoute;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import packets.RREP;
import packets.RREQ;
import utils.Converter;
import utils.Parser;
import utils.Timer;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class MessageHandler {


    public static byte[] handle(byte[] bytes) {
        var s = StringUtils.substringBefore(new String(bytes), "\r");
        var addr = Optional.ofNullable(StringUtils.substringBetween(s, ",", ",")).orElse("");
        if (s.contains("LR")) {
            var arr = s.split(",");
            var prevHop = Parser.parseAddrToBytes(arr[1]);
            var data = arr[3];
            var decoded = Base64.getDecoder().decode(data.getBytes());
            decoded = Converter.convertDecoded(decoded);
            return switch (decoded[0]) {
                case 1 -> handleRREQ(decoded, prevHop);
                case 2 -> handleRREP(decoded, prevHop);
                default -> null;
            };
        } else if (addr.length() == 4 && s.contains("AT")) {
            Node.setADDR(Parser.parseAddrToBytes(addr));
        }
        return null;
    }

    private static byte[] handleRREQ(byte[] decodedPacket, byte[] prevHop) {
        var rreq = new RREQ(decodedPacket);
        rreq.incrementHopCount();
        byte hopCount = Optional.ofNullable(Node.findRoute(rreq.getDestAddr())).or(() -> Optional.of(new ForwardRoute(null, null, Node.OVER_MAX, (byte) 0, null))).get().getHopCount();
        var forwardRoute = new ForwardRoute(rreq.getDestAddr(), rreq.getOriAddr(), hopCount, (rreq.getFlag() == RREQ.Flags.U.getValue()) ? -1 : rreq.getOriSeqNum(), null); // todo: OVER_MAX ?
        var reverseRoute = new ReverseRoute(rreq.getOriAddr(), rreq.getDestAddr(), rreq.getHopCount(), rreq.getDestSeqNum(), prevHop);

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
                    var r = new ForwardRoute(rreq.getOriAddr(), rreq.getDestAddr(), rreq.getHopCount(), (rreq.getFlag() == RREQ.Flags.U.getValue()) ? -1 : rreq.getOriSeqNum(), prevHop);
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
            return new RREP(Node.MY_ROUTE_TIMEOUT, rreq.getOriAddr(), Node.getSeqNum(), rreq.getDestAddr(), (byte) 0).getBytes();
        } else if (route != null) {
            return new RREP((int) Math.abs((route.getLifetime() - Timer.getCurrentTimestamp()) % 0x3ffff), rreq.getOriAddr(), route.getSeq(), rreq.getDestAddr(), route.getHopCount()).getBytes();
        }

        return rreq.getBytes();
    }

    private static byte[] handleRREP(byte[] decodedPacket, byte[] prevHop) {
        var rrep = new RREP(decodedPacket);
        rrep.incrementHopCount();
        var forwardRouteToPrevHop = new ForwardRoute(prevHop, null, (byte) 1, (byte) 0, prevHop);
        var hopCount = Optional.of(Node.findRoute(rrep.getDestAddr()).getHopCount()).orElse(Node.OVER_MAX);
        var forwardRoute = new ForwardRoute(rrep.getDestAddr(), rrep.getOriAddr(), hopCount, rrep.getDestSeqNum(), null);

        if (Node.updateRouteEntry(forwardRouteToPrevHop)) {
            Node.updateRouteLifetimeRREP(forwardRouteToPrevHop.getDestAddr(), rrep.getLifetime());
        }
        if (Node.updateRouteEntry(forwardRoute)) {
            Node.updateRouteLifetimeRREP(forwardRoute.getDestAddr(), rrep.getLifetime());
        }

        //Update forward route to originator
        Optional.of(Node.findRoute(rrep.getOriAddr()))
                .ifPresentOrElse(r -> {
                    r.setHopCount(rrep.getHopCount());
                    r.setNextHop(prevHop);
                    if (Node.updateRouteEntry(r)) {
                        Node.updateRouteLifetimeRREP(r.getDestAddr(), rrep.getLifetime());
                    }
                }, () -> {
                    var r = new ForwardRoute(rrep.getOriAddr(), rrep.getDestAddr(), Node.OVER_MAX, (byte) -1, prevHop);
                    if (Node.updateRouteEntry(r)) {
                        Node.updateRouteLifetimeRREP(r.getDestAddr(), rrep.getLifetime());
                    }
                });

        if (Arrays.equals(rrep.getDestAddr(), Node.getADDR())) {
            return null;
        }
        var reverseRoute = Node.findReverseRoute(rrep.getDestAddr());

        //todo send rrep to oriAddr from reverse table ? https://github.com/2c4m1d7/AODV-Protocol_LORA#processing-rreps
        return ArrayUtils.addAll(rrep.getBytes(), reverseRoute.getPrevHop());
    }


}
