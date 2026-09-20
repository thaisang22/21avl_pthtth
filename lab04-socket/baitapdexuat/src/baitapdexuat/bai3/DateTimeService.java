package baitapdexuat.bai3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class DateTimeService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MM yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH mm ss");

    private DateTimeService() {
    }

    static String process(String request) {
        LocalDateTime now = LocalDateTime.now();
        if ("DATE".equalsIgnoreCase(request)) return "OK " + DATE.format(now);
        if ("TIME".equalsIgnoreCase(request)) return "OK " + TIME.format(now);
        if ("DATETIME".equalsIgnoreCase(request)) return "OK " + DATE.format(now) + " " + TIME.format(now);
        if ("QUIT".equalsIgnoreCase(request)) return "OK BYE";
        return "ERR UNKNOWN_COMMAND";
    }
}
