/*
 * the dictionary server, handling the connections
 */
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.*;

public class DictionaryServer {
    private static final int FIXED_THREAD_NUMBER = 5;
    private static final int MAX_THREAD_NUMBER = 10;

    private ThreadPoolExecutor executor;
    private ServerSocket serverSocket;
    private String dictionaryFilePath;
    private ServerListener listener;

    private boolean isStarted;

    public DictionaryServer() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(FIXED_THREAD_NUMBER);
        executor.setMaximumPoolSize(MAX_THREAD_NUMBER);
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    public void setDictionaryFilePath(String path) {
        this.dictionaryFilePath = path;
    }

    /**
     * create server socket,
     * bind to the port and start listening
     *
     * @param port valid port
     */
    public void startServer(int port) throws IOException {
        if (!isStarted) {
            ServerSocketFactory factory = ServerSocketFactory.getDefault();
            serverSocket = factory.createServerSocket(port);
            isStarted = true;

            // start a new thread to get server working
            Thread workingThread = new Thread(this::work);
            workingThread.start();
            onServerStarted();
        }
    }

    /**
     * close server
     */
    public void closeServer() {
        if (serverSocket != null && !serverSocket.isClosed() && isStarted) {
            try {
                serverSocket.close();
                onServerStopped();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isStarted = false;
        }
    }

    /**
     * shutdown the thread pool
     */
    public void shutdownThreadPool() {
        executor.shutdownNow();
    }

    public boolean isStarted() {
        return isStarted;
    }

    /**
     * server is working in thread, accepting connections
     */
    private void work() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                executor.execute(() -> {
                    handleClientSocket(socket);
                });
            } catch (Exception e) {
                closeServer();
                return;
            }
        }
    }

    /**
     * read and write information through stream
     * @param socket client socket
     */
    private void handleClientSocket(Socket socket) {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            String clientMsg = inputStream.readUTF();

            String responseMsg = getResponseMessage(clientMsg);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(responseMsg);
            outputStream.flush();

            JSONObject responseJson = new JSONObject(responseMsg);
            String command = responseJson.optString("command");
            String message = responseJson.optString("message");
            boolean result = responseJson.optBoolean("result");
            onServerRequest(socket, command, message);


            closeSocket(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * parse client message and produce the response message
     *
     * @param clientMsg client message
     * @return response message
     */
    private String getResponseMessage(String clientMsg) {
        Map<String, Object> map = new HashMap<>();
        map.put("result", false);
        map.put("message", ConsoleMessage.INVALID_REQUEST);
        map.put("command", "unknown");

        List<String> words = parseUtil.getDictionaryWords(dictionaryFilePath);
        try {
            JSONObject json = new JSONObject(clientMsg);
            String command = json.optString("command");
            map.put("command", command);
            if (command.equals("add")) {
                String word = json.optString("word").trim().toLowerCase();
                String des = json.optString("des").trim();
                if (words.contains(word)) {
                    map.put("message", ConsoleMessage.WORD_EXISTS);
                    map.put("result", false);
                } else if (parseUtil.addDictionaryWord(dictionaryFilePath, word, des)) {
                    map.put("message", ConsoleMessage.ADD_WORD_SUCCEEDED);
                    map.put("result", true);
                } else {
                    map.put("message", ConsoleMessage.ADD_WORD_FAILED);
                    map.put("result", false);
                }
            } else if (command.equals("delete")) {
                String word = json.optString("word").trim().toLowerCase();
                if (!words.contains(word)) {
                    map.put("message", ConsoleMessage.WORD_NOT_EXISTS);
                    map.put("result", false);
                } else if (parseUtil.deleteDictionaryWord(dictionaryFilePath, word)) {
                    map.put("message", ConsoleMessage.DELETE_WORD_SUCCEEDED);
                    map.put("result", true);
                } else {
                    map.put("message", ConsoleMessage.DELETE_WORD_FAILED);
                    map.put("result", false);
                }
            } else if (command.equals("search")) {
                String word = json.optString("word").trim().toLowerCase();
                if (!words.contains(word)) {
                    map.put("message", ConsoleMessage.SEARCH_NOT_EXISTS);
                    map.put("result", false);
                } else {
                    String des = parseUtil.searchDictionaryWord(dictionaryFilePath, word);
                    if (des.isEmpty()) {
                        map.put("result", false);
                        map.put("message", ConsoleMessage.SEARCH_NOT_EXISTS);
                    } else {
                        map.put("result", true);
                        map.put("des", des);
                        map.put("message", word + " : " + des);
                    }
                }
            } else if (command.equals("list")) {
                int count = words.size();
                String wordsStr = "";
                for (String word : words) {
                    wordsStr = wordsStr + word + " ";
                }
                map.put("result", true);
                map.put("message", count + " word(s): " + wordsStr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new JSONObject(map).toString();
    }

    /**
     * close a socket
     *
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

    private void onServerStarted() {
        SwingUtilities.invokeLater(listener::onServerStarted);
    }

    private void onServerStopped() {
        SwingUtilities.invokeLater(listener::onServerStopped);
    }

    private void onServerRequest(Socket socket, String command, String message) {
        SwingUtilities.invokeLater(() -> {
            listener.onServerRequest(socket, command, message);
        });
    }
}
