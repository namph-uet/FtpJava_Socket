import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public final static int FILE_READER_BUFF = 4096;
    public final static int MESSAGE_BUFF = 4096;

    public static void main(String[] args) {

        // Địa chỉ máy chủ.
        final String serverHost = "localhost";
        final int cliPort = 9998;
        Socket socketOfClient = null;
        OutputStream os = null;
        InputStream is = null;
        boolean startDownload = false;
        byte[] buffer = new byte[FILE_READER_BUFF];
        FileOutputStream fout = null;
        int fileLength = 0;
        final String TYPE_START = "Type start to start download";

        try {
            fout = new FileOutputStream("download.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

        String input;
        Scanner scanner = new Scanner(System.in);

        try {

            while (true) {
                System.out.println("Input a message to the echo server (QUIT for exit): ");

                buffer = new byte[MESSAGE_BUFF];
                input = scanner.nextLine();
                os.write(input.getBytes());
                os.flush();

                if(input.toUpperCase().equals("QUIT")) break;
                if(input.equalsIgnoreCase("start")) startDownload = true;

                if(!startDownload) {
                    is.read(buffer);
                    System.out.println("Message received from server: " + new String(buffer));
                }

                String response = new String(buffer).replaceAll("\r\n", "").replaceAll("\u0000.*", "");

                if(response.split(",").length == 2 && response.split(",")[1].equalsIgnoreCase(TYPE_START)) {
                    fileLength = Integer.parseInt(response.split(",")[0].split(":")[1]);
                    System.out.println(fileLength);
                }

                if(startDownload) {
                    int remaining = fileLength;
                    int read = 0;
                    while ((read = is.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        fout.write(buffer);
                        buffer = new byte[FILE_READER_BUFF];
                        remaining -= read;
                    }
                    startDownload = false;
                    System.out.println("download is done!");
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
