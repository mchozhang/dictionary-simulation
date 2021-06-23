/*
 * the UI window of client
 */

import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class ClientWindow {
    private static final String TITLE = "Dictionary Client";
    private static final int WIDTH = 500;
    private static final int HEIGHT = 480;
    private static final int DEFAULT_PORT = 8000;
    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int SIZE_OF_BUFFER = 8 * 1024;

    private String serverAddress;
    private int port;
    private String word;
    private String des;

    // UI element
    private JFrame frame;
    private JPanel panel;
    private JTextField serverField;
    private JTextField portField;
    private JTextField wordField;
    private JTextField desField;
    private JButton addButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton listButton;
    private JButton clearButton;
    private JTextArea consoleText;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ClientWindow window = new ClientWindow();
                String errorMessage = "";

                // parse arguments, port and server address
                if (args.length == 0) {
                    window.port = DEFAULT_PORT;
                    window.serverAddress = DEFAULT_SERVER;
                } else if (args.length == 2) {
                    int port = parseUtil.parsePort(args[0]);
                    if (port == -1) {
                        window.port = DEFAULT_PORT;
                        errorMessage = ConsoleMessage.INVALID_PORT;
                    } else {
                        window.port = port;
                    }
                    window.serverAddress = args[1];
                } else {
                    System.err.println(ConsoleMessage.WRONG_ARGUMENT);
                    System.exit(1);
                }

                window.initFrame();
                window.createUIElements();

                if (!errorMessage.isEmpty()) {
                    window.showMessage(errorMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * send a request to server
     * use a thread, in case blocking UI
     */
    private void request(String command, String word, String des) {
        Thread thread = new Thread(() -> {
            try {
                Socket socket = new Socket(serverAddress, port);
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                outputStream.writeUTF(requestMessage(command, word, des));
                outputStream.flush();

                byte[] receiveBuffer = new byte[SIZE_OF_BUFFER];
                int bufferLength;
                while ((bufferLength = inputStream.read(receiveBuffer)) >= 0) {
                    String response = new String(receiveBuffer, 2, bufferLength - 2);
                    handleResponseMessage(response);
                }
                closeSocket(socket);
            } catch (IOException e) {
                showMessage(ConsoleMessage.CONNECT_FAILED);
                e.printStackTrace();
            } catch (Exception e2) {
                showMessage(ConsoleMessage.INVALID_REQUEST);
                e2.printStackTrace();
            }
        });
        thread.start();
    }

    /**
     * translate request message into json string
     *
     * @return request message
     */
    private String requestMessage(String command, String word, String des) {
        Map<String, String> map = new HashMap<>();
        map.put("command", command);
        map.put("word", word);
        map.put("des", des);
        return new JSONObject(map).toString();
    }

    /**
     * parse response message, show in console
     * @param responseMsg response json
     */
    private void handleResponseMessage(String responseMsg) {
        SwingUtilities.invokeLater(() -> {
            try {
                JSONObject json = new JSONObject(responseMsg);
                String command = json.optString("command");
                String message = json.optString("message");
                boolean result = json.optBoolean("result");
                if (command.equals("search")) {
                    String des = json.optString("des");
                    desField.setText(des);
                }
                showMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
                showMessage(ConsoleMessage.INVALID_REQUEST);
            }
        });
    }

    /**
     * initialize UI frame
     */
    private void initFrame() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame(TITLE);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        frame.setContentPane(panel);
    }

    /**
     * UI elements
     */
    private void createUIElements() {
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0, 0};
        layout.columnWeights = new double[]{1, 1, 1, 1};
        layout.rowHeights = new int[]{30, 30, 30, 30, 30, 200};
        layout.rowWeights = new double[]{1, 1, 1, 1, 1, 3};
        panel.setLayout(layout);

        // row 0
        JLabel serverLabel = new JLabel("server");
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(serverLabel, constraint);

        serverField = new JTextField();
        constraint.insets = new Insets(0, -10, 0, -50);
        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(serverField, constraint);

        JLabel portLabel = new JLabel("port");
        constraint.insets = new Insets(0, 50, 0, 0);
        constraint.gridx = 2;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portLabel, constraint);

        portField = new JTextField();
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 3;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portField, constraint);

        // row 1
        JLabel wordLabel = new JLabel("word");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 1;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(wordLabel, constraint);

        wordField = new JTextField();
        constraint.insets = new Insets(0, -10, 0, 0);
        constraint.gridx = 1;
        constraint.gridy = 1;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(wordField, constraint);

        // row 2
        JLabel desLabel = new JLabel("description");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 2;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(desLabel, constraint);

        desField = new JTextField();
        constraint.insets = new Insets(0, -10, 0, 0);
        constraint.gridx = 1;
        constraint.gridy = 2;
        constraint.gridwidth = 3;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(desField, constraint);

        // row 3
        searchButton = new JButton("search");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 3;
        constraint.gridwidth = 1;
        panel.add(searchButton, constraint);
        searchButton.addActionListener((e -> {
            if (checkServerAndPort() && checkWord()) {
                request("search", word, "");
            }
        }));

        addButton = new JButton("add");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 1;
        constraint.gridy = 3;
        constraint.gridwidth = 1;
        panel.add(addButton, constraint);
        addButton.addActionListener((e -> {
            if (checkServerAndPort() && checkWord() && checkDescription()) {
                request("add", word, des);
            }
        }));

        deleteButton = new JButton("delete");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 2;
        constraint.gridy = 3;
        constraint.gridwidth = 1;
        panel.add(deleteButton, constraint);
        deleteButton.addActionListener((e -> {
            if (checkServerAndPort() && checkWord()) {
                request("delete", word, "");
            }
        }));

        listButton = new JButton("list");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 3;
        constraint.gridy = 3;
        constraint.gridwidth = 1;
        panel.add(listButton, constraint);
        listButton.addActionListener((e -> {
            if (checkServerAndPort()) {
                request("list", "", "");
            }
        }));

        // row 4
        JLabel consoleLabel = new JLabel("console");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 4;
        constraint.gridwidth = 1;
        panel.add(consoleLabel, constraint);

        clearButton = new JButton("clear");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 1;
        constraint.gridy = 4;
        constraint.gridwidth = 1;
        panel.add(clearButton, constraint);
        clearButton.addActionListener((e -> {
            wordField.setText("");
            desField.setText("");
            consoleText.setText("");
        }));

        // row 5
        consoleText = new JTextArea();
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 5;
        constraint.gridwidth = 4;
        constraint.fill = GridBagConstraints.BOTH;
        consoleText.setBorder(new EmptyBorder(10, 10, 10, 10));
        consoleText.setMinimumSize(new Dimension(300, 280));
        consoleText.setLineWrap(true);
        consoleText.setWrapStyleWord(true);
        panel.add(new JScrollPane(consoleText), constraint);

        serverField.setText(serverAddress);
        portField.setText(port + "");

        frame.setVisible(true);
    }

    /**
     * check whether server and port is legal
     */
    private boolean checkServerAndPort() {
        port = parseUtil.parsePort(portField.getText());
        if (port == -1) {
            showMessage(ConsoleMessage.INVALID_PORT);
            return false;
        }

        serverAddress = serverField.getText().trim();
        if (serverAddress.isEmpty()) {
            showMessage(ConsoleMessage.SERVER_EMPTY);
        }
        return true;
    }

    /**
     * check whether word field is empty
     */
    private boolean checkWord() {
        word = wordField.getText().trim().toLowerCase();
        if (word.isEmpty()) {
            showMessage(ConsoleMessage.WORD_EMPTY);
            return false;
        }
        return true;
    }

    /**
     * check whether des field is empty
     */
    private boolean checkDescription() {
        des = desField.getText().trim().toLowerCase();
        if (des.isEmpty()) {
            showMessage(ConsoleMessage.DES_EMPTY);
            return false;
        }
        return true;
    }

    /**
     * close a socket
     * @param socket
     */
    private void closeSocket(Socket socket) {
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * show messages on console
     */
    private void showMessage(String msg) {
        consoleText.append(msg + "\n");
    }
}
