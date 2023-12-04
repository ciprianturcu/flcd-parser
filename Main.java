import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void printMenu() {
        System.out.println("1 - Print the set of nonterminals");
        System.out.println("2 - Print the set of terminals");
        System.out.println("3 - Print the set of productions");
        System.out.println("4 - Print the productions of a given nonterminal");
        System.out.println("5 - CFG check");
        System.out.println("6 - LL1 parser FIRST set");
        System.out.println("7 - LL1 parser FOLLOW set");
        System.out.println("0 - EXIT");
    }

    public static void cases() {
        Grammar g = new Grammar();
        g.readFromFile("resources/g4.in");
        LL1 ll1 = new LL1(g);
        ll1.FIRST();
        ll1.FOLLOW();
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
                case "6" -> {
                    ll1.printFirstSet();
                }
                case "7" -> {
                    ll1.printFollowSet();
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
