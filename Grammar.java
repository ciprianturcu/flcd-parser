import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Grammar {
    private List<String> nonTerminals ;
    private List<String> terminals;
    private String startSymbol;
    private Map<String, List<List<String>>> productions;

    private String epsilon = "epsilon";

    public Grammar() {
        nonTerminals = new ArrayList<>();
        terminals = new ArrayList<>();
        productions = new HashMap<>();
    }

    public List<String> getNonTerminals() {
        return nonTerminals;
    }

    public List<String> getTerminals() {
        return terminals;
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    public Map<String, List<List<String>>> getProductions() {
        return productions;
    }

    public String getEpsilon() {
        return epsilon;
    }

    public List<List<String>> getProductionForNonterminal(String nonterminal) {
        return productions.get(nonterminal);
    }

    public void readFromFile(String filename) {
        String line;
        try (FileReader fileReader = new FileReader(filename); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            while ((line = bufferedReader.readLine()) != null) {
                switch (line) {
                    case "~nonTerminals" -> nonTerminals.addAll(List.of(bufferedReader.readLine().split(" ")));
                    case "~terminals" -> terminals.addAll(List.of(bufferedReader.readLine().split(" ")));
                    case "~startSymbol" -> startSymbol = bufferedReader.readLine().strip();
                    case "~productions" -> readProductions(bufferedReader);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readProductions(BufferedReader bufferedReader) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String source = line.split("->")[0].strip();
            String sequence = line.split("->")[1];
            if(productions.containsKey(source)) {
                productions.get(source).add(Arrays.asList(sequence.strip().split(" ")));
            }
            else {
                List<List<String>> productionList = new ArrayList<>();
                productionList.add(List.of(sequence.strip().split(" ")));
                productions.put(source, productionList);
            }
        }
    }

    public Map<String, Set<List<String>>> getProductionsWithNonTerminalInRHS(String nonTerminal) {
        var productionsWithNonTerminalInRHS = new HashMap<String, Set<List<String>>>();
        productions.forEach((leftSideOfProduction, rightSideOfProduction)->{
            for(var production : rightSideOfProduction) {
                if(production.contains(nonTerminal)){
                    if(!productionsWithNonTerminalInRHS.containsKey(leftSideOfProduction))
                        productionsWithNonTerminalInRHS.put(leftSideOfProduction, new HashSet<>());
                    productionsWithNonTerminalInRHS.get(leftSideOfProduction).add(production);
                }
            }
        });
        return productionsWithNonTerminalInRHS;
    }

    public boolean checkContextFreeGrammar() {
        boolean startingSymbolExists = false;
        for (String key : productions.keySet()) {
            if (Objects.equals(key, startSymbol)) {
                startingSymbolExists = true;
            }
            if (!nonTerminals.contains(key)) {
                return false;
            }
        }
        if (!startingSymbolExists) {
            return false;
        }
        for (List<List<String>> production : productions.values()) {
            for (List<String> productionRightHandSide : production) {
                for (String val : productionRightHandSide) {
                    if (!nonTerminals.contains(val) && !terminals.contains(val) && !Objects.equals(val, epsilon)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void printNonTerminals() {
        System.out.println("NONTERMINALS:");
        for (String nonTerminal : nonTerminals) {
            System.out.println(nonTerminal);
        }
    }

    public void printTerminals() {
        System.out.println("TERMINALS:");
        for (String terminal: terminals) {
            System.out.println(terminal);
        }
    }

    public void printProductions() {
        System.out.println("PRODUCTIONS:");
        StringBuilder rhs = new StringBuilder();
        for (String key : productions.keySet()) {
            List<List<String>> production = productions.get(key);
            for (List<String> productionRightHandSide : production) {
                for (String val : productionRightHandSide) {
                    rhs.append(val).append(" ");
                }
                System.out.println(key + "->" + rhs);
                rhs.setLength(0);
            }
        }
    }

    public void printProductionsForNonterminal(String nonTerminal) {
        System.out.println("PRODUCTIONS FOR " + nonTerminal);
        StringBuilder rhs = new StringBuilder();
        List<List<String>> production = productions.get(nonTerminal);
        for (List<String> productionRightHandSide : production) {
            for (String val : productionRightHandSide) {
                rhs.append(val).append(" ");
            }
            System.out.println(nonTerminal + "->" + rhs);
            rhs.setLength(0);
        }
    }
}
