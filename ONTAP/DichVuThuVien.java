import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * =========================================================================
 * THƯ VIỆN DỊCH VỤ DÙNG CHUNG (DichVuThuVien) - FIT IUH
 * --------------------------------------------------------------------------
 * TÁCH BIỆT HOÀN TOÀN PHẦN XỬ LÝ LOGIC RA KHỎI SERVER/CLIENT.
 * Khi vào phòng thi, nếu giáo viên đổi dịch vụ nào:
 *  1. Bạn chỉ việc gọi hàm tương ứng trong file này (hoặc viết thêm hàm mới).
 *  2. ServerTCP và ServerUDP chỉ đóng vai trò nhận chuỗi -> gọi hàm -> gửi kết quả.
 *  3. Các dịch vụ hoàn toàn độc lập, KHÔNG HỀ ẢNH HƯỞNG LẪN NHAU.
 *
 * CÁCH TỰ VIẾT THÊM HÀM MỚI (CHỈ MẤT 30 GIÂY):
 *  public static String xuLyDichVuMoi(String input) {
 *      // 1. Kiểm tra rỗng: if (input == null || input.isEmpty()) return "ERR";
 *      // 2. Xử lý thuật toán đề bài yêu cầu...
 *      // 3. Trả về kết quả dạng chuỗi String
 *      return "KẾT QUẢ";
 *  }
 * =========================================================================
 */
public class DichVuThuVien {

    // =========================================================================
    // DỊCH VỤ 1 (ĐỀ THI GỐC): ĐẢO TOÀN BỘ CHUỖI & IN HOA CHỮ ĐẦU
    // - Ý nghĩa: "PHAT TRIEN HE THONG TICH HOP" -> "Poh Hcit Gnoht Eh Neirt Tahp"
    // - Giải thuật: Tách từ bằng split("\\s+"), duyệt ngược từ cuối mảng về đầu mảng,
    //   mỗi từ dùng StringBuilder(w).reverse(), sau đó viết hoa chữ đầu.
    // =========================================================================
    public static String dichVu1_DaoNguocToanBo(String str) {
        if (str == null || str.trim().isEmpty()) return "";
        String[] words = str.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        // Duyệt từ cuối về đầu (đảo thứ tự toàn bộ từ)
        for (int i = words.length - 1; i >= 0; i--) {
            String reversedWord = new StringBuilder(words[i]).reverse().toString();
            sb.append(inHoaChuDau(reversedWord));
            if (i > 0) sb.append(" ");
        }
        return sb.toString();
    }

    // =========================================================================
    // DỊCH VỤ 2 (ĐỀ THI GỐC): ĐẢO TỪ GIỮ NGUYÊN VỊ TRÍ & IN HOA CHỮ ĐẦU
    // Ví dụ: "PHAT TRIEN HE THONG TICH HOP" -> "Tahp Neirt Eh Gnoht Hcit Poh"
    // =========================================================================
    public static String dichVu2_DaoNguocGiuViTri(String str) {
        if (str == null || str.trim().isEmpty()) return "";
        String[] words = str.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        // Duyệt từ đầu đến cuối (giữ nguyên vị trí các từ)
        for (int i = 0; i < words.length; i++) {
            String reversedWord = new StringBuilder(words[i]).reverse().toString();
            sb.append(inHoaChuDau(reversedWord));
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    // =========================================================================
    // DỊCH VỤ 3 (ĐỀ THI GỐC): ĐẾM SỐ LƯỢNG TỪ CỦA TẤT CẢ CÁC DÒNG GỬI ĐẾN
    // =========================================================================
    public static int dichVu3_DemTongSoTu(List<String> lines) {
        if (lines == null || lines.isEmpty()) return 0;
        int total = 0;
        for (String line : lines) {
            total += demSoTu(line);
        }
        return total;
    }

    // =========================================================================
    // DỊCH VỤ 4 (ĐỀ THI GỐC): ĐẾM SỐ LƯỢNG TỪ TỪNG DÒNG
    // =========================================================================
    public static List<String> dichVu4_DemTuTungDong(List<String> lines) {
        List<String> result = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return result;
        for (int i = 0; i < lines.size(); i++) {
            int count = demSoTu(lines.get(i));
            result.add("Dòng " + (i + 1) + ": " + count + " từ");
        }
        return result;
    }

    // =========================================================================
    // DỊCH VỤ ĐỔI CHỮ SỐ THÀNH CHỮ (BÀI 2)
    // Ca biên: 0-9 hợp lệ; rỗng, khoảng trắng, 10, 'a' -> ERR INVALID_DIGIT
    // =========================================================================
    private static final String[] VIET_DIGITS = {
        "Không", "Một", "Hai", "Ba", "Bốn", "Năm", "Sáu", "Bảy", "Tám", "Chín"
    };

    public static String xuLyDoiSo(String input) {
        // Ca biên bắt buộc: 0, 9, chuỗi rỗng (""), 10, ký tự 'a', dữ liệu có khoảng trắng (" 5 ") -> ERR INVALID_DIGIT
        if (input == null || input.length() != 1 || input.charAt(0) < '0' || input.charAt(0) > '9') {
            return "ERR INVALID_DIGIT";
        }
        return VIET_DIGITS[input.charAt(0) - '0'];
    }

    // =========================================================================
    // DỊCH VỤ NGÀY GIỜ (BÀI 3)
    // Lệnh: DATE | TIME | DATETIME
    // =========================================================================
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH mm ss");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd MM yyyy HH mm ss");

    public static String xuLyNgayGio(String command) {
        if (command == null) return "ERR UNKNOWN_COMMAND";
        String cmd = command.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        switch (cmd) {
            case "DATE": return now.format(DATE_FMT);
            case "TIME": return now.format(TIME_FMT);
            case "DATETIME": return now.format(DATETIME_FMT);
            default: return "ERR UNKNOWN_COMMAND (Hỗ trợ: DATE, TIME, DATETIME)";
        }
    }

    // =========================================================================
    // DỊCH VỤ MÁY TÍNH TỪ XA (BÀI 4)
    // Cú pháp: CALC <toán_tử> <số_1> <số_2> (Ví dụ: CALC + 10 20)
    // =========================================================================
    public static String xuLyMayTinh(String expr) {
        if (expr == null) return "ERR INVALID_FORMAT";
        String[] parts = expr.trim().split("\\s+");
        if (parts.length != 4 || !parts[0].equalsIgnoreCase("CALC")) {
            return "ERR INVALID_FORMAT";
        }
        String op = parts[1];
        if (!op.equals("+") && !op.equals("-") && !op.equals("*") && !op.equals("/")) {
            return "ERR UNSUPPORTED_OPERATOR";
        }
        double n1, n2;
        try {
            n1 = Double.parseDouble(parts[2]);
            n2 = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return "ERR INVALID_NUMBER";
        }
        if (op.equals("/") && n2 == 0) {
            return "ERR DIVIDE_BY_ZERO";
        }
        double res = 0;
        switch (op) {
            case "+": res = n1 + n2; break;
            case "-": res = n1 - n2; break;
            case "*": res = n1 * n2; break;
            case "/": res = n1 / n2; break;
        }
        return (res == (long) res) ? ("OK " + (long) res) : ("OK " + res);
    }

    // =========================================================================
    // DỊCH VỤ LƯU NHẬT KÝ CLIENT (BÀI 7)
    // Ghi file data/logs/{clientId}.txt
    // =========================================================================
    public static String xuLyGhiLog(String clientId, String remoteAddr, String content) {
        if (clientId == null || !clientId.matches("^[a-zA-Z0-9_-]+$")) {
            return "ERR INVALID_CLIENT_ID";
        }
        try {
            File dir = new File("data/logs");
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, clientId + ".txt");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8"), true)) {
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                pw.println(String.format("[%s] [%s] %s", time, remoteAddr, content));
            }
            return "SAVED: " + content;
        } catch (IOException e) {
            return "ERR WRITE_FILE: " + e.getMessage();
        }
    }

    // =========================================================================
    // HÀM TIỆN ÍCH BỔ TRỢ
    // =========================================================================
    public static int demSoTu(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        return str.trim().split("\\s+").length;
    }

    public static String inHoaChuDau(String word) {
        if (word == null || word.isEmpty()) return "";
        if (word.length() == 1) return word.toUpperCase();
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    // Chuyển toàn bộ chuỗi về IN HOA (UPPERCASE)
    // Ví dụ: "phat trien" -> "PHAT TRIEN"
    public static String chuyenToanBoInHoa(String str) {
        if (str == null) return "";
        return str.toUpperCase();
    }

    // Chuyển toàn bộ chuỗi về in thường (lowercase)
    // Ví dụ: "PHAT TRIEN" -> "phat trien"
    public static String chuyenToanBoInThuong(String str) {
        if (str == null) return "";
        return str.toLowerCase();
    }

    // =========================================================================
    // 6. CÁC HÀM XỬ LÝ CHUỖI NÂNG CAO HAY RA THI
    // =========================================================================

    // A. Chuẩn hóa chuỗi (xóa khoảng trắng thừa, viết hoa chữ cái đầu mỗi từ)
    // Ví dụ: "  nguyen   van   an " -> "Nguyen Van An"
    public static String chuanHoaChuoi(String str) {
        if (str == null || str.trim().isEmpty()) return "";
        String[] words = str.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            sb.append(inHoaChuDau(words[i]));
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    // B. Kiểm tra chuỗi đối xứng (Palindrome)
    // Ví dụ: "radar", "madam", "12321" -> Đối xứng
    public static String kiemTraDoiXung(String str) {
        if (str == null) return "ERR: Chuỗi rỗng";
        String s = str.replaceAll("\\s+", "").toLowerCase();
        String reversed = new StringBuilder(s).reverse().toString();
        if (s.equals(reversed)) {
            return "Chuỗi [" + str + "] là chuỗi ĐỐI XỨNG (Palindrome)";
        } else {
            return "Chuỗi [" + str + "] KHÔNG PHẢI chuỗi đối xứng";
        }
    }

    // C. Thống kê ký tự (Đếm số chữ hoa, chữ thường, chữ số, khoảng trắng, ký tự đặc biệt)
    public static String thongKeKyTu(String str) {
        if (str == null) return "ERR: Chuỗi rỗng";
        int hoa = 0, thuong = 0, so = 0, space = 0, dacbiet = 0;
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) hoa++;
            else if (Character.isLowerCase(c)) thuong++;
            else if (Character.isDigit(c)) so++;
            else if (Character.isWhitespace(c)) space++;
            else dacbiet++;
        }
        return String.format("Thống kê ký tự: [Hoa: %d, Thường: %d, Chữ số: %d, Khoảng trắng: %d, Ký tự khác: %d]",
                hoa, thuong, so, space, dacbiet);
    }

    // D. Đếm số nguyên âm và phụ âm (Tiếng Anh)
    // Ví dụ: "Hello World" -> Nguyên âm: 3 (e, o, o), Phụ âm: 7
    public static String demNguyenAmPhuAm(String str) {
        if (str == null) return "ERR: Chuỗi rỗng";
        int nguyenAm = 0, phuAm = 0;
        String s = str.toLowerCase();
        for (char c : s.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                if ("aeiou".indexOf(c) != -1) nguyenAm++;
                else phuAm++;
            }
        }
        return String.format("Nguyên âm: %d, Phụ âm: %d", nguyenAm, phuAm);
    }

    // =========================================================================
    // 7. CÁC HÀM XỬ LÝ TOÁN HỌC & DÃY SỐ HAY RA THI
    // =========================================================================

    // A. Kiểm tra số nguyên tố
    // Ví dụ: "PRIME 17" hoặc gửi "17"
    public static String kiemTraSoNguyenTo(String input) {
        try {
            long n = Long.parseLong(input.trim().replaceAll("[^0-9-]", ""));
            if (n < 2) return n + " KHÔNG PHẢI số nguyên tố";
            for (long i = 2; i <= Math.sqrt(n); i++) {
                if (n % i == 0) return n + " KHÔNG PHẢI số nguyên tố";
            }
            return n + " LÀ SỐ NGUYÊN TỐ";
        } catch (Exception e) {
            return "ERR: Dữ liệu không phải là số hợp lệ!";
        }
    }

    // B. Kiểm tra số chính phương
    public static String kiemTraSoChinhPhuong(String input) {
        try {
            long n = Long.parseLong(input.trim().replaceAll("[^0-9-]", ""));
            if (n < 0) return n + " KHÔNG PHẢI số chính phương";
            long can = (long) Math.sqrt(n);
            if (can * can == n) return n + " LÀ SỐ CHÍNH PHƯƠNG (" + can + "^2)";
            return n + " KHÔNG PHẢI số chính phương";
        } catch (Exception e) {
            return "ERR: Dữ liệu không phải là số hợp lệ!";
        }
    }

    // C. Kiểm tra số hoàn hảo (tổng ước số bằng chính nó)
    // Ví dụ: 6, 28, 496...
    public static String kiemTraSoHoanHao(String input) {
        try {
            long n = Long.parseLong(input.trim().replaceAll("[^0-9-]", ""));
            if (n <= 1) return n + " KHÔNG PHẢI số hoàn hảo";
            long tongUoc = 1;
            for (long i = 2; i <= Math.sqrt(n); i++) {
                if (n % i == 0) {
                    tongUoc += i;
                    if (i != n / i) tongUoc += (n / i);
                }
            }
            if (tongUoc == n) return n + " LÀ SỐ HOÀN HẢO";
            return n + " KHÔNG PHẢI số hoàn hảo (Tổng ước = " + tongUoc + ")";
        } catch (Exception e) {
            return "ERR: Dữ liệu không phải số!";
        }
    }

    // D. Chuyển đổi hệ cơ số (Thập phân sang Nhị phân Bin, Bát phân Oct, Thập lục phân Hex)
    // Ví dụ: gửi "255" -> Bin: 11111111, Oct: 377, Hex: FF
    public static String chuyenCoSo(String input) {
        try {
            int n = Integer.parseInt(input.trim());
            return String.format("Số %d: [Nhị phân (BIN): %s | Bát phân (OCT): %s | Thập lục phân (HEX): %s]",
                    n, Integer.toBinaryString(n), Integer.toOctalString(n), Integer.toHexString(n).toUpperCase());
        } catch (Exception e) {
            return "ERR: Dữ liệu không phải số nguyên!";
        }
    }

    // E. Tìm Ước chung lớn nhất (UCLN) và Bội chung nhỏ nhất (BCNN)
    // Ví dụ: gửi "24 36" -> UCLN = 12, BCNN = 72
    public static String timUCLNvaBCNN(String input) {
        try {
            String[] p = input.trim().split("\\s+");
            long a = Math.abs(Long.parseLong(p[p.length - 2]));
            long b = Math.abs(Long.parseLong(p[p.length - 1]));
            long x = a, y = b;
            while (y != 0) {
                long temp = y;
                y = x % y;
                x = temp;
            }
            long ucln = x;
            long bcnn = (ucln == 0) ? 0 : (a * b) / ucln;
            return String.format("Giữa %d và %d -> UCLN = %d, BCNN = %d", a, b, ucln, bcnn);
        } catch (Exception e) {
            return "ERR: Cú pháp: <số1> <số2> (Ví dụ: 24 36)";
        }
    }

    // F. Xử lý dãy số (Tìm Min, Max, Trung bình cộng, Sắp xếp tăng dần)
    // Ví dụ gửi chuỗi: "10 5 8 20 3"
    public static String xuLyDaySo(String input) {
        try {
            String[] tokens = input.trim().split("\\s+");
            List<Double> numbers = new ArrayList<>();
            for (String t : tokens) {
                numbers.add(Double.parseDouble(t));
            }
            if (numbers.isEmpty()) return "ERR: Dãy rỗng";

            double min = Collections.min(numbers);
            double max = Collections.max(numbers);
            double sum = 0;
            for (double d : numbers) sum += d;
            double avg = sum / numbers.size();

            Collections.sort(numbers);
            return String.format("Dãy số: [Min = %.1f | Max = %.1f | TBC = %.2f | Sắp xếp tăng dần: %s]",
                    min, max, avg, numbers.toString());
        } catch (Exception e) {
            return "ERR: Dãy số chứa phần tử không hợp lệ!";
        }
    }

    // G. Tính giai thừa n!
    // Ví dụ: "5" -> 5! = 120
    public static String tinhGiaiThua(String input) {
        try {
            int n = Integer.parseInt(input.trim());
            if (n < 0 || n > 20) return "ERR: Chỉ hỗ trợ số từ 0 đến 20 để tránh tràn số long!";
            long gt = 1;
            for (int i = 2; i <= n; i++) gt *= i;
            return n + "! = " + gt;
        } catch (Exception e) {
            return "ERR: Dữ liệu không hợp lệ!";
        }
    }

    // H. Tính tổng các chữ số của một số nguyên
    // Ví dụ: "12345" -> 1 + 2 + 3 + 4 + 5 = 15
    public static String tongCacChuSo(String input) {
        try {
            String clean = input.trim().replaceAll("[^0-9]", "");
            if (clean.isEmpty()) return "ERR: Không có chữ số nào";
            int sum = 0;
            for (char c : clean.toCharArray()) {
                sum += (c - '0');
            }
            return "Tổng các chữ số của " + clean + " là: " + sum;
        } catch (Exception e) {
            return "ERR: Dữ liệu không hợp lệ!";
        }
    }
}
