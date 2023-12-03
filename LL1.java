import java.util.*;
public class LL1 {
    private Grammar grammar;
    private Map<String, Set<String>> firstSet;
    private Map<String, Set<String>> followSet;

    public LL1(Grammar grammar) {
        this.grammar = grammar;
        this.firstSet = new HashMap<>();
        this.followSet = new HashMap<>();
    }

    public Set<String> concatenationOfLength1(List<String> nonTerminals, String terminal) {
        // in this case we already added all terminals
        if (nonTerminals.size() == 0) {
            return new HashSet<>();
        }
        // in this case we need to add the first element (which is the only one element) from the nonterminals list
        if (nonTerminals.size() == 1) {
            return firstSet.get(nonTerminals.get(0));
        }

        Set<String> concatenation = new HashSet<>();
        var index = 0;
        var allNonTerminalsContainEpsilon = true;

        for (String nonTerminal : nonTerminals) {
            if (!firstSet.get(nonTerminal).contains("epsilon")) {
                allNonTerminalsContainEpsilon = false;
            }
        }

        // if the nonterminals contain epsilon, we must add the terminal if it is not null, otherwise epsilon
        if (allNonTerminalsContainEpsilon) {
            if (!terminal.equals("")) concatenation.add(terminal);
            else concatenation.add("epsilon");
        }

        // we have a while that continues until there are epsilons in the first set of the current nonterminal
        // and increases the index in order to move to the next nonterminal from the list
        // if epsilon is in the list, we increment the index, otherwise we break the loop
        while (index < nonTerminals.size()) {
            boolean nonTerminalContainsEpsilon = false;
            for (String s : firstSet.get(nonTerminals.get(index)))
                if (s.equals("epsilon")) nonTerminalContainsEpsilon = true;
                else concatenation.add(s);
            if (nonTerminalContainsEpsilon) index++;
            else break;
        }
        return concatenation;
    }

    public void FIRST() {
        for (String nonterminal : grammar.getNonTerminals()) {
            firstSet.put(nonterminal, new HashSet<>());
            List<List<String>> productionsForNonterminal = grammar.getProductionForNonterminal(nonterminal);
            for (List<String> production : productionsForNonterminal) {
                // if the production starts with a terminal or epsilon, we add it to the first set
                if (grammar.getTerminals().contains(production.get(0)) || production.get(0).equals("epsilon"))
                    firstSet.get(nonterminal).add(production.get(0));
            }
        }

        var isFirstChanged = true;

        while (isFirstChanged) {
            isFirstChanged = false;
            Map<String, Set<String>> currColumn = new HashMap<>();

            for (String nonTerminal : grammar.getNonTerminals()) {
                List<List<String>> productionsForNonTerminal = grammar.getProductionForNonterminal(nonTerminal);
                // in currentFirst we have the current terminals for each nonTerminal
                // now it contains what it is now in the first set for the current nonTerminal
                Set<String> currentFirst = new HashSet<>(firstSet.get(nonTerminal));
                for (List<String> production : productionsForNonTerminal) {
                    // this list will contain all nonTerminals from the rhs until it finds a terminal
                    List<String> rhsNonTerminals = new ArrayList<>();
                    // this will be the first terminal from the rhs
                    String rhsTerminal = null;
                    for (String element : production) {
                        if (grammar.getNonTerminals().contains(element)) {
                            rhsNonTerminals.add(element);
                        }
                        else {
                            rhsTerminal = element;
                            break;
                        }
                    }
                    currentFirst.addAll(concatenationOfLength1(rhsNonTerminals, rhsTerminal));
                }
                if (!currentFirst.equals(firstSet.get(nonTerminal))) {
                    isFirstChanged = true;
                }
                currColumn.put(nonTerminal, currentFirst);
            }
            // now the first set will be the last column
            firstSet = currColumn;
        }
    }

    public void printFirstSet() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("First Set:\n");

        for (Map.Entry<String, Set<String>> entry : firstSet.entrySet()) {
            stringBuilder.append("Key: ").append(entry.getKey()).append(", Values: ").append(entry.getValue()).append("\n");
        }

        System.out.println(stringBuilder);
    }
}
