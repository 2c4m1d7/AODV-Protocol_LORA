import com.fazecast.jSerialComm.SerialPort;
import model.SendPacket;
import utils.Utils;

import java.util.*;

import static java.lang.System.*;
import static java.lang.System.out;

public class Main {
    static Scanner scanner = new Scanner(in);
    public static App app = null;

    public static void main(String[] args) {

        Set<SendPacket> sendPackets = new HashSet<>();
        var sendP = SendPacket.RREQ.setPacket(new byte[]{1, 2, 3}).setNextHop(new byte[]{1, 2, 3});
        ;
        sendPackets.add(sendP);
        out.println(sendPackets.contains(sendP));

        var sendP2 = SendPacket.RREQ.setPacket(new byte[]{1, 2, 3}).setNextHop(new byte[]{1, 2, 3});
        out.println(sendPackets.contains(sendP2));

        sendPackets.add(sendP2);
        out.println(sendPackets.size());


        int current =  100;
        for (int i = 0; i <= 255; i++) {

            var a = (i % 0x100);
            out.println(a);
//            out.println(i+": singed: "+ (Utils.compareSeqNums(i,current)>0) +" "+(a>0 || Byte.compareUnsigned((byte) i, (byte) current) >0));

        }

        if (args.length > 0) {
            return;
        }
        Connection connection = null;
//        App app = null;
        while (true) {
            if (connection == null) {
                connection = setConnection();
                app = new App(connection);
            } else {
                app.start();
            }
        }
//        connection.stop();
    }


    private static Connection setConnection() {
        Connection connection;
        var port = SerialPort.getCommPort("/dev/ttys001");

        var ports = Connection.getPorts();
        for (int i = 0; i < ports.length; i++) {
            out.println(i + ". " + ports[i].getDescriptivePortName());
        }
        out.print("Choose port: ");
        var num = scanner.nextInt();
        if (num < 0 || num >= ports.length) {
            return null;
        }
//        connection = new Connection(ports[num], null);
        connection = new Connection(port, null);
        if (connection.connect()) {
            out.println("Opened port: " + connection.port().getDescriptivePortName());
            return connection;
        } else {
            return null;
        }
    }
}
