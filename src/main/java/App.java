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
        System.out.println("Addres " + Arrays.toString(Node.getADDR()));
        System.out.println(Node.getInfo());
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
            try {
                addr = Parser.parseAddrToBytes(input);
            }catch (IllegalArgumentException e1){
                System.err.println(e1);
                return;
            }
        }

        userData.setDestAddr(addr);

        System.out.print("Write message:");
        var message = scanner.next();
        userData.setMessage(message);

        SendPacket sendPacket;
        var i = 0;
        do {
            i++;
            sendPacket = MessageHandler.handleUD(userData.getBytes(), null);
            try {
                sendPacket(sendPacket);
                if (sendPacket == SendPacket.RREQ) {
                    Thread.sleep(((long) i * i * Node.NET_TRAVERSAL_TIME));
                } else if (sendPacket == SendPacket.UD) {
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (Node.RREQ_RETRIES != i);
        if (sendPacket != SendPacket.UD) {
            sendPacket = MessageHandler.handleUD(userData.getBytes(), null);
            if (sendPacket == SendPacket.UD){
                sendPacket(sendPacket);
            }else {
                System.err.println("Not sent");
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

