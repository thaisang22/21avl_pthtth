# HƯỚNG DẪN SỬ DỤNG BỘ CODE ÔN TẬP DỊCH VỤ (KHÔNG CHAT)

Bộ code trong thư mục `ontap_dichvu/` được tinh giản 100%, **loại bỏ hoàn toàn dịch vụ Chat** để bạn tập trung trọn vẹn vào việc xử lý các bài toán logic dịch vụ khi đi thi.

---

## 1. Cấu trúc thư mục

| Tên File | Vai trò | Mô tả |
| :--- | :--- | :--- |
| **`DichVuThuVien.java`** | Thư viện 15 dịch vụ | Chứa toàn bộ hàm giải quyết các đề thi: Đổi số, Ngày giờ, Máy tính, Xử lý chuỗi, Ghi log, v.v. |
| **`ServerTCP.java`** | Server TCP | Sử dụng `ServerSocket` + `ThreadPool`. Hỗ trợ nhập nhiều dòng kết thúc bằng `.` theo Đề 7000 và các lệnh trực tiếp. |
| **`ClientTCP.java`** | Client TCP | Luồng nhận kết quả bất đồng bộ + luồng gửi lệnh có giao diện Prompt trực quan. |
| **`ServerUDP.java`** | Server UDP | Sử dụng `DatagramSocket` + `DatagramPacket`. Xử lý từng gói tin độc lập không cần trạng thái. |
| **`ClientUDP.java`** | Client UDP | Gửi gói tin DatagramPacket và nhận kết quả với bộ đếm timeout 5s chống treo. |

---

## 2. Hướng dẫn biên dịch và chạy

### Cách 1: Biên dịch bằng dòng lệnh (Terminal / CMD)

Mở terminal tại thư mục `ontap_dichvu`:

```powershell
# Di chuyển vào thư mục
cd d:\IUHCNTT\IUHHK3\phattrienhethongtichhop\ontap_dichvu

# Biên dịch tất cả file với mã hóa UTF-8 (hỗ trợ tiếng Việt không lỗi font)
javac -encoding UTF-8 *.java
```

### Chạy phiên bản TCP:
```powershell
# Cửa sổ 1: Chạy Server TCP
java ServerTCP

# Cửa sổ 2: Chạy Client TCP
java ClientTCP
```

### Chạy phiên bản UDP:
```powershell
# Cửa sổ 1: Chạy Server UDP
java ServerUDP

# Cửa sổ 2: Chạy Client UDP
java ClientUDP
```

---

## 3. Cú pháp gọi các dịch vụ khi chạy

| Dịch vụ / Đề thi | Cú pháp gọi trên TCP | Cú pháp gọi trên UDP | Kết quả mẫu |
| :--- | :--- | :--- | :--- |
| **DV1: Đảo chuỗi toàn bộ** | Gõ `1` -> Enter -> Nhập văn bản -> gõ `.` | `1 Phat Trien He Thong` | `Gnoht Eh Neirt Tahp` |
| **DV2: Đảo từ giữ vị trí** | Gõ `2` -> Enter -> Nhập văn bản -> gõ `.` | `2 Phat Trien He Thong` | `Tahp Neirt Eh Gnoht` |
| **DV3: Đếm tổng số từ** | Gõ `3` -> Enter -> Nhập các dòng -> gõ `.` | `3 Xin chao sinh vien` | `Tổng số từ: 4 từ` |
| **DV4: Đếm từ từng dòng** | Gõ `4` -> Enter -> Nhập các dòng -> gõ `.` | `4 Dong mot\nDong hai` | `Dòng 1: 2 từ \| Dòng 2: 2 từ` |
| **In HOA / In thường** | `UPPER <chuỗi>`<br>`LOWER <chuỗi>` | `UPPER <chuỗi>`<br>`LOWER <chuỗi>` | `HELLO WORLD`<br>`hello world` |
| **Bài 2: Đổi số** | `DIGIT 5` hoặc gõ thẳng `5` | `DIGIT 5` hoặc `5` | `Năm` |
| **Bài 3: Ngày giờ** | `DATE`, `TIME` hoặc `DATETIME` | `DATE`, `TIME`, `DATETIME` | `18 10 2026` |
| **Bài 4: Máy tính** | `CALC + 10 20`<br>`CALC / 10 0` | `CALC + 10 20`<br>`CALC / 10 0` | `OK 30`<br>`ERR DIV_ZERO: Khong the chia cho 0` |
| **Bài 7: Ghi nhật ký** | `LOG Sinh vien nop bai` | `LOG Sinh vien nop bai` | `SAVED: Sinh vien nop bai` |
| **Toán học: SNT, Giai thừa** | `PRIME 17`<br>`FACTORIAL 5` | `PRIME 17`<br>`FACTORIAL 5` | `17 LÀ SỐ NGUYÊN TỐ`<br>`5! = 120` |
| **Xem menu trợ giúp** | `MENU` hoặc `HELP` | `MENU` hoặc `HELP` | In bảng hướng dẫn |
| **Thoát chương trình** | `0` hoặc `EXIT` | `0` hoặc `EXIT` | Tắt kết nối |

---

## 4. Cách thêm hoặc sửa một dịch vụ mới trong 10 giây

Nếu đề thi yêu cầu một dịch vụ mới (ví dụ: kiểm tra số nguyên tố, giải phương trình bậc 2...):

1. **Bước 1**: Mở `DichVuThuVien.java`, thêm hàm static xử lý:
   ```java
   public static String kiemTraNguyenTo(int n) {
       if (n < 2) return n + " không phải số nguyên tố";
       for (int i = 2; i <= Math.sqrt(n); i++) {
           if (n % i == 0) return n + " không phải số nguyên tố";
       }
       return n + " là số nguyên tố";
   }
   ```
2. **Bước 2**: Trong hàm `xuLyYeuCau(String input)` của `DichVuThuVien.java`, thêm nhánh:
   ```java
   if (upper.startsWith("PRIME ") || upper.startsWith("NT ")) {
       int n = Integer.parseInt(tokens[1]);
       return kiemTraNguyenTo(n);
   }
   ```
3. **Cả TCP Server và UDP Server sẽ lập tức tự động hỗ trợ dịch vụ mới này** mà bạn không cần phải sửa đổi thêm mã nguồn socket!
