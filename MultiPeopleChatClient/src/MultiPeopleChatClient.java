
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MultiPeopleChatClient extends JFrame implements ActionListener {

    private JTextField connectIP;
    private JTextField tfMyName;
    private JScrollBar chatWindowBar;
    private JButton connect;
    private JLabel myIP;
    private JLabel lbMyName;
    private JLabel targetIP;
    private JTextArea chatWindow;
    private JTextField chatInputWindow;
    private JButton send;
    private Socket clientSocket;
    private String name;
    private BufferedReader reader;
    private PrintStream writer;

    public MultiPeopleChatClient() throws UnknownHostException {
        //UI設計
        super("多人聊天室");
        connectIP = new JTextField("127.0.0.1", 12);
        tfMyName = new JTextField("帥哥", 8);
        chatInputWindow = new JTextField(25);
        connect = new JButton("開始連線");
        send = new JButton("送出");
        send.setEnabled(false);
        myIP = new JLabel("我的IP : ");
        myIP.setFont(new Font("標楷體", Font.BOLD, 24));
        lbMyName = new JLabel("我的名字 : ");
        targetIP = new JLabel("連線IP : ");
        chatWindow = new JTextArea(15, 50);
        JPanel panel = new JPanel();
        JPanel panel2 = new JPanel();
        panel2.add(myIP);
        panel.add(lbMyName);
        panel.add(tfMyName);
        panel.add(targetIP);
        panel.add(connectIP);
        panel.add(connect);
        panel.setPreferredSize(new Dimension(0, 50));
        add(panel2, BorderLayout.SOUTH);
        add(panel, BorderLayout.NORTH);
        chatWindow.setEditable(false);
        chatWindow.setLineWrap(true);
        chatWindow.setWrapStyleWord(true);
        JScrollPane jsp = new JScrollPane(chatWindow);
        // 取得scrollBar
        chatWindowBar = jsp.getVerticalScrollBar();
        JPanel panel3 = new JPanel();
        panel3.add(jsp);
        panel3.add(chatInputWindow);
        panel3.add(send);
        add(panel3, BorderLayout.CENTER);
        //////////////////////////////////
        //取得本機IP並顯示
        InetAddress myComputer = InetAddress.getLocalHost();
        myIP.setText(myIP.getText() + myComputer.getHostAddress());
        //設定連線按鈕功能
        connect.addActionListener(this);
        //設定訊息送出按鈕功能
        send.addActionListener(this);
        //run server
    }

    public static void main(String[] args) throws UnknownHostException {
        MultiPeopleChatClient client = new MultiPeopleChatClient();
        client.setVisible(true);
        client.setSize(600, 450);
        client.setResizable(false);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private class chatWindowUpdate implements Runnable {

        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    chatWindow.append(message + "\n");
                    // 使jsp每次都自動滾動到最後一行
                    chatWindowBar.setValue(chatWindowBar.getMaximum());
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void clientConnectServer(String ip) {
        try {
            clientSocket = new Socket(ip, 8888);
            writer = new PrintStream(clientSocket.getOutputStream());
            reader= new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
            name = tfMyName.getText();
            chatWindow.append("執行狀態：連接IP: "+clientSocket.getInetAddress().toString()
                    +"的聊天室成功\n");
            chatWindow.append("IP: " + clientSocket.getLocalAddress().toString()
                    + "連到IP: " + clientSocket.getInetAddress() + "\n");
            chatWindow.append("你使用的名字是 :" + name + "\n");
            chatWindow.append("-----------------------------"
                    + "------------------------------------"
                    + "------------------------------------"
                    + "------------------------------------\n");
            tfMyName.setEditable(false);
            connectIP.setEditable(false);
            connect.setEnabled(false);
            send.setEnabled(true);
            new Thread(new chatWindowUpdate()).start();
        } catch (UnknownHostException e) {
            System.out.println("錯誤的IP位置");
            JOptionPane.showMessageDialog(MultiPeopleChatClient.this, "錯誤的IP位置，請重新輸入", "錯誤訊息", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ioe) {
            System.out.println("此IP無法連結");
            JOptionPane.showMessageDialog(MultiPeopleChatClient.this, "此IP位置無法連結，請重新輸入", "錯誤訊息", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("開始連線")) {
            clientConnectServer(connectIP.getText());
        } else if (command.equals("送出")) {
            if (chatInputWindow.getText().equals("")) {
                JOptionPane.showMessageDialog(MultiPeopleChatClient.this, "請輸入聊天內容", "錯誤訊息",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            writer.println(name + ": " + chatInputWindow.getText());
            writer.flush();
            chatInputWindow.setText("");
            // 使jsp每次都自動滾動到最後一行
            chatWindowBar.setValue(chatWindowBar.getMaximum());
        }
    }
}
