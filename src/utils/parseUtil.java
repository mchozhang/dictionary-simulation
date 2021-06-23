/*
 * parse objects into valid format
 */

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class parseUtil {
    /**
     * parse port,
     * port should be in [1024, 65535]
     * return -1 if argument is invalid
     *
     * @param portStr argument of port
     * @return port
     */
    public static int parsePort(String portStr) {
        try {
            int value = Integer.parseInt(portStr);
            if (1024 <= value && value <= 65565)
                return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * parse dictionary file path
     *
     * @param path file path
     * @return valid file path
     */
    public static String parseFilePath(String path) {
        return "";
    }

    /**
     * parse server address, return empty string if invalid
     *
     * @param address server address
     * @return valid address
     */
    public static String parseServerAddress(String address) {
        return address;
    }

    /**
     * in jar, copy the default file to the working directory
     * @return default dictionary file path
     */
    public static String getDefaultDictPath() {
        URL url = parseUtil.class.getClassLoader().getResource("dictionary.xml");
        if (url.toString().startsWith("jar:")) {
            File file = new File("dictionary.xml");
            if (file.exists()) {
                return file.getAbsolutePath();
            } else {
                try {
                    InputStream inputStream = parseUtil.class.getClassLoader().getResourceAsStream("dictionary.xml");
                    OutputStream outputStream = new FileOutputStream(file);
                    int read;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file.getAbsolutePath();
            }
        } else {
            return url.getPath();
        }
    }

    /**
     * check if the dictionary file is parsable
     * @param path file path
     * @return parsable
     */
    public static boolean isDictionaryFileLegal(String path) {
        try {
            Element root = getDictDom(path).getRootElement();
            return root.getName().equals("dictionary");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * get all words
     * @return list of words
     */
    public static List<String> getDictionaryWords(String path) {
        List<String> words = new ArrayList<>();
        try {
            Element root = getDictDom(path).getRootElement();
            for (Iterator i = root.elementIterator(); i.hasNext();) {
                Element nodeElement = (Element) i.next();
                if (nodeElement.getName().equals("node")) {
                    String word = nodeElement.element("word").getText();
                    if (!word.trim().isEmpty())
                        words.add(word);
                }
            }
            return words;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return words;
    }

    /**
     * add new word to dictionary file
     * @param path file path
     * @param word word
     * @param des description
     * @return do adding succeed
     */
    public static boolean addDictionaryWord(String path, String word, String des) {
        try {
            SAXReader saxReader = new SAXReader();
            File file = new File(path);
            Document dom = saxReader.read(file);
            Element root = dom.getRootElement();

            // add content
            Element nodeElement = root.addElement("node");
            Element wordElement = nodeElement.addElement("word");
            wordElement.setText(word);
            Element desElement = nodeElement.addElement("des");
            desElement.setText(des);

            FileWriter out = new FileWriter(path);
            dom.write(out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * delete word to dictionary file
     * @param path file path
     * @param word word
     * @return do deleting succeed
     */
    public static boolean deleteDictionaryWord(String path, String word) {
        try {
            Document dom = getDictDom(path);
            Element root = dom.getRootElement();
            for (Iterator i = root.elementIterator(); i.hasNext();) {
                Element nodeElement = (Element) i.next();
                if (nodeElement.getName().equals("node")) {
                    if(word.equals(nodeElement.element("word").getText())) {
                        root.remove(nodeElement);

                        FileWriter out = new FileWriter(path);
                        dom.write(out);
                        out.close();
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * search word description
     * @param path file path
     * @param word word
     * @return word description
     */
    public static String searchDictionaryWord(String path, String word) {
        try {
            Document dom = getDictDom(path);
            Element root = dom.getRootElement();
            for (Iterator i = root.elementIterator(); i.hasNext();) {
                Element nodeElement = (Element) i.next();
                if (nodeElement.getName().equals("node")) {
                    if(word.equals(nodeElement.element("word").getText())) {
                        return nodeElement.element("des").getText();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static Document getDictDom(String path) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        File file = new File(path);
        return saxReader.read(file);
    }
}
