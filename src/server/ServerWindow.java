/*
 * the UI window of server
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;

public class ServerWindow implements ServerListener {
    private static final String TITLE = "Dictionary Server";
    private static final int WIDTH = 500;
    private static final int HEIGHT = 480;
    private static final int DEFAULT_PORT = 8000;

    // private properties
    private DictionaryServer server;
    private int port;
    private String filePath;

    // UI elements
    private JFrame frame;
    private JPanel panel;
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea consoleText;
    private JButton clearButton;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ServerWindow window = new ServerWindow();
                String errorMessage = "";

                // parse arguments, port and file path
                if (args.length == 0) {
                    window.port = DEFAULT_PORT;
                    window.filePath = parseUtil.getDefaultDictPath();
                } else if (args.length == 1) {
                    int port = parseUtil.parsePort(args[0]);
                    if (port == -1) {
                        window.port = DEFAULT_PORT;
                        errorMessage = ConsoleMessage.INVALID_PORT;
                    } else {
                        window.port = port;
                    }
                    window.filePath = parseUtil.getDefaultDictPath();
                } else if (args.length == 2) {
                    int port = parseUtil.parsePort(args[0]);
                    if (port == -1) {
                        window.port = DEFAULT_PORT;
                        errorMessage = ConsoleMessage.INVALID_PORT;
                    } else {
                        window.port = port;
                    }

                    if (parseUtil.isDictionaryFileLegal(args[1])) {
                        window.filePath = args[1];
                    } else {
                        window.filePath = parseUtil.getDefaultDictPath();
                        errorMessage = ConsoleMessage.INVALID_FILE_PATH;
                    }
                } else {
                    System.err.println(ConsoleMessage.WRONG_ARGUMENT);
                    System.exit(1);
                }

                window.initFrame();
                window.createUIElements();

                if (!errorMessage.isEmpty()) {
                    window.showMessage(errorMessage);
                }

                window.initServer();
                window.startServer();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ServerWindow() {
    }

    @Override
    public void onServerStarted() {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        showMessage(ConsoleMessage.SERVER_STARTED + " Listening on " + port + ".");
    }

    @Override
    public void onServerStopped() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        showMessage(ConsoleMessage.SERVER_STOPPED);
    }

    @Override
    public void onServerRequest(Socket socket, String command, String message) {
        showMessage("request: " + socket.getInetAddress() + ":" + socket.getPort()
                + " command: " + command);
        showMessage("response message: " + message);
    }

    /**
     * initialize the server
     */
    private void initServer() {
        server = new DictionaryServer();
        server.setListener(this);
        server.setDictionaryFilePath(filePath);
    }

    /**
     * start server, should be called after initializing
     */
    private void startServer() {
        // check port validity
        port = parseUtil.parsePort(portField.getText());
        if (port == -1) {
            showMessage(ConsoleMessage.INVALID_PORT);
            return;
        }

        // start server
        try {
            server.startServer(port);
        } catch (IOException e) {
            // port in use
            if (e.toString().contains("in use")) {
                showMessage(ConsoleMessage.PORT_IN_USE);
            } else {
                showMessage(ConsoleMessage.SERVER_START_FAILED + e.getMessage());
            }
        }
    }

    private void stopServer() {
        server.closeServer();
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
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.closeServer();
                server.shutdownThreadPool();
                super.windowClosed(e);
            }
        });
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        frame.setContentPane(panel);
    }

    private void createUIElements() {
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0, 0};
        layout.columnWeights = new double[]{1, 1, 1.5, 1.5};
        layout.rowHeights = new int[]{50, 50, 300};
        layout.rowWeights = new double[]{1, 1, 6};
        panel.setLayout(layout);

        // row 0
        JLabel portLabel = new JLabel("port");
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portLabel, constraint);

        portField = new JTextField();
        constraint.insets = new Insets(0, 0, 0, 30);
        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portField, constraint);

        startButton = new JButton("start");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 2;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(startButton, constraint);
        startButton.addActionListener((e) -> {
            startServer();
        });

        stopButton = new JButton("stop");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 3;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        panel.add(stopButton, constraint);
        stopButton.addActionListener((e) -> {
            stopServer();
        });

        // row 1
        JLabel consoleLabel = new JLabel("console");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 1;
        panel.add(consoleLabel, constraint);

        clearButton = new JButton("clear");
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 1;
        constraint.gridy = 1;
        panel.add(clearButton, constraint);
        clearButton.addActionListener((e -> {
            consoleText.setText("");
        }));

        // row 2
        consoleText = new JTextArea();
        constraint.insets = new Insets(0, 0, 0, 0);
        constraint.gridx = 0;
        constraint.gridy = 2;
        constraint.gridwidth = 4;
        constraint.fill = GridBagConstraints.BOTH;
        consoleText.setBorder(new EmptyBorder(10, 10, 10, 10));
        consoleText.setMinimumSize(new Dimension(300, 400));
        consoleText.setLineWrap(true);
        consoleText.setWrapStyleWord(true);
        panel.add(new JScrollPane(consoleText), constraint);

        portField.setText(port + "");
        stopButton.setEnabled(false);

        frame.setVisible(true);
    }

    /**
     * show messages on console
     */
    private void showMessage(String msg) {
        consoleText.append(msg + "\n");
    }
}
