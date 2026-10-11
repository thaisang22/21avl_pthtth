import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NhanVien {
    private int id;
    private String ten;
    private String chucVu;
    private double luong;

    public NhanVien() {
    }

    public NhanVien(int id, String ten, String chucVu, double luong) {
        this.id = id;
        this.ten = ten;
        this.chucVu = chucVu;
        this.luong = luong;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public double getLuong() {
        return luong;
    }

    public void setLuong(double luong) {
        this.luong = luong;
    }

    @Override
    public String toString() {
        return String.format("%d - %s - %s - %,.0f VND", id, ten, chucVu, luong);
    }

    // --- JDBC thuc hanh B1 -> B5 va Bai 5 ---
    private static final String URL = "jdbc:sqlite:nhanvien.db";

    public static void demoCoBan() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.createStatement();

            stmt.executeUpdate("DROP TABLE IF EXISTS NhanVien;");
            String sqlCreate = "CREATE TABLE IF NOT EXISTS NhanVien ("
                    + "id INTEGER PRIMARY KEY, "
                    + "ten TEXT NOT NULL, "
                    + "chuc_vu TEXT, "
                    + "luong REAL);";
            stmt.executeUpdate(sqlCreate);

            stmt.executeUpdate("INSERT INTO NhanVien VALUES (1, 'Nguyen Van An', 'Truong Phong', 25000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (2, 'Tran Thi Bich', 'Lap Trinh Vien', 18000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (3, 'Le Hoang Cuong', 'Lap Trinh Vien', 16500000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (4, 'Pham Thi Dung', 'Ke Toan', 14000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (5, 'Doan Minh Em', 'Tester', 13000000);");

            System.out.println("--- Danh sach nhan vien ---");
            rs = stmt.executeQuery("SELECT * FROM NhanVien;");
            while (rs.next()) {
                NhanVien nv = new NhanVien(
                        rs.getInt("id"),
                        rs.getString("ten"),
                        rs.getString("chuc_vu"),
                        rs.getDouble("luong"));
                System.out.println(nv);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Bai 5: Thong ke luong (SUM, AVG, MIN, MAX, COUNT va GROUP BY)
    public static void demoAggregation() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(URL);
            stmt = conn.createStatement();

            System.out.println("\n--- Thong ke luong (Bai 5) ---");

            // SUM, AVG, MIN, MAX
            String sqlStats = "SELECT SUM(luong) AS tong, AVG(luong) AS tb, MIN(luong) AS min_l, MAX(luong) AS max_l FROM NhanVien;";
            rs = stmt.executeQuery(sqlStats);
            if (rs.next()) {
                System.out.printf("Tong luong: %,.0f VND%n", rs.getDouble("tong"));
                System.out.printf("Luong trung binh: %,.0f VND%n", rs.getDouble("tb"));
                System.out.printf("Luong thap nhat: %,.0f VND%n", rs.getDouble("min_l"));
                System.out.printf("Luong cao nhat: %,.0f VND%n", rs.getDouble("max_l"));
            }
            rs.close();

            // Nhan vien luong cao nhat va thap nhat
            System.out.println("\nNhan vien luong cao nhat va thap nhat:");
            String sqlMinMax = "SELECT ten, chuc_vu, luong FROM NhanVien "
                    + "WHERE luong = (SELECT MAX(luong) FROM NhanVien) "
                    + "   OR luong = (SELECT MIN(luong) FROM NhanVien);";
            rs = stmt.executeQuery(sqlMinMax);
            while (rs.next()) {
                System.out.printf("- %s (%s): %,.0f VND%n",
                        rs.getString("ten"),
                        rs.getString("chuc_vu"),
                        rs.getDouble("luong"));
            }
            rs.close();

            // Dem so luong theo chuc vu
            System.out.println("\nSo luong nhan vien theo tung chuc vu:");
            String sqlGroup = "SELECT chuc_vu, COUNT(*) AS sl, AVG(luong) AS luong_tb FROM NhanVien GROUP BY chuc_vu;";
            rs = stmt.executeQuery(sqlGroup);
            while (rs.next()) {
                System.out.printf("- %s: %d nguoi (Luong TB: %,.0f VND)%n",
                        rs.getString("chuc_vu"),
                        rs.getInt("sl"),
                        rs.getDouble("luong_tb"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        demoCoBan();
        demoAggregation();
    }
}
