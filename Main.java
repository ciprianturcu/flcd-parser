import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void printMenu() {
        System.out.println("1 - Print the set of nonterminals");
        System.out.println("2 - Print the set of terminals");
        System.out.println("3 - Print the set of productions");
        System.out.println("4 - Print the productions of a given nonterminal");
        System.out.println("5 - CFG check");
        System.out.println("0 - EXIT");
    }

    public static void cases() {
        Grammar g = new Grammar();
        g.readFromFile("resources/g2.in");
        printMenu();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input the number of your option:");
        String option = scanner.nextLine();
        while (!Objects.equals(option, "0")) {
            switch (option) {
                case "0" -> {
                    return;
                }
                case "1" -> g.printNonTerminals();
                case "2" -> g.printTerminals();
                case "3" -> g.printProductions();
                case "4" -> {
                    System.out.println("Input the nonterminal:");
                    String nonTerminal = scanner.nextLine();
                    g.printProductionsForNonterminal(nonTerminal);
                }
                case "5" -> {
                    if (g.checkContextFreeGrammar()) System.out.println("The given grammar is a CFG!");
                    else System.out.println("The given grammar is NOT a CFG!");
                }
            }
            System.out.println();
            printMenu();
            System.out.println("Input the number of your option:");
            option = scanner.nextLine();
        }
    }
    public static void main(String[] args) {
        cases();
    }
}
