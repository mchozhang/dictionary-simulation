/*
 *  Messages that show in the console
 */

public class ConsoleMessage {
    public static final String WRONG_ARGUMENT = "Usage: java -jar DictionaryServer.jar <port> <dictionary-file>";
    public static final String INVALID_PORT = "port should be in range of (1024, 65535)";
    public static final String INVALID_FILE_PATH = "Custom dictionary file is invalid, the default file is in use.";
    public static final String SERVER_STARTED = "Server started.";
    public static final String SERVER_STOPPED = "Server stopped.";
    public static final String PORT_IN_USE = "Server failed to start: port is in use.";
    public static final String SERVER_START_FAILED = "Server failed to start: ";
    public static final String WORD_EXISTS = "Add word failed: word already exists";
    public static final String ADD_WORD_SUCCEEDED = "Add word succeeded.";
    public static final String ADD_WORD_FAILED = "Add word failed: unknown reason.";
    public static final String WORD_NOT_EXISTS = "Delete word failed: word doesn't exists";
    public static final String DELETE_WORD_SUCCEEDED = "Delete word succeeded.";
    public static final String DELETE_WORD_FAILED = "Delete word failed: unknown reason.";
    public static final String SEARCH_NOT_EXISTS = "Search word failed: word doesn't exists";
    public static final String INVALID_REQUEST = "Error occurred.";
    public static final String SERVER_EMPTY = "Please input server address.";
    public static final String WORD_EMPTY = "Please input word.";
    public static final String DES_EMPTY = "Please input description.";
    public static final String CONNECT_FAILED = "Connect failed,please check server address and port.";
}
