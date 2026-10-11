
import java.util.Scanner;

public class lab1 {

    public static void helloWord() {
        System.out.println("Hello word");
    }

    public static void nhapTenVaIn(Scanner sc) {
        System.out.println("What`s your name?");
        String hoVaTen = sc.nextLine();
        System.out.println("Ho va ten:" + hoVaTen);
    }

    public static void tongHaiSo(Scanner sc) {
        System.out.println("Nhap A:");
        int a = sc.nextInt();
        System.out.println("Nhap B:");
        int b = sc.nextInt();
        int kq = a + b;
        System.out.println("Tong cua 2 so a va b = " + kq);

    }

    public static void soChanHayLe(Scanner sc) {
        System.out.println("Kiem tra so chan le ");
        System.out.println("Nhap so can kiem tra");
        int numberCheck = sc.nextInt();

        if (numberCheck % 2 == 0) {
            System.out.println(numberCheck + " la so chan");
        } else {
            System.out.println(numberCheck + " la so le");
        }
    }

    public static void inRaThang(Scanner sc) {
        System.out.println("Vui long nhap thang (1 - 12):");
        if (!sc.hasNextInt()) {
            System.out.println("Vui long nhap mot so nguyen.");
            sc.next();
            return;
        }

        int thang = sc.nextInt();
        String tenThang = tenThang(thang);
        if (tenThang == null) {
            System.out.println("Vui long chi nhap so tu 1 - 12.");
        } else {
            System.out.println(tenThang);
        }
    }

    public static String tenThang(int thang) {
        String[] thangTrongNam = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        return thang >= 1 && thang <= 12 ? thangTrongNam[thang - 1] : null;
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        helloWord();
        nhapTenVaIn(sc);
        tongHaiSo(sc);
        soChanHayLe(sc);
        inRaThang(sc);
        sc.close();
    }
}
