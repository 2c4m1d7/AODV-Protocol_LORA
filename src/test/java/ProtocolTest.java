import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import packets.RREP;
import packets.RREQ;
import utils.Converter;

import java.util.Objects;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {
//    Connection protocol;
//    TestConnection testConnection;
    @BeforeEach
    void setup() {
        Node.clearForTest();
        MessageHandler.handle("AT,000A,OK".getBytes());
//        protocol = new Connection(SerialPort.getCommPort("/dev/ttys007"));
//        if (protocol.connect()) {
//            out.println("Opened port: " + protocol.port().getDescriptivePortName());
//        }
//
//        testConnection = new TestConnection(SerialPort.getCommPort("/dev/ttys008"));
//        if (testConnection.connect(Main.getListener())) {
//            out.println("Opened port: " + testConnection.port().getDescriptivePortName());
//        }

    }
//    void start(){
//        MyLogger.start();
//    }

    @Test
    void testAddrSetup() {
        assertArrayEquals(new byte[]{0, 0, 0, 10}, Node.getADDR());
    }

    @Test
    void testRREQ() {
        assertArrayEquals(new byte[]{0, 0, 0, 10}, Node.getADDR());

        var expectedRREQ = new RREQ((byte) 0x20, (byte) 1, (byte) 2, new byte[]{0, 0, 0, 1}, (byte) 0, new byte[]{0, 0, 0, 3}, (byte) 8);
        var sendPacket = Objects.requireNonNull(MessageHandler.handle("LR,000D,12,BgACAAEAAAMI".getBytes())); //1,1,0,2; 0,0,0,1,0; 0,0,0,3,8
        assertEquals(PacketType.RREQ, sendPacket.getType());
        var actualRREQ = new RREQ(Converter.convertDecoded(sendPacket.getPacket()));
        assertEquals(expectedRREQ.getFlag(), actualRREQ.getFlag());
        assertArrayEquals(expectedRREQ.getDestAddr(), actualRREQ.getDestAddr());
        assertEquals(expectedRREQ.getDestSeqNum(), actualRREQ.getDestSeqNum());
        assertEquals(expectedRREQ.getHopCount(), actualRREQ.getHopCount());
        assertEquals(expectedRREQ.getReqId(), actualRREQ.getReqId());
        assertArrayEquals(expectedRREQ.getOriAddr(), actualRREQ.getOriAddr());
        assertEquals(expectedRREQ.getOriSeqNum(), actualRREQ.getOriSeqNum());

        assertArrayEquals(expectedRREQ.getBytes(), sendPacket.getPacket());

        var expectedReverseRoute = new ReverseRoute(new byte[]{0, 0, 0, 1}, new byte[]{0, 0, 0, 3}, (byte) 1,  expectedRREQ.getDestSeqNum(), new byte[]{0, 0, 0, 13}, true);
        var reverseRoute = Node.findReverseRoute(expectedReverseRoute.getSourceAddr());
        assertEquals(expectedReverseRoute, reverseRoute);

        var expectedForwardRoute = new ForwardRoute(new byte[]{0, 0, 0, 1}, new byte[]{0, 0, 0, 3}, (byte) 0x40, expectedRREQ.getDestSeqNum(), null, false);
        var forwardRoute = Node.findRoute(expectedForwardRoute.getDestAddr());
        assertArrayEquals(expectedForwardRoute.getNextHop(), forwardRoute.getNextHop());
        assertArrayEquals(expectedForwardRoute.getDestAddr(), forwardRoute.getDestAddr());
        assertArrayEquals(expectedForwardRoute.getSourceAddr(), forwardRoute.getSourceAddr());
        assertEquals(expectedForwardRoute.getHopCount(), forwardRoute.getHopCount());
        assertEquals(expectedForwardRoute.getSeq(), forwardRoute.getSeq());
        assertFalse(forwardRoute.isValid());

        var expectedForwardRouteReverse = new ForwardRoute(new byte[]{0, 0, 0, 3}, new byte[]{0, 0, 0, 1}, (byte) 1, (byte) 8, new byte[]{0, 0, 0, 13}, false);
        var forwardRouteReverse = Node.findRoute(expectedForwardRouteReverse.getDestAddr());
        assertArrayEquals(expectedForwardRouteReverse.getNextHop(), forwardRouteReverse.getNextHop());
        assertArrayEquals(expectedForwardRouteReverse.getDestAddr(), forwardRouteReverse.getDestAddr());
        assertArrayEquals(expectedForwardRouteReverse.getSourceAddr(), forwardRouteReverse.getSourceAddr());
        assertEquals(expectedForwardRouteReverse.getHopCount(), forwardRouteReverse.getHopCount());
        assertEquals(expectedForwardRouteReverse.getSeq(), forwardRouteReverse.getSeq());
        assertTrue(forwardRouteReverse.isValid());
    }

    @Test
    void testRREPNoReverseRoute() {
        assertArrayEquals(new byte[]{0, 0, 0, 10}, Node.getADDR());

        var expectedRREP = new RREP((byte) (System.currentTimeMillis() % 0x3ffff), new byte[]{0, 0, 0, 1}, (byte) 2, new byte[]{0, 0, 0, 3}, (byte) 1);
        var sendPacket = MessageHandler.handle("LR,000D,12,CABmAAECAAMA".getBytes()); // 2; 102; 0,0,0,1; 2; 0,0,0,3; 0
        assertNull(sendPacket);

        var expectedForwardRoute = new ForwardRoute(expectedRREP.getDestAddr(), expectedRREP.getOriAddr(), expectedRREP.getHopCount(), expectedRREP.getDestSeqNum(), new byte[]{0,0,0,13}, true);
        var forwardRoute = Node.findRoute(expectedForwardRoute.getDestAddr());
        assertArrayEquals(expectedForwardRoute.getNextHop(), forwardRoute.getNextHop());
        assertArrayEquals(expectedForwardRoute.getDestAddr(), forwardRoute.getDestAddr());
        assertArrayEquals(expectedForwardRoute.getSourceAddr(), forwardRoute.getSourceAddr());
        assertEquals(expectedForwardRoute.getHopCount(), forwardRoute.getHopCount());
        assertEquals(expectedForwardRoute.getSeq(), forwardRoute.getSeq());
        assertTrue(forwardRoute.isValid());
    }


    @Test
    void testReturnCorrectRREP() {
        var rreq = "BBACAAoAAAMI";
        var sendPacket = Objects.requireNonNull(MessageHandler.handle(("LR,0003,12," + rreq).getBytes()));
        assertEquals(PacketType.RREP, sendPacket.getType());
        var rrep = new RREP(Converter.convertDecoded(sendPacket.getPacket()));
        var expectedRREP = new RREP(6000, Node.getADDR(), (byte) 0, new byte[]{0, 0, 0, 3}, (byte) 0);
        assertArrayEquals(expectedRREP.getDestAddr(), rrep.getDestAddr());
        assertArrayEquals(expectedRREP.getOriAddr(), rrep.getOriAddr());
        assertEquals(expectedRREP.getHopCount(), rrep.getHopCount());
        assertEquals(expectedRREP.getLifetime(), rrep.getLifetime());
        Node.logInfo();
    }



}
