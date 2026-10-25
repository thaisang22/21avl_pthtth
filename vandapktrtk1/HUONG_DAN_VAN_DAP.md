# TÀI LIỆU ÔN THI VẤN ĐÁP: HỆ THỐNG PHÒNG CHAT ĐA LUỒNG (TCP SOCKET)

> **Mục tiêu**: Nắm vững 100% bản chất lập trình mạng Socket, cơ chế Đa luồng (Multi-threading), giải thích trôi chảy từng dòng code và tự tin sửa code trực tiếp khi giảng viên đưa ra yêu cầu bổ sung.

---

## MỤC LỤC
1. [Kiến trúc tổng quan & Cấu trúc thư mục](#1-kiến-trúc-tổng-quan--cấu-trúc-thư-mục)
2. [Sơ đồ luồng hoạt động (Data Flow)](#2-sơ-đồ-luồng-hoạt-động-data-flow)
3. [Giải thích chi tiết từng File & Từng dòng Code](#3-giải-thích-chi-tiết-từng-file--từng-dòng-code)
4. [Bộ 10 câu hỏi & Đáp án vấn đáp kinh điển](#4-bộ-10-câu-hỏi--đáp-án-vấn-đáp-kinh-điển)
5. [6 tình huống giảng viên yêu cầu sửa code trực tiếp tại chỗ](#5-6-tình-huống-giảng-viên-yêu-cầu-sửa-code-trực-tiếp-tại-chỗ)
6. [Hướng dẫn chạy chương trình khi demo](#6-hướng-dẫn-chạy-chương-trình-khi-demo)

---

## 1. KIẾN TRÚC TỔNG QUAN & CẤU TRÚC THƯ MỤC

Hệ thống hoạt động theo mô hình **Client - Server** tập trung sử dụng giao thức **TCP Socket**:

| File | Vai trò | Trách nhiệm chính |
| :--- | :--- | :--- |
| **`ChatServer.java`** | Máy chủ trung tâm | - Mở cổng TCP `5000` (`ServerSocket`).<br>- Chứa danh sách phòng (`rooms`) và danh sách kết nối (`clients`).<br>- `server.accept()` đón nhận client và tạo `ClientHandler` thread riêng.<br>- Cung cấp 2 hàm phát sóng `broadcast()`. |
| **`ClientHandler.java`** | Luồng phục vụ 1 Client | - Kế thừa `Thread`, chạy nền cho từng client.<br>- Đọc/Ghi dữ liệu luồng UTF-8.<br>- Xác thực nickname (không rỗng, không trùng).<br>- Hiển thị menu phòng, xử lý chọn phòng / tự tạo phòng.<br>- Vòng lặp chat, lệnh `/quit` (về menu), `/exit` (thoát hẳn).<br>- Khối `finally` dọn dẹp tài nguyên sống còn. |
| **`ChatClient.java`** | Ứng dụng người dùng | - Tạo kết nối `Socket` tới Server `HOST:PORT`.<br>- **Sử dụng 2 Thread song song**:<br>  + *Thread 1 (Chính)*: Đọc bàn phím (`System.in`) -> Gửi lên Server.<br>  + *Thread 2 (Phụ)*: Đọc tin nhắn từ Server -> In ra màn hình console. |

---

## 2. SƠ ĐỒ LUỒNG HOẠT ĐỘNG (DATA FLOW)

```text
[ChatServer] (ServerSocket lắng nghe cổng 5000)
     │
     ├─► [Client A kết nối] ──► Tạo Thread ClientHandler A
     │                              ├─► Nhập Nickname: "An" (Kiểm tra trùng)
     │                              ├─► Chọn phòng: 1 ("Study")
     │                              └─► Chat loop: gửi tin -> broadcast vào "Study"
     │
     └─► [Client B kết nối] ──► Tạo Thread ClientHandler B
                                    ├─► Nhập Nickname: "Binh"
                                    ├─► Chọn phòng: "Study"
                                    └─► Chat loop: gửi tin -> broadcast vào "Study"
```

### Các bước trạng thái của 1 Client:
1. **Kết nối**: `Socket socket = new Socket("localhost", 5000);`
2. **Nhập Nickname**: Server kiểm tra rỗng / `/exit` / trùng tên với các client đang online.
3. **Chọn phòng**:
   - Nhập số thứ tự (ví dụ: `1`, `2`, `3`).
   - Hoặc nhập tên phòng có sẵn (ví dụ: `Study`, `Game`).
   - Nếu nhập tên phòng chưa có -> Hệ thống **tự động tạo phòng mới** và cho vào phòng đó.
4. **Vào phòng chat**:
   - Server gửi thông báo cho những người trong phòng: `name da tham gia phong.`
   - Nhập tin nhắn thông thường -> Server đóng gói `[name]: tin nhắn` rồi broadcast cho tất cả mọi người trong phòng.
5. **Rời phòng hoặc Thoát**:
   - Gõ `/quit`: Thoát khỏi phòng hiện tại -> Quay lại danh sách chọn phòng.
   - Gõ `/exit`: Ngắt kết nối hoàn toàn khỏi Server -> Khối `finally` dọn dẹp.

---

## 3. GIẢI THÍCH CHI TIẾT TỪNG FILE & TỪNG DÒNG CODE

### 3.1. `ChatServer.java`

- `public static ArrayList<String> rooms`: Lưu tên các phòng. Khởi tạo mặc định 3 phòng: `Study`, `Game`, `TraiNghiem`.
- `public static ArrayList<ClientHandler> clients`: Lưu tất cả các đối tượng `ClientHandler` đang kết nối.
- `countMembers(String room)`: Duyệt qua `clients`, đếm xem có bao nhiêu client có `room.equalsIgnoreCase(c.currentRoom)`.
- **Hàm `broadcast(String room, String msg)`**:
  - Duyệt toàn bộ `clients`. Nếu `c.currentRoom` trùng với `room` thì gọi `c.sendMessage(msg)`.
  - Dùng cho tin nhắn thông thường (người gửi cũng nhận được tin của chính mình để xác nhận tin đã gửi).
- **Hàm `broadcast(String room, String msg, ClientHandler except)`**:
  - Nạp chồng (Overload), có thêm điều kiện `c != except`.
  - Dùng khi một người rời phòng (`/quit` hoặc mất mạng): Báo cho những người khác trong phòng biết, không gửi lại cho người vừa rời.
- **Hàm `main`**:
  ```java
  ServerSocket server = new ServerSocket(PORT); // Mở cổng
  while (true) {
      Socket socket = server.accept();          // Chờ client kết nối (hàm Blocking)
      ClientHandler client = new ClientHandler(socket);
      clients.add(client);
      client.start();                           // Khởi động luồng mới cho client
  }
  ```

---

### 3.2. `ClientHandler.java`

- **Thuộc tính**:
  - `socket`: Socket kết nối với client.
  - `in`: Đọc từ socket (`BufferedReader`).
  - `out`: Gửi về socket (`PrintWriter` với `autoFlush = true`).
  - `name`: Tên đăng nhập của người dùng.
  - `currentRoom`: Tên phòng hiện tại mà người này đang ở (null nếu đang ở menu).
- **Khởi tạo luồng I/O UTF-8**:
  ```java
  in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
  out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
  ```
  *Ý nghĩa*: Đảm bảo gửi nhận tiếng Việt không bao giờ bị lỗi font (ký tự `?` hoặc ô vuông).
- **Vòng lặp xác thực Nickname**:
  - Kiểm tra `name == null` (client tắt ngang).
  - Kiểm tra `/exit`.
  - Kiểm tra `name.isEmpty()`.
  - Duyệt `ChatServer.clients` kiểm tra trùng tên:
    ```java
    if (c != this && c.name != null && c.name.equalsIgnoreCase(name)) { trung = true; }
    ```
- **Vòng lặp Menu chọn phòng**:
  - In ra các phòng kèm số người: `ChatServer.countMembers(r)`.
  - Parse số thứ tự `Integer.parseInt(line)`.
  - Tìm theo tên phòng.
  - Nếu không trùng phòng nào: `ChatServer.rooms.add(selectedRoom);` -> Tự động sinh phòng mới.
- **Vòng lặp Chat trong phòng**:
  - Đọc liên tục: `while ((msg = in.readLine()) != null)`.
  - Nếu `msg.equalsIgnoreCase("/quit")`: đặt `currentRoom = null; quit = true; break;` -> Trở lại menu chọn phòng.
  - Nếu `msg.equalsIgnoreCase("/exit")`: thoát vòng lặp chat với `quit = false` -> Ra ngoài thoát hẳn.
  - Bình thường: `ChatServer.broadcast(currentRoom, "[" + name + "]: " + msg);`.
- **Khối `finally` (DỌN DẸP TÀI NGUYÊN)**:
  - Nếu client mất kết nối khi đang ở trong phòng: broadcast báo rời phòng.
  - `ChatServer.clients.remove(this)`: Xóa khỏi danh sách server để tránh gửi tin vào socket chết.
  - `socket.close()`: Giải phóng cổng mạng trên hệ điều hành.

---

### 3.3. `ChatClient.java`

- **Tạo kết nối**: `Socket socket = new Socket(HOST, PORT);`
- **Khởi tạo 3 luồng stream**:
  - `console`: Đọc phím người dùng gõ.
  - `in`: Nhận tin nhắn từ server.
  - `out`: Đẩy tin nhắn lên server (`autoFlush = true`).
- **Tại sao cần luồng phụ `readThread`**:
  ```java
  Thread readThread = new Thread(new Runnable() {
      public void run() {
          String msg;
          while ((msg = in.readLine()) != null) {
              if (msg.endsWith(": ")) {
                  System.out.print(msg);  // Để con trỏ chuột ở cùng hàng sau dấu :
              } else {
                  System.out.println(msg);
              }
          }
      }
  });
  readThread.start();
  ```
  *Bản chất*: Lệnh `readLine()` là hàm **chặn (blocking)**. Nếu không tách luồng, khi chương trình đang đợi người dùng gõ phím ở luồng chính, nó sẽ không thể đọc được tin nhắn mà người khác vừa chat về!
- **`Thread.sleep(200)` khi gõ `/exit`**: Đảm bảo gói tin `/exit` đã kịp truyền qua mạng đến Server trước khi hàm `socket.close()` ngắt kết nối.

---

## 4. BỘ 10 CÂU HỎI & ĐÁP ÁN VẤN ĐÁP KINH ĐIỂN

### Câu 1: Socket là gì? Khác biệt giữa Socket và ServerSocket?
- **Trả lời**:
  - **Socket** là một đầu mút (endpoint) của kênh truyền thông hai chiều giữa 2 tiến trình qua mạng (được định danh bởi địa chỉ IP + số cổng Port).
  - **ServerSocket** là lớp chỉ nằm ở phía Server, có nhiệm vụ bind vào một cổng cố định (vd: 5000) và lắng nghe yêu cầu kết nối từ Client thông qua hàm `accept()`. Khi có kết nối, `accept()` trả về một đối tượng `Socket` thông thường để Server truyền nhận dữ liệu trực tiếp với Client đó.

---

### Câu 2: Tại sao ứng dụng Chat Room này chọn TCP thay vì UDP?
- **Trả lời**:
  - **TCP (Transmission Control Protocol)** là giao thức hướng kết nối (Connection-oriented), đảm bảo dữ liệu truyền đi **chính xác 100%, đúng thứ tự, không bị mất gói tin** nhờ cơ chế bắt tay 3 bước và truyền lại khi lỗi (ACK). Ứng dụng chat yêu cầu tin nhắn không được mất chữ và đúng thứ tự hội thoại nên TCP là lựa chọn chuẩn xác.
  - **UDP (User Datagram Protocol)** không hướng kết nối, gửi gói tin nhanh nhưng không đảm bảo tới đích hay đúng thứ tự, chỉ phù hợp cho truyền video trực tiếp, livestream hoặc game online.

---

### Câu 3: Tại sao bắt buộc phải dùng Đa luồng (Multi-threading) trên Server? Nếu không dùng thì sao?
- **Trả lời**:
  - Hàm `server.accept()` và `in.readLine()` đều là các thao tác **I/O Blocking** (nó sẽ dừng tiểu trình hiện tại chờ cho đến khi có dữ liệu).
  - Nếu Server **đơn luồng (Single-thread)**: Khi Client 1 kết nối vào, Server bị cuốn vào vòng lặp phục vụ Client 1. Lúc này không có Client nào khác có thể kết nối vào Server được nữa; toàn bộ hệ thống bị tắc nghẽn (freeze).
  - Khi dùng Đa luồng: Mỗi client kết nối đến được gán vào 1 `ClientHandler` chạy trên 1 luồng độc lập, giải phóng luồng chính của Server tiếp tục quay lại `accept()` đón nhận các client tiếp theo.

---

### Câu 4: Tại sao phía Client cũng phải dùng 2 luồng (Thread)?
- **Trả lời**:
  - Trên Client có 2 hành động chặn đồng thời:
    1. Đọc dữ liệu từ bàn phím người dùng: `console.readLine()` (chờ người dùng gõ).
    2. Đọc tin nhắn từ Server truyền về: `in.readLine()` (chờ server gửi).
  - Nếu chỉ có 1 luồng, khi người dùng đang suy nghĩ chưa gõ phím, hàm `console.readLine()` sẽ chiếm giữ luồng, Client hoàn toàn không thể nhận hay hiển thị tin nhắn của những người khác đang chat trong phòng. Tách thành 2 luồng giúp việc nhận tin diễn ra tức thời (bất đồng bộ).

---

### Câu 5: Hiện tượng Race Condition trên `ArrayList<ClientHandler> clients` là gì? Cách khắc phục?
- **Trả lời**:
  - Trong code hiện tại, `ArrayList` là cấu trúc dữ liệu **không an toàn đa luồng (Not Thread-Safe)**. Khi có nhiều client cùng lúc ngắt kết nối (`clients.remove()`) trong khi Server đang chạy vòng lặp `broadcast` (`for (ClientHandler c : clients)`), chương trình có thể văng lỗi `ConcurrentModificationException`.
  - **Cách khắc phục**:
    1. Dùng `CopyOnWriteArrayList<ClientHandler>` thay cho `ArrayList` thông thường (phù hợp nhất cho bài toán đọc nhiều, ghi ít).
    2. Hoặc bọc danh sách bằng: `Collections.synchronizedList(new ArrayList<>())`.
    3. Hoặc dùng khối đồng bộ: `synchronized(clients) { ... }`.

---

### Câu 6: Cơ chế Broadcast hoạt động như thế nào? Nêu sự khác biệt giữa 2 hàm broadcast trong bài?
- **Trả lời**:
  - Bản chất Broadcast là duyệt qua danh sách các client đang kết nối và gọi hàm gửi tin nhắn (`sendMessage`) tới các client thỏa mãn điều kiện cùng phòng.
  - **Khác biệt**:
    + `broadcast(room, msg)`: Gửi cho **tất cả** người trong phòng (dùng cho chat thường).
    + `broadcast(room, msg, except)`: Gửi cho mọi người trong phòng **ngoại trừ** client được truyền vào (thường dùng khi thông báo ai đó rời phòng, người rời không cần nhận lại thông báo của chính mình).

---

### Câu 7: Ý nghĩa của tham số `true` trong `new PrintWriter(..., true)` là gì?
- **Trả lời**:
  - Tham số `true` bật cờ **`autoFlush`**. Khi cờ này bật, mỗi khi gọi phương thức `println()`, `printf()` hoặc `format()`, bộ đệm output sẽ tự động xả (flush) dữ liệu đi qua mạng ngay lập tức mà không cần phải gọi thủ công lệnh `out.flush()`. Nếu không có cờ này, dữ liệu có thể bị ứ đọng lại trong bộ đệm khiến phía bên kia không nhận được tin nhắn.

---

### Câu 8: Tại sao phải chỉ định bảng mã `"UTF-8"` khi khởi tạo Reader/Writer?
- **Trả lời**:
  - Mặc định nếu không chỉ định mã hóa, Java sẽ dùng `Charset` mặc định của hệ điều hành (trên Windows tiếng Việt thường là `Windows-1258` hoặc `Cp1252`). Nếu Client và Server chạy trên các hệ điều hành khác nhau (hoặc cài đặt ngôn ngữ khác nhau), tin nhắn tiếng Việt có dấu sẽ bị biến thành ký tự lạ hoặc dấu hỏi chấm `???`. Việc ép cứng `"UTF-8"` đảm bảo đồng nhất truyền tải tiếng Việt có dấu trên mọi môi trường.

---

### Câu 9: Khối `finally` trong `ClientHandler` giữ vai trò gì?
- **Trả lời**:
  - Khối `finally` luôn luôn được thực thi kể cả khi code chạy bình thường hay phát sinh lỗi (ngắt mạng đột ngột).
  - Nó thực hiện 3 nhiệm vụ dọn dẹp sống còn:
    1. Thông báo cho những người còn lại trong phòng biết người này đã rời phòng.
    2. Xóa đối tượng client khỏi danh sách `ChatServer.clients` để tránh gửi tin vào socket chết gây lỗi rò rỉ bộ nhớ.
    3. Đóng socket (`socket.close()`) để trả lại tài nguyên cổng kết nối cho hệ điều hành.

---

### Câu 10: Xử lý giao diện dòng lệnh: Dòng `if (msg.endsWith(": "))` trong Client có ý nghĩa gì?
- **Trả lời**:
  - Khi Server gửi các câu hỏi nhập liệu như `"Nhap ten: "` hoặc `"Chon phong: "`, tin nhắn này kết thúc bằng `": "`.
  - Phía Client dùng `System.out.print(msg)` thay vì `System.out.println(msg)` để con trỏ chuột không bị nhảy xuống dòng mới, giúp người dùng gõ ngay sau dấu hai chấm, tạo cảm giác giao diện thân thiện, trực quan giống hệt các ứng dụng console chuyên nghiệp.

---

## 5. 6 TÌNH HUỐNG GIẢNG VIÊN YÊU CẦU SỬA CODE TRỰC TIẾP TẠI CHỖ

### Tình huống 1: Thêm tính năng Chat Riêng Tư (Thì thầm) `/pm <tên> <nội dung>`
- **Yêu cầu**: Người dùng trong phòng gõ `/pm Binh Alo ban oi` thì chỉ có `Binh` nhận được, kèm thông báo gửi thành công cho người gửi.
- **Vị trí sửa**: Trong `ClientHandler.java`, bên trong vòng lặp chat `while ((msg = in.readLine()) != null)`:
```java
// Thêm đoạn này trước lệnh broadcast bình thường:
if (msg.startsWith("/pm ")) {
    String[] parts = msg.split(" ", 3);
    if (parts.length >= 3) {
        String targetUser = parts[1];
        String privateMsg = parts[2];
        boolean found = false;

        for (ClientHandler c : ChatServer.clients) {
            if (c.name.equalsIgnoreCase(targetUser)) {
                c.sendMessage("[Tin riêng tu " + name + "]: " + privateMsg);
                this.sendMessage("[Ban -> " + targetUser + "]: " + privateMsg);
                found = true;
                break;
            }
        }
        if (!found) {
            this.sendMessage("Khong tim thay nguoi dung: " + targetUser);
        }
    } else {
        this.sendMessage("Cu phap dung: /pm <ten> <noi dung>");
    }
    continue; // Bo qua broadcast chung
}
```

---

### Tình huống 2: Thêm lệnh xem danh sách người trong phòng `/list` hoặc `/who`
- **Yêu cầu**: Khi đang ở trong phòng, gõ `/list` sẽ in ra danh sách nickname của tất cả người đang ở cùng phòng đó.
- **Vị trí sửa**: Trong `ClientHandler.java`, bên trong vòng lặp chat:
```java
if (msg.equalsIgnoreCase("/list") || msg.equalsIgnoreCase("/who")) {
    StringBuilder sb = new StringBuilder("Thanh vien trong phong " + currentRoom + ": ");
    for (ClientHandler c : ChatServer.clients) {
        if (currentRoom.equalsIgnoreCase(c.currentRoom)) {
            sb.append(c.name).append(" ");
        }
    }
    this.sendMessage(sb.toString());
    continue; // Khong broadcast tin nay ra phong
}
```

---

### Tình huống 3: Giới hạn số người tối đa trong một phòng (Ví dụ: Tối đa 3 người)
- **Yêu cầu**: Phòng chỉ cho phép tối đa 3 người. Nếu đã đủ 3 người thì không cho vào và bắt chọn lại.
- **Vị trí sửa**: Trong `ClientHandler.java`, ngay sau đoạn xác định được `selectedRoom`:
```java
// Kiểm tra số lượng người trong phòng trước khi cho vào
int currentCount = ChatServer.countMembers(selectedRoom);
if (currentCount >= 3) {
    out.println("Phong " + selectedRoom + " da day (Toi da 3 nguoi), vui long chon phong khac!\n");
    continue; // Quay lại đầu vòng lặp Menu chọn phòng
}
```

---

### Tình huống 4: CẤM tự tạo phòng mới (Chỉ được chọn phòng có sẵn)
- **Yêu cầu**: Nếu người dùng nhập tên phòng không có trong danh sách thì báo lỗi chứ không tự tạo phòng mới.
- **Vị trí sửa**: Trong `ClientHandler.java`, phần Cơ chế 3:
```java
// Sửa đoạn tạo phòng mới thành báo lỗi:
if (selectedRoom == null) {
    out.println("Phong khong hop le! Vui long chi chon phong co trong danh sach.\n");
    continue; // Quay lại menu chọn phòng
}
```

---

### Tình huống 5: Thêm hiển thị thời gian gửi tin nhắn (Timestamp)
- **Yêu cầu**: Mỗi tin nhắn chat hiển thị thêm giờ phút giây, ví dụ: `[14:35:20] [An]: Xin chao`.
- **Vị trí sửa**: Trong `ClientHandler.java`, khi phát tin nhắn:
```java
// Import thêm: java.time.LocalTime; java.time.format.DateTimeFormatter;
String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
ChatServer.broadcast(currentRoom, "[" + time + "] [" + name + "]: " + msg);
```

---

### Tình huống 6: Chạy Client trên máy khác trong mạng LAN
- **Yêu cầu**: Kết nối 2 máy tính thật với nhau.
- **Cách làm**:
  1. Trên máy Server: Mở CMD gõ `ipconfig`, tìm dòng `IPv4 Address` (ví dụ: `192.168.1.15`).
  2. Mở cổng tường lửa Windows (Firewall) cho cổng `5000`.
  3. Trên máy Client: Mở file `ChatClient.java`, sửa:
     ```java
     public static String HOST = "192.168.1.15"; // Thay bang IP cua may Server
     ```
  4. Biên dịch và chạy `java ChatClient`.

---

## 6. HƯỚNG DẪN CHẠY CHƯƠNG TRÌNH KHI DEMO

Mở PowerShell / CMD tại thư mục `vandapktrtk1`:

```powershell
# 1. Di chuyển vào thư mục
cd d:\IUHCNTT\IUHHK3\phattrienhethongtichhop\vandapktrtk1

# 2. Biên dịch toàn bộ bằng mã UTF-8
javac -encoding UTF-8 *.java

# 3. Cửa sổ 1: Khởi động Server
java ChatServer

# 4. Cửa sổ 2: Mở Client 1 (vd: Đặt tên An, chọn phòng 1)
java ChatClient

# 5. Cửa sổ 3: Mở Client 2 (vd: Đặt tên Binh, chọn phòng 1)
java ChatClient
```
*Lúc này gõ chat ở Client 1 thì Client 2 sẽ nhận được ngay lập tức và ngược lại.*
