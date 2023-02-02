package utils;

import model.Node;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class MyLogger {
    private static final Logger logger;

    static {
        try {
            logger = Logger.getLogger("MyLogger");
            logger.setLevel(Level.ALL);
            FileHandler fh = new FileHandler("my_log_file.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warning(msg);
    }


    public static String createTable(List<String[]> data, String... columnNames) {
        if (data.get(0).length != columnNames.length) {
            throw new RuntimeException("data.size() != columnNames.length");
        }
        var tableFormat = "";
        for (int i = 0; i < columnNames.length; i++) {
            tableFormat += "| %-" + Math.max(columnNames[i].length(), data.get(0)[i].length()) + "s ";
        }
        tableFormat += "|\n";
        var tableHeader = String.format(tableFormat, columnNames);

        StringBuilder output = new StringBuilder();
        output.append("\n");
        var tableLine = createTableLine(tableHeader.length());
//        for (int j = 0; j < tableHeader.length() - 3; j++) {
//            output.append("-");
//        }
//        output.append("+\n");
        output.append(tableLine);
        output.append(tableHeader);
        output.append(tableLine);
//        output.append("+");
//        for (int j = 0; j < tableHeader.length() - 3; j++) {
//            output.append("-");
//        }
//        output.append("+\n");

        for (String[] info : data) {
            output.append(String.format(tableFormat, info));

        }
        output.append(tableLine);
//        output.append("+");
//        for (int j = 0; j < tableHeader.length() - 3; j++) {
//            output.append("-");
//        }
//        output.append("+\n");
        return output.toString();
    }

    private static String createTableLine(int length) {
        var output = "";
        output += "+";
        for (int j = 0; j < length - 3; j++) {
            output += "-";
        }
        output += "+\n";
        return output;
    }
}
