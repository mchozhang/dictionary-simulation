/*
 * listener of server
 */

import java.net.Socket;

/**
 * listener observes server
 */
public interface ServerListener {

    // server started event
    void onServerStarted();

    // server stopped event
    void onServerStopped();

    // a new request is received by server
    void onServerRequest(Socket socket, String command, String message);
}
