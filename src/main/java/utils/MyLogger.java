package utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.*;


public class MyLogger {
    private static Logger logger;

//    static {
//        try {
//            logger = Logger.getLogger("MyLogger");
//            logger.setLevel(Level.ALL);
//            FileHandler fh = new FileHandler("my_log_file.log");
//            fh.setFormatter(new SimpleFormatter() {
//                private static final String format = "[%1$tT] [%2$-7s] %3$s %n";
//
//                @Override
//                public synchronized String format(LogRecord lr) {
//                    return String.format(format,
//                            new Date(lr.getMillis()),
//                            lr.getLevel().getLocalizedName(),
//                            lr.getMessage()
//                    );
//                }
//            });
//            logger.setUseParentHandlers(false);
//            logger.addHandler(fh);
//
//        } catch (IOException e) {
//            System.err.println(e);
//            warn(e.getMessage());
////            throw new RuntimeException(e);
//        }
//    }

   public static void start(){
       try {
           logger = Logger.getLogger("MyLogger");
           logger.setLevel(Level.ALL);
           FileHandler fh = new FileHandler("my_log_file.log");
           fh.setFormatter(new SimpleFormatter() {
               private static final String format = "[%1$tT] [%2$-7s] %3$s %n";

               @Override
               public synchronized String format(LogRecord lr) {
                   return String.format(format,
                           new Date(lr.getMillis()),
                           lr.getLevel().getLocalizedName(),
                           lr.getMessage()
                   );
               }
           });
           logger.setUseParentHandlers(false);
           logger.addHandler(fh);

       } catch (IOException e) {
           System.err.println(e);
           warn(e.getMessage());
//            throw new RuntimeException(e);
       }
   }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warning(msg);
    }


    public static String createTable(List<String[]> data, String... columnNames) {
        if (data.size() == 0) return null;
        if (data.get(0).length != columnNames.length) {
            warn("data.size() != columnNames.length");
            throw new RuntimeException("data.size() != columnNames.length");
        }
        var tableFormat = "";
        for (int i = 0; i < columnNames.length; i++) {
            tableFormat += "| %-" + Math.max(columnNames[i].length(), data.get(0)[i].length()) + "s ";
        }
        tableFormat += "|\n";

        var tableHeader = String.format(tableFormat, (Object[]) columnNames);

        StringBuilder result = new StringBuilder();
        result.append("\n");
        var tableLine = createTableLine(tableHeader.length());

        result.append(tableLine);
        result.append(tableHeader);
        result.append(tableLine);

        for (String[] info : data) {
            result.append(String.format(tableFormat, (Object[]) info));
        }
        result.append(tableLine);

        return result.toString();
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
