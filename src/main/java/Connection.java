import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import model.Node;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import utils.Converter;
import utils.MyArrayUtils;
import utils.Parser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static java.lang.Thread.*;

public record Connection(SerialPort port) {

    private static OutputStream outputStream;

    public SerialPort port() {
        return port;
    }

    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    public boolean connect() {
        if (!port.isOpen() && !port.openPort(2000)) {
            return false;
        }
        outputStream = port.getOutputStream();
        port.addDataListener(new Listener());
        try {
            send("AT+ADDR?");
        } catch (IOException e) {
            System.err.println("Address is not available");
        }
        return true;
    }

    public boolean send(String m) throws IOException {
        if (outputStream == null) {
            return false;
        }
        outputStream.write((m + "\r\n").getBytes());
        outputStream.flush();
        return true;
    }

    private class Listener implements SerialPortDataListener {

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
            event.getSerialPort().readBytes(buffer, buffer.length);
            //test
            printInfo();
            try {
                var answer = MessageHandler.handle(buffer);
                if (answer != null) {
                    if (answer.length > 12) {
                        byte[] addr = MyArrayUtils.getRangeArray(answer, answer.length - 5, answer.length - 1);

                        send("AT+DEST=" + Parser.parseBytesToAddr(addr));
                        sleep(1000);
                        answer = ArrayUtils.removeAll(answer, answer.length - 1, answer.length - 2, answer.length - 3, answer.length - 4);
                    }
                    answer = Converter.prepareForEncoding(answer);
                    answer = Base64.getEncoder().encode(answer);

                    send("AT+SEND=" + new String(answer).length() + "\r\n".length());
                    sleep(1000);
                    send(new String(answer));
                }
                System.out.println("-------------------------");
                printInfo();
                System.out.println("***************************");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        private void printInfo() {
            System.out.println("My address = " + Arrays.toString(Node.getADDR()));
            System.out.println(Node.getInfo());
        }
    }
}
