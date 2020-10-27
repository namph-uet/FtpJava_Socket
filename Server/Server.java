import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public final static int SOCKET_PORT = 9998;
    public final static int FILE_READER_BUFF = 4096;
    public final static int MESSAGE_BUFF = 4096;

    public static void main(String[] args) {
        // define variable
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        byte[] messageBuff = new byte[MESSAGE_BUFF];
        OutputStream os = null;
        InputStream is;
        ServerSocket listener = null;
        Socket sock = null;
        File requestFile = null;
        String message = null;
        String filename = "";
        boolean servReady = false;
        boolean typingFileName = false;
        boolean startDownload = false;
        boolean haveFile = false;
        boolean wrongSyntax = true;
        String HELLO_CLIENT = "200 Hello Client";
        String NOT_READY = "Type Hello Server to start. ";
        String READY = "210 OK";
        String DOWNLOAD_FILE_OK = "Download file OK";
        String FILE_NOT_FOUND = "File not found";
        String WRONG_SYNTAX = "Wrong syntax";
        String TYPE_START = "Type start to start download";

        try {
            try {
                listener = new ServerSocket(SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            while (true) {
                System.out.println("Waiting for a client...");
                sock = listener.accept();
                System.out.println("Accepted connection : " + sock);
                os = sock.getOutputStream();
                is = sock.getInputStream();

                while (true) {
                    wrongSyntax = true;
                    // read message from client
                    messageBuff = new byte[MESSAGE_BUFF];
                    int messLength = is.read(messageBuff);
                    message = new String(messageBuff);
                    System.out.println("Received a connection from a client: " + message);

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
                            System.out.println(new String(buffer));
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
            e.printStackTrace();
        } finally {
            if (sock != null) {
                try {
                    if (bis != null) bis.close();
                    if (os != null) os.close();
                    if (sock != null) sock.close();
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
