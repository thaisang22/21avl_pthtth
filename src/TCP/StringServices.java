package TCP;
public class StringServices {
    public static String process(int service, String input) {
        switch (service) {
            case 1: return capitalize(new StringBuilder(input).reverse().toString());
            case 2:
                // Dao nguoc ky tu trong tung tu, giu nguyen khoang trang va thu tu tu.
                StringBuilder result = new StringBuilder();
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\S+|\\s+").matcher(input);
                while (matcher.find()) {
                    String part = matcher.group();
                    result.append(part.trim().isEmpty() ? part : capitalize(new StringBuilder(part).reverse().toString()));
                }
                return result.toString();
            case 3: return "Tong so tu: " + countWords(input);
            case 4:
                String[] lines = input.split("\\n", -1);
                StringBuilder counts = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) counts.append('\n');
                    counts.append("Dong ").append(i + 1).append(": ").append(countWords(lines[i])).append(" tu");
                }
                return counts.toString();
            default: return "Lua chon khong hop le";
        }
    }
    private static int countWords(String text) {
        return text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }
    private static String capitalize(String text) {
        StringBuilder result = new StringBuilder();
        boolean newWord = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) { newWord = true; result.append(c); }
            else { result.append(newWord ? Character.toUpperCase(c) : c); newWord = false; }
        }
        return result.toString();
    }
}
