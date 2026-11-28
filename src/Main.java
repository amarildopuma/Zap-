import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            Menu menu = new Menu(scanner);
            menu.iniciar();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Entrada encerrada.");
        } finally {
            scanner.close();
        }
    }
}
