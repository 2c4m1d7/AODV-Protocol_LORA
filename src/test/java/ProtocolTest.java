import model.ForwardRoute;
import model.Node;
import model.ReverseRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import packets.RREP;
import packets.RREQ;
import utils.Converter;
import utils.Timer;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {
//    Connection protocol;
//    TestConnection testConnection;

    @BeforeEach
    void setup() {
        Node.clearForTest();
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

    @Test
    void testAddrSetup() {
//        testConnection.send("AT,000A,OK");
        MessageHandler.handle("AT,000A,OK".getBytes());
//        sleep(2000);
        assertArrayEquals(new byte[]{0, 0, 0, 10}, Node.getADDR());
    }

    @Test
    void testRREQ() {
        MessageHandler.handle("AT,000A,OK".getBytes());
        assertArrayEquals(new byte[]{0, 0, 0, 10}, Node.getADDR());

        var expectedRREQ = new RREQ((byte) 1, (byte) 1, (byte) 2, new byte[]{0, 0, 0, 1}, (byte) 0, new byte[]{0, 0, 0, 3}, (byte) 8).getBytes();
        var sendPacket = Objects.requireNonNull(MessageHandler.handle("LR,000D,12,BBACAAEAAAMI".getBytes())); //1,1,0,2; 0,0,0,1,0; 0,0,0,3,8
        var rreq = new RREQ(sendPacket.getPacket()).getBytes();
        assertArrayEquals(expectedRREQ, rreq);

        var expectedReverseRoute = new ReverseRoute(new byte[]{0, 0, 0, 3}, new byte[]{0, 0, 0, 1}, (byte) 1, (byte) -1, new byte[]{0, 0, 0, 13}, true);
        var reverseRoute = Node.findReverseRoute(expectedReverseRoute.getDestAddr());
        assertEquals(expectedReverseRoute, reverseRoute);

        var expectedForwardRoute = new ForwardRoute(new byte[]{0, 0, 0, 1}, new byte[]{0, 0, 0, 3}, (byte) 0x40, (byte) -1, null, false);
        var forwardRoute = Node.findRoute(expectedForwardRoute.getDestAddr());
        assertEquals(expectedForwardRoute, forwardRoute);

        var expectedForwardRouteReverse = new ForwardRoute(new byte[]{0, 0, 0, 3}, new byte[]{0, 0, 0, 1}, (byte) 1, (byte) -1, new byte[]{0, 0, 0, 13}, false);
        var forwardRouteReverse = Node.findRoute(expectedForwardRouteReverse.getDestAddr());
        assertEquals(expectedForwardRouteReverse, forwardRouteReverse);
    }

    @Test
    void testRREPNoReverseRoute() {
        MessageHandler.handle("AT,000A,OK".getBytes());
        assertArrayEquals(new byte[]{0, 0, 0, 10}, Node.getADDR());

        var expectedRREP = new RREP((byte) (Timer.getCurrentTimestamp() % 0x3ffff), new byte[]{0, 0, 0, 1}, (byte) 2, new byte[]{0, 0, 0, 3}, (byte) 0);
        var sendPacket = MessageHandler.handle("LR,000D,12,CABmAAECAAMA".getBytes()); // 2; 102; 0,0,0,1; 2; 0,0,0,3; 0
        assertNull(sendPacket);

        var expectedForwardRoute = new ForwardRoute(expectedRREP.getDestAddr(), expectedRREP.getOriAddr(), expectedRREP.getHopCount(), expectedRREP.getDestSeqNum(), null, true);
        var forwardRoute = Node.findRoute(expectedForwardRoute.getDestAddr());
        assertEquals(expectedForwardRoute, forwardRoute);

        var expectedForwardRouteReverse = new ForwardRoute(expectedRREP.getOriAddr(), expectedRREP.getDestAddr(), expectedRREP.getHopCount(), expectedRREP.getDestSeqNum(), new byte[]{0, 0, 0, 13}, true);
        var forwardRouteReverse = Node.findRoute(expectedForwardRouteReverse.getDestAddr());
        assertEquals(expectedForwardRouteReverse, forwardRouteReverse);
    }

    //todo test return correct rrep

    @Test
    void testReturnCorrectRREP() {
        var rreq = "BBACAAEAAAMI";
        Node.setADDR(new byte[]{0, 0, 0, 1});
        var sendPacket = Objects.requireNonNull(MessageHandler.handle(("LR,0003,12," + rreq).getBytes()));
        var rrep = new RREP(sendPacket.getPacket());
        var expectedRREP = new RREP(6000, Node.getADDR(), (byte) 0, new byte[]{0, 0, 0, 3}, (byte) 0);
//        assertArrayEquals(expectedRREP.getBytes(), rrep.getBytes());
        assertArrayEquals(expectedRREP.getDestAddr(), rrep.getDestAddr());
        assertArrayEquals(expectedRREP.getOriAddr(), rrep.getOriAddr());
        assertEquals(expectedRREP.getHopCount(), rrep.getHopCount());
        assertEquals(expectedRREP.getLifetime(), rrep.getLifetime());

    }

}
