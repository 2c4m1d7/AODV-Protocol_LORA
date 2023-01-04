package connector;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Scanner;

import static java.lang.System.*;
import static java.lang.Thread.sleep;

/**
 * @author Alexander Schmidt, 578358
 * <p>
 * AT+CFG=433920000,5,6,10,4,1,0,0,0,0,3000,8,4
 */
public class Main {
    static Scanner scanner = new Scanner(in);
    static final SerialPortDataListener listener = getListener();
    static TestConnection connection = null;

    public static void main(String[] args) {


//        Connection connection = null;
        while (true) {
            if (connection == null) {
                connection = setConnection();
            } else {

                var m = scanner.nextLine();
                try {
                    if (m.length() > 0 && !connection.send(m)) {
                        connection = null;
                    }
                } catch (IOException e) {
                    err.println("Failed");
                    connection = null;
                }
            }
        }
    }

    private static TestConnection setConnection() {
        var ports = TestConnection.getPorts();
        var port = SerialPort.getCommPort("/dev/ttys008");

        for (int i = 0; i < ports.length; i++) {
            out.println(i + ". " + ports[i].getDescriptivePortName());
        }
        out.print("Choose port: ");
        var num = scanner.nextInt();
        if (num < 0 || num >= ports.length) {
            return null;
        }

//        Connection connection = new Connection(ports[num]);
        connection = new TestConnection(port);
        if (connection.connect(listener)) {
            out.println("Opened port: " + connection.port().getDescriptivePortName());
            return connection;
        } else {
            return null;
        }
    }

    public static SerialPortDataListener getListener() {
        return new SerialPortDataListener() {

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
                event.getSerialPort().readBytes(buffer, buffer.length);
                var m = StringUtils.substringBefore(new String(buffer), "\r");
                buffer = m.getBytes();
                //test
                if (m.equals("AT+ADDR?")) {
                    try {
                        connection.send("AT,000A,OK");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (!m.contains("AT")){
                    try {
//                        var decoded = Base64.getDecoder().decode(buffer);
//                        var converted = Converter.convertDecoded(decoded);
//                        out.println(Arrays.toString(converted));
                    } catch (Exception e) {
                        err.println(e);
                    }

                }
            }
        };
    }
//test data
    // LR,000D,12,BBACAAEAAAMI

}