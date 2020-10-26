/// name: Phạm Hoàng Nam
// mssv: 17021164
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public final static int FILE_READER_BUFF = 4096;
    public static void main(String[] args) {

        // Địa chỉ máy chủ.
        final String serverHost = "localhost";
        final int cliPort = 9998;
        Socket socketOfClient = null;
        OutputStream os = null;
        InputStream is = null;
        String response;
        boolean startDownload = false;
        byte[] buffer = new byte[FILE_READER_BUFF];

        try {
            // request connect to server
            socketOfClient = new Socket(serverHost, cliPort);

            // create stream
            os = socketOfClient.getOutputStream();

            is = socketOfClient.getInputStream();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverHost);
            return;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverHost);
            return;
        }

        System.out.println("Input a message to the echo server (QUIT for exit): ");
        String input;
        Scanner scanner = new Scanner(System.in);

        try {
            // Ghi dữ liệu vào luồng đầu ra của Socket tại Client.
            while (true) {
                input = scanner.nextLine();
                os.write(input.getBytes());
                os.flush();

                if(input.toUpperCase().equals("QUIT")) break;
                int nread = is.read(buffer);
                System.out.println("Message received from server: " + new String(buffer));

                if(input.equalsIgnoreCase("start")) startDownload = true;
                while (nread > 0 && startDownload) {
                    System.out.println("Message received from server: " + new String(buffer));
                    os.write(buffer,0,0);
                    nread = is.read(buffer, 0,FILE_READER_BUFF);
//                    System.out.println("Message received from server: " + new String(buffer));
                }
            }

            os.close();
            is.close();
            socketOfClient.close();
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        }
        catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}

