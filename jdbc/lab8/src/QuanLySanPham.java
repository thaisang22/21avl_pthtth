import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QuanLySanPham {
    private static final String URL = "jdbc:sqlite:sanpham.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Dong tai nguyen trong finally de tranh ro ri bo nho (Bai 3)
    public static void dongKetNoi(ResultSet rs, Statement stmt, Connection conn) {
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

    public static void taoBang() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS SanPham;");
            String sql = "CREATE TABLE IF NOT EXISTS SanPham ("
                    + "maSP TEXT PRIMARY KEY, "
                    + "tenSP TEXT NOT NULL, "
                    + "gia REAL NOT NULL, "
                    + "soluong INTEGER NOT NULL DEFAULT 0);";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dongKetNoi(null, stmt, conn);
        }
    }

    // Bai 1 & 4: Create (Them san pham bang PreparedStatement)
    public static void themSanPham(String maSP, String tenSP, double gia, int soLuong) {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "INSERT INTO SanPham (maSP, tenSP, gia, soluong) VALUES (?, ?, ?, ?);";
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, maSP);
            ps.setString(2, tenSP);
            ps.setDouble(3, gia);
            ps.setInt(4, soLuong);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dongKetNoi(null, ps, conn);
        }
    }

    // Bai 1 & 4: Read (Liet ke tat ca san pham)
    public static void lietKeTatCa() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM SanPham;";
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            System.out.println("--- Danh sach san pham ---");
            while (rs.next()) {
                System.out.printf("%s - %s - %,.0f VND - SL: %d%n",
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getDouble("gia"),
                        rs.getInt("soluong"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dongKetNoi(rs, ps, conn);
        }
    }

    // Bai 1 & 4: Read (Tim kiem theo ten)
    public static void timKiemTheoTen(String tenSP) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM SanPham WHERE tenSP LIKE ?;";
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + tenSP + "%");
            rs = ps.executeQuery();
            System.out.println("Ket qua tim kiem: " + tenSP);
            while (rs.next()) {
                System.out.printf("%s - %s - %,.0f VND - SL: %d%n",
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getDouble("gia"),
                        rs.getInt("soluong"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dongKetNoi(rs, ps, conn);
        }
    }

    // Bai 1 & 4: Update (Cap nhat gia va so luong)
    public static void capNhatSanPham(String maSP, double giaMoi, int soLuongMoi) {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "UPDATE SanPham SET gia = ?, soluong = ? WHERE maSP = ?;";
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, giaMoi);
            ps.setInt(2, soLuongMoi);
            ps.setString(3, maSP);
            ps.executeUpdate();
            System.out.println("Cap nhat thanh cong ma: " + maSP);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dongKetNoi(null, ps, conn);
        }
    }

    // Bai 1 & 4: Delete (Xoa san pham theo ma)
    public static void xoaSanPham(String maSP) {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "DELETE FROM SanPham WHERE maSP = ?;";
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, maSP);
            ps.executeUpdate();
            System.out.println("Da xoa san pham ma: " + maSP);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dongKetNoi(null, ps, conn);
        }
    }

    // Bai 2: Transaction khi cap nhat du lieu (xuat kho)
    public static void capNhatSoLuongTransaction(String[] dsMa, int[] dsSoLuongXuat) {
        Connection conn = null;
        PreparedStatement psCheck = null;
        PreparedStatement psUpdate = null;
        ResultSet rs = null;

        String sqlCheck = "SELECT soluong FROM SanPham WHERE maSP = ?;";
        String sqlUpdate = "UPDATE SanPham SET soluong = soluong - ? WHERE maSP = ?;";

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bat dau transaction

            psCheck = conn.prepareStatement(sqlCheck);
            psUpdate = conn.prepareStatement(sqlUpdate);

            for (int i = 0; i < dsMa.length; i++) {
                String ma = dsMa[i];
                int slXuat = dsSoLuongXuat[i];

                psCheck.setString(1, ma);
                rs = psCheck.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Khong tim thay ma: " + ma);
                }
                int tonKho = rs.getInt("soluong");
                rs.close();

                if (tonKho < slXuat) {
                    throw new SQLException("SanPham " + ma + " khong du hang (ton: " + tonKho + ", can: " + slXuat + ")");
                }

                psUpdate.setInt(1, slXuat);
                psUpdate.setString(2, ma);
                psUpdate.executeUpdate();
            }

            conn.commit();
            System.out.println("Transaction thanh cong: Da cap nhat!");

        } catch (SQLException e) {
            System.out.println("Loi transaction: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Da rollback toan bo thay doi.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
            dongKetNoi(rs, psCheck, null);
            dongKetNoi(null, psUpdate, conn);
        }
    }

    public static void main(String[] args) {
        taoBang();

        // 1. Create (Them it nhat 3 san pham)
        themSanPham("SP01", "Laptop Dell XPS 13", 28500000, 10);
        themSanPham("SP02", "iPhone 15 Pro Max", 31900000, 15);
        themSanPham("SP03", "Ban Phim Co Keychron", 1850000, 30);
        themSanPham("SP04", "Chuot Logitech MX Master", 2350000, 25);
        themSanPham("SP05", "Tai Nghe Sony", 7490000, 12);

        // 2. Read (Liet ke)
        lietKeTatCa();

        // 3. Read (Tim kiem theo ten)
        System.out.println();
        timKiemTheoTen("Dell");

        // 4. Update (Cap nhat gia va so luong)
        System.out.println();
        capNhatSanPham("SP03", 1750000, 50);

        // 5. Delete (Xoa san pham)
        System.out.println();
        xoaSanPham("SP05");
        lietKeTatCa();

        // 6. Transaction (Cap nhat hop le)
        System.out.println("\n--- Test transaction thanh cong ---");
        capNhatSoLuongTransaction(new String[]{"SP01", "SP04"}, new int[]{2, 5});
        lietKeTatCa();

        // 7. Transaction (Cap nhat loi de kiem tra rollback)
        System.out.println("\n--- Test transaction rollback ---");
        capNhatSoLuongTransaction(new String[]{"SP01", "SP02"}, new int[]{1, 100});
        lietKeTatCa();
    }
}
