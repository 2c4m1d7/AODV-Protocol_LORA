import com.fazecast.jSerialComm.SerialPort;
import connector.Main;
import connector.TestConnection;
import model.ForwardRoute;
import model.Node;
import model.ReverseRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import packets.RREQ;

import java.io.IOException;
import java.util.Objects;

import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {
//    Connection protocol;
//    TestConnection testConnection;

    @BeforeEach
    void setup() {
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
    void test(){
        MessageHandler.handle("AT,000A,OK".getBytes());
        assertArrayEquals(new byte[]{0, 0, 0, 10}, Node.getADDR());

        var expectedRREQ = new RREQ((byte) 1, (byte) 1, (byte) 2, new byte[]{0,0,0,1}, (byte) 0, new byte[]{0,0,0,3}, (byte) 8).getBytes();
        var rreq = MessageHandler.handle("LR,000D,12,BBACAAEAAAMI".getBytes()); //1,1,0,2; 0,0,0,1,0; 0,0,0,3,8
        assertArrayEquals(expectedRREQ, rreq);

        var expectedReverseRoute = new ReverseRoute(new byte[]{0,0,0,3}, new byte[]{0,0,0,1}, (byte) 1, (byte) -1,new byte[]{0,0,0,13});
        var reverseRoute = Node.findReverseRoute(expectedReverseRoute.getDestAddr());
        assertEquals(expectedReverseRoute, reverseRoute);

        var expectedForwardRoute = new ForwardRoute(new byte[]{0,0,0,1}, new byte[]{0,0,0,3}, (byte) 0x40, (byte) -1, null);
        var forwardRoute = Node.findRoute(expectedForwardRoute.getDestAddr());
        assertEquals(expectedForwardRoute, forwardRoute);

        var expectedForwardRouteReverse = new ForwardRoute(new byte[]{0,0,0,3}, new byte[]{0,0,0,1}, (byte) 1, (byte) -1, new byte[]{0,0,0,13});
        var forwardRouteReverse = Node.findRoute(expectedForwardRouteReverse.getDestAddr());
        assertEquals(expectedForwardRouteReverse, forwardRouteReverse);
    }

}
