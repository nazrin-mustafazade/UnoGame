package model;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameLogger {
    private final String filename;

    /**
     * Constructs a GameLogger with a base filename.
     * The actual filename is formed by appending the current date and time to the base filename.
     * The date and time are formatted as 'yyyyMMdd_HHmmss'.
     *
     * @param baseFilename The base filename for the log file.
     */
    public GameLogger(String baseFilename) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateString = formatter.format(new Date());
        this.filename = baseFilename + "_" + dateString + ".txt";
    }

    /**
     * Logs a message to the log file.
     * The message is prefixed with the current date and time.
     * If an I/O error occurs while writing to the file, an error message is printed to the standard error stream.
     *
     * @param message The message to log.
     */
    public void log(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename, true))) {
            out.println(new Date() + ": " + message);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
