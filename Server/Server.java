// name: Phạm Hoàng Nam
// mssv: 17021164

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public final static int SOCKET_PORT = 9999;

    public static void main(String[] args) {
        OutputStream os = null;
        InputStream is;
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(SOCKET_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Socket sock = null;
                System.out.println("Waiting for a client...");
                sock = listener.accept();
                System.out.println("Accepted connection : " + sock);
                os = sock.getOutputStream();
                is = sock.getInputStream();

                // create a new thread object
                Thread t = new ClientHandler(sock, is, os);

                // Invoking the start() method
                t.start();

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}

// ClientHandler class
class ClientHandler extends Thread {
    final InputStream is;
    final OutputStream os;
    final Socket sock;

    public final static int FILE_READER_BUFF = 4096;
    public final static int MESSAGE_BUFF = 4096;

    // Constructor
    public ClientHandler(Socket sock, InputStream is, OutputStream os) {
        this.sock = sock;
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {

        // define variable
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        byte[] messageBuff = new byte[MESSAGE_BUFF];
        File requestFile = null;
        String message = null;
        String filename = "";
        boolean servReady = false;
        boolean typingFileName = false;
        boolean haveFile = false;
        boolean wrongSyntax = true;
        String HELLO_CLIENT = "200 Hello Client";
        String NOT_READY = "Type Hello Server to start. ";
        String DOWNLOAD_FILE_OK = "Download file OK";
        String FILE_NOT_FOUND = "File not found";
        String WRONG_SYNTAX = "Wrong syntax";
        String TYPE_START = "Type start to start download";

        try {


            while (true) {
                while (true) {
                    wrongSyntax = true;
                    // read message from client
                    messageBuff = new byte[MESSAGE_BUFF];
                    int messLength = is.read(messageBuff);
                    message = new String(messageBuff);
                    System.out.println("Received a connection from a client" +sock+ ": " + message);

                    message = message.replaceAll("\r\n", "").replaceAll("\u0000.*", "");

                    if (!servReady && !message.equalsIgnoreCase("HELLO SERVER")) {
                        os.write(NOT_READY.getBytes());
                        wrongSyntax = false;
                    }

                    if (message.equalsIgnoreCase("HELLO SERVER")) {
                        servReady = true;
                        wrongSyntax = false;
                        os.write(HELLO_CLIENT.getBytes());
                    } else if (message.equalsIgnoreCase("DOWNLOAD FILE")) {
                        os.write(DOWNLOAD_FILE_OK.getBytes());
                        typingFileName = true;
                        wrongSyntax = false;
                    } else if (typingFileName) {
                        try {
                            filename = message;
                            requestFile = new File(filename);

                            fis = new FileInputStream(requestFile);

                            System.out.println("File: " + filename);
                            System.out.println("Size: " + requestFile.length());
                            typingFileName = false;
                            haveFile = true;
                            String response = "file size:" + String.valueOf(requestFile.length()) + "," + TYPE_START;
                            System.out.println("file name: " + filename);
                            System.out.println("file size: " + requestFile.length());
                            os.write(response.getBytes());
                            wrongSyntax = false;

                        } catch (FileNotFoundException ex) {
                            System.out.println("file not found");
                            os.write(FILE_NOT_FOUND.getBytes());
                            wrongSyntax = false;
                        }
                    }

                    if (message.equalsIgnoreCase("START") && haveFile) {
                        byte[] buffer = new byte[FILE_READER_BUFF];
                        wrongSyntax = false;
                        int count = 0;

                        requestFile = new File(filename);
                        fis = new FileInputStream(requestFile);

                        while (fis.read(buffer) > 0) {
                            os.write(buffer);
                            os.flush();
                            buffer = new byte[FILE_READER_BUFF];
                        }

                        wrongSyntax = false;
                    }

                    if (message.equalsIgnoreCase("quit")) break;

                    if (wrongSyntax) {
                        os.write(WRONG_SYNTAX.getBytes());
                        wrongSyntax = false;
                    }
                    os.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnect to client");
        } finally {
            if (sock != null) {
                try {
                    bis.close();
                    os.close();
                    sock.close();
                } catch (IOException | NullPointerException e) {
                    System.out.println("Close connection to client " + sock);
                }
            }
        }
    }
}

