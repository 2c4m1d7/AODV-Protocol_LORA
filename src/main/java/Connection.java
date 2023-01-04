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
import java.util.HashSet;
import java.util.Set;

//AT+DEST=FFFF
//AT+CFG=433920000,5,6,10,4,1,0,0,0,0,3000,8,4
//AT+RX
//AT+SEND=14
//BBACAAEAAAMI
public record Connection(SerialPort port) {

    private static boolean connected = false;

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
        var listener = new Listener(this);
        port.addDataListener(listener);
        try {
            synchronized (this) {
                send("AT+ADDR?");
                this.wait();

                send("AT+DEST=FFFF");
                this.wait();

                send("AT+CFG=433920000,5,6,10,4,1,0,0,0,0,3000,8,4");
                this.wait();

                send("AT+RX");
                this.wait();
            }
        } catch (IOException e) {
            System.err.println(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        connected = true;
        return true;
    }

    public boolean send(String m) throws IOException {
        if (outputStream == null) {
            return false;
        }
        outputStream.write((m + "\r\n").getBytes());
//        outputStream.flush();
        return true;
    }

    public MyThread sendPacket(byte[] answer) {
        return new MyThread(answer);
    }

    public void stop() {
        port.closePort();
    }

    private class Listener implements SerialPortDataListener {

        private final Set<byte[]> answers = new HashSet<>();

        private MyThread myThread = new MyThread(null);
        private final Set<String> loraResponses = Set.of("AT,OK", "AT,SENDED");
        private String tmp = "";
        private final Connection connection;

        public Listener(Connection connection) {
            this.connection = connection;
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
            event.getSerialPort().readBytes(buffer, buffer.length);

            tmp += new String(buffer);
            if (!tmp.contains("\r\n")) {
                return;
            } else {
                buffer = tmp.getBytes();
                tmp = "";
            }

            var response = StringUtils.substringBefore(new String(buffer), "\r");

            if (loraResponses.contains(response) || response.contains("OK")) {
                synchronized (connection) {
                    connection.notify();
                }
                if (myThread != null) {
                    synchronized (myThread) {
                        myThread.notify();
                    }
                }
            }

            if (!connected){
                return;
            }

            //test
            System.out.println(new String(buffer));
            printInfo();
            System.out.println("-------------------------");

            var answer = MessageHandler.handle(buffer);
            if (answer != null) {
                answers.add(answer);
            }

            if (answers.size() != 0 && !myThread.inProcess()){
                var o = (byte[]) answers.toArray()[0];
                answers.remove(o);
                myThread = sendPacket(o);
            }
//            if (answer == null && answers.size() != 0 && !myThread.inProcess()) {
//                var o = (byte[]) answers.toArray()[0];
//                answers.remove(o);
//                myThread = sendPacket(o);
//            } else if (myThread.inProcess() && answer != null) {
//                answers.add(answer);
//            } else {
//                if (answers.size() != 0) {
//                    var o = (byte[]) answers.toArray()[0];
//                    answers.remove(o);
//                    myThread = sendPacket(o);
//                    if (answer != null)
//                        answers.add(answer);
//                } else if (answer != null) {
//                    myThread = sendPacket(answer);
//                }
//            }
            printInfo();
            System.out.println("***************************");


        }

        private void printInfo() {
            System.out.println(answers);
            System.out.println("Thread in process : " + myThread.inProcess());
            System.out.println("My address = " + Arrays.toString(Node.getADDR()));
            System.out.println(Node.getInfo());
        }
    }


    private class MyThread implements Runnable {
        private final Thread t;
        byte[] answer;

        public MyThread(byte[] answer) {
            this.answer = answer;
            t = new Thread(this);
            t.start();
        }

        public boolean inProcess() {
            return t.isAlive();
        }

        @Override
        public void run() {

            try {
                if (answer != null) {
                    synchronized (this) {
                        if (answer.length > 12) {
                            byte[] addr = MyArrayUtils.getRangeArray(answer, answer.length - 5, answer.length - 1);

                            send("AT+DEST=" + Parser.parseBytesToAddr(addr));
                            this.wait();
                            answer = ArrayUtils.removeAll(answer, answer.length - 1, answer.length - 2, answer.length - 3, answer.length - 4);
                        }
                        answer = Converter.prepareForEncoding(answer);
                        answer = Base64.getEncoder().encode(answer);

                        send("AT+SEND=" + answer.length);
                        this.wait();
                        send(new String(answer));
                        this.wait();
                        send("AT+DEST=FFFF");
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.out.println(e);
            }

        }

    }
}
