
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiPeopleChatServer {

    private List<PrintStream> clientPrintStreams;//放置client端printstream的list
    ServerSocket serverSocket;
    int maxCount = 20;

    public MultiPeopleChatServer() {
        clientPrintStreams = Collections.synchronizedList(new ArrayList<PrintStream>());
    }

    public static void main(String[] args) {
        new MultiPeopleChatServer().runServer();
    }

    public void runServer() {
        try {
            serverSocket = new ServerSocket(8888, maxCount);
            System.out.println("ServerIP為: " + InetAddress.getLocalHost().getHostAddress());
            while (clientPrintStreams.size() < 20) {
                //等待連接串流
                Socket clientSocket = serverSocket.accept();
                //建立IO通道
                PrintStream ps = new PrintStream(clientSocket.getOutputStream());
                //將連接到的printstream加入到list
                clientPrintStreams.add(ps);
                System.out.println("目前有" + clientPrintStreams.size() + "個Client連線，其IP為: "+
                        clientSocket.getInetAddress());
                //建立伺服器主執行緒
                Thread thread = new Thread(new Process(clientSocket, ps));
                //啟動執行緒
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("連接失敗");
        }
    }

    private class Process implements Runnable {

        BufferedReader reader;
        PrintStream ps;
        Socket socket;

        public Process(Socket socket, PrintStream ps) {
            try {
                this.socket = socket;
                this.ps = ps;
                reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            } catch (IOException ioe) {
                System.out.println("Process執行失敗");
            }
        }

        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("收到來自IP: " + socket.getInetAddress() + "的訊息: "
                            + message);
                    tellEveryone(message);
                }
            } catch (IOException ioe) {
                System.out.println("IP: " + socket.getInetAddress() + "已斷線");
                clientPrintStreams.remove(ps);
                System.out.println("目前有" + clientPrintStreams.size() + "個Client連線");
                
            }
        }
    }

    public void tellEveryone(String message) {
        for (PrintStream ps : clientPrintStreams) {
            ps.println(message);
            ps.flush();
        }
    }
}
