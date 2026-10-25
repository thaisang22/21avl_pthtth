# 🚀 HƯỚNG DẪN ĐỔI DỊCH VỤ TRONG PHÒNG THI (MẤT ĐÚNG 15 GIÂY)
*(Thư mục ONTAP: Khung bài thi chuẩn FIT - IUH đạt điểm tối đa)*

---

## 📂 KIẾN TRÚC THƯ MỤC `ONTAP/`

```text
ONTAP/
├── DichVuThuVien.java   --> NƠI CHỨA MỌI THUẬT TOÁN (Đổi số, Ngày giờ, CALC, Log, Đảo chuỗi...)
├── DichVuChat.java     --> BỘ QUẢN LÝ CHAT PHÒNG VÀ BROADCAST (Dùng chung TCP & UDP)
├── ServerTCP.java       --> Khung chuẩn đề thi TCP (Đa luồng, Menu, Dấu '.', Lặp tới 'EXIT.')
├── ClientTCP.java       --> Khung chuẩn Client TCP (Nhận phản hồi ngầm, Chat theo phòng)
├── ServerUDP.java       --> Khung chuẩn Server UDP (Điều phối dịch vụ & Chat UDP)
└── ClientUDP.java       --> Khung chuẩn Client UDP
```

---

## ⚡ CÁCH ĐỔI SANG BÀI THI KHÁC TRONG `ServerTCP.java`:

Nếu giáo viên đổi đề bài sang một bài khác (ví dụ: Máy tính, Đổi số, Ngày giờ), bạn **KHÔNG CẦN VIẾT LẠI CODE SERVER**. Chỉ cần sửa đúng **2 chỗ** trong file `ServerTCP.java`:

### BƯỚC 1: Sửa nội dung Menu hiển thị (Dòng ~90)
```java
out.println("----- DANH SACH DICH VU -----");
out.println("1. Doi chu so thanh chu");        // Sửa dòng mô tả menu
out.println("2. Xem ngay gio he thong");
out.println("3. May tinh tu xa (CALC)");
out.println("4. Ghi log nhat ky");
out.println("Nhap 'EXIT.' de dong ket noi");
```

---

### BƯỚC 2: Sửa gọi hàm trong `switch (trimmed)` (Dòng ~230)
Tất cả hàm đã được viết sẵn trong `DichVuThuVien.java`, bạn chỉ cần đổi tên hàm:

```java
switch (trimmed) {
    case "1":
        // Ví dụ Bài 2 (Đổi số):
        for (String l : lines) gui(DichVuThuVien.xuLyDoiSo(l));
        break;

    case "2":
        // Ví dụ Bài 3 (Ngày giờ):
        for (String l : lines) gui(DichVuThuVien.xuLyNgayGio(l));
        break;

    case "3":
        // Ví dụ Bài 4 (Máy tính CALC):
        for (String l : lines) gui(DichVuThuVien.xuLyMayTinh(l));
        break;

    case "4":
        // Ví dụ Bài 7 (Lưu log):
        for (String l : lines) gui(DichVuThuVien.xuLyGhiLog("client01", socket.getRemoteSocketAddress().toString(), l));
        break;
}
```

---

## 🧪 LỆNH CHẠY TRONG PHÒNG THI:

### Đối với TCP:
```powershell
# Terminal 1 (Server):
cd d:\IUHCNTT\IUHHK3\phattrienhethongtichhop\ONTAP
javac -encoding UTF-8 *.java
java ServerTCP

# Terminal 2 (Client):
cd d:\IUHCNTT\IUHHK3\phattrienhethongtichhop\ONTAP
java ClientTCP
```

### Đối với UDP:
```powershell
# Terminal 1 (Server):
cd d:\IUHCNTT\IUHHK3\phattrienhethongtichhop\ONTAP
javac -encoding UTF-8 *.java
java ServerUDP

# Terminal 2 (Client):
cd d:\IUHCNTT\IUHHK3\phattrienhethongtichhop\ONTAP
java ClientUDP
```
