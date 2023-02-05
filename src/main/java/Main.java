import com.fazecast.jSerialComm.SerialPort;
import model.SendPacket;
import model.SendPacketT;
import utils.Converter;
import utils.MyArrayUtils;
import utils.MyLogger;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.System.*;
import static java.lang.System.out;

public class Main {
    static Scanner scanner = new Scanner(in);
public static  App app = null;
    public static void main(String[] args) {
//        MyLogger.start();

        var sendP = SendPacketT.RREQ.setPacket(new byte[]{1,2,3}).setNextHop(new byte[]{1,2,3});;

        Set<SendPacketT> sendPackets = new HashSet<>();
        sendPackets.add(sendP);
        out.println(sendPackets.contains(sendP));
        var sendP2 = SendPacketT.RREP.setPacket(new byte[]{1,2,3}).setNextHop(new byte[]{1,2,3});
        out.println(sendPackets.contains(sendP2));
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
