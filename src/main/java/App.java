import model.Node;
import model.SendPacket;
import packets.UserData;
import utils.Parser;

import java.util.Arrays;
import java.util.Scanner;

public class App {

    private final Connection connection;

    public App(Connection connection) {
        this.connection = connection;
    }

    public void start() {
        var userData = new UserData();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Send data");
        System.out.println("Select an address or write another:");
        var addrs = Node.getValidDestAddrs();
        for (int i = 0; i < addrs.size(); i++) {
            System.out.println(i + ": " + Arrays.toString(addrs.get(i)));
        }
        var input = scanner.nextLine();
        byte[] addr;
        try {
            var num = Integer.parseInt(input);
            if (num >= 0 && num < addrs.size()) {
                addr = addrs.get(num);
            } else {
                addr = Parser.parseAddrToBytes(input);
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            addr = Parser.parseAddrToBytes(input);
        }

        userData.setDestAddr(addr);

        System.out.print("Write message:");
        var message = scanner.next();
        userData.setMessage(message);

        SendPacket sendPacket;
        var i = 0;
        do {
            i++;
            sendPacket = MessageHandler.handleUD(userData.getBytes());
            try {
                sendPacket(sendPacket);
                if (sendPacket == SendPacket.RREQ) {
                    Thread.sleep(((long) i * i * Node.NET_TRAVERSAL_TIME));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (Node.RREQ_RETRIES != i);
        if (sendPacket != SendPacket.UD) {
            sendPacket = MessageHandler.handleUD(userData.getBytes());
            if (sendPacket == SendPacket.UD){
                sendPacket(sendPacket);
            }
        }

    }


    private void sendPacket(SendPacket sendPacket) {
        connection.listener().addSendPacket(sendPacket);
        if (!connection.sendThreadInProcess()) {
            connection.send("AT");
        }
    }
}

