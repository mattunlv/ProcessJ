package utilities;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

/**
 * This utility class provides abstraction layer for sending
 * multipart HTTP Post request to the ProcessJ web server.
 * 
 * @author ben
 * @author jiani
 */
public final class FrequencyFileProcessing {
    
    /** Maximum size of file before it is updated */
    private static final int FILE_SIZE = 1000000;
    
    private FrequencyFileProcessing() {
    }
    
    public static void updateFrequency() {
        String path = "resources/files/errors";
        URL url = PropertiesLoader.getURL(path);
        if ( url!=null ) {
            path = url.getFile();
            // Check if file is 1MB or more. If no, then return
//            if ( new File(path).length()!=FILE_SIZE ) {
//                // Send file to the server
//            }
            try {
                String server = "http://localhost:8080/frequency.do";
                String charset = "UTF-8";
                String param = "value";
                // Create a unique boundary based on time stamp
                String boundary = Long.toHexString(System.currentTimeMillis());
                // Line separator required by multipart/form-data
                String CRLF = "\r\n";
                // File to be sent over HTTP
                File file = new File(path);
                System.err.println("[info] Connecting to the web server..");
                HttpURLConnection conn = (HttpURLConnection) new URL(server).openConnection();
                conn.setDoOutput(true); // Indicates POST method
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                OutputStream os = conn.getOutputStream();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, charset), true);
                System.err.println("[info] Processing file..");
                // Send parameter
                pw.append("--" + boundary).append(CRLF);
                pw.append("Content-Disposition: form-data; name=\"" + param + "\"").append(CRLF);
                pw.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                pw.append(CRLF).append(param).append(CRLF).flush();
                // Send text file
                pw.append("--" + boundary).append(CRLF);
                pw.append("Content-Disposition: form-data; name=\"fileUpload\"" +
                        "; filename=\"" + file.getName() + "\"").append(CRLF);
                // The text file must be saved in this character set
                pw.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                pw.append(CRLF).flush();
                Files.copy(file.toPath(), os);
                os.flush(); // Important before continuing with writer
                pw.append(CRLF).flush(); // CRLF indicates the end of boundary
                // End of multipart/form-data
                pw.append("--" + boundary + "--").append(CRLF).flush();
                pw.close();
                int status = conn.getResponseCode();
                if ( status==HttpURLConnection.HTTP_OK ) {
                    System.err.println("[info] Closing connection..");
                    conn.disconnect();
                    // Cleaning file
                    System.err.println("[info] Cleaning file for future processing..");
                    pw = new PrintWriter(file);
                    pw.print("");
                    pw.close();
                    System.err.println("[info] Done..");
                } else {
                    System.err.println("[error] Server returned non-OK status: " + status);
                    System.exit(0);
                }
            } catch (Exception ex) {
                System.err.println("[error] Error connecting to the web server application..");
                ex.printStackTrace();
            }
        }
    }
}
