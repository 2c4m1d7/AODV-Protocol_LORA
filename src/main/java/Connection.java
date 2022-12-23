import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public record Connection(SerialPort port) {

    private static OutputStream outputStream;

    public SerialPort port() {
        return port;
    }

    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    public boolean connect() {
        System.out.println(port.isOpen());
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

            try {
               var answer =  MessageHandler.handle(buffer);
                if (answer != null){
                    send("AT+SEND="+ answer.length());
                    send(answer);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
