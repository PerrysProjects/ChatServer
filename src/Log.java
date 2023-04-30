import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static final String LOG_FOLDER_NAME = "log";
    private static final DateTimeFormatter FILENAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter LOG_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static File logFolder;
    private static FileWriter logWriter;

    static {
        try {
            logFolder = new File(LOG_FOLDER_NAME);
            if(!logFolder.exists()) {
                logFolder.mkdir();
            }
        } catch(Exception e) {
            System.err.println("Error creating log folder: " + e.getMessage());
        }
    }

    public static void log(String message) {
        try {
            if(logWriter == null) {
                String filename = LocalDateTime.now().format(FILENAME_FORMAT) + ".log";
                logWriter = new FileWriter(new File(logFolder, filename));
            }
            String logMessage = "[" + LocalDateTime.now().format(LOG_FORMAT) + "] " + message + "\n";
            logWriter.write(logMessage);
            logWriter.flush();
        } catch(IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    public static void close() {
        try {
            if(logWriter != null) {
                logWriter.close();
            }
        } catch(IOException e) {
            System.err.println("Error closing log file: " + e.getMessage());
        }
    }
}
