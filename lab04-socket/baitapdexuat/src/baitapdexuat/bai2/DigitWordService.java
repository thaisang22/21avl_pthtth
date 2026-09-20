package baitapdexuat.bai2;

final class DigitWordService {
    private static final String[] WORDS = {
            "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    private DigitWordService() {
    }

    static String process(String request) {
        if ("QUIT".equalsIgnoreCase(request)) {
            return "OK BYE";
        }
        if (request != null && request.length() == 1 && request.charAt(0) >= '0' && request.charAt(0) <= '9') {
            return "OK " + WORDS[request.charAt(0) - '0'];
        }
        return "ERR INVALID_DIGIT";
    }
}
