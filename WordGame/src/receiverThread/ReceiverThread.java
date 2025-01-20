package receiverThread;

import client.Client;

import java.io.*;

public class ReceiverThread extends Thread {
    private Client client;
    private BufferedReader in;

    public ReceiverThread(Client client, BufferedReader in) {
        this.client = client;
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                client.processServerMessage(serverMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


