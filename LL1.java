import java.util.*;

public class LL1 {
    private Grammar grammar;
    private Map<String, Set<String>> firstSet;
    private Map<String, Set<String>> followSet;
    private Map<Pair, Pair> parsingTable;
    private List<List<String>> productionsRHS;

    public LL1(Grammar grammar) {
        this.grammar = grammar;
        this.firstSet = new HashMap<>();
        this.followSet = new HashMap<>();
        this.parsingTable = new HashMap<>();

        initializeFollowSet();
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
            concatenation.add(Objects.requireNonNullElse(terminal, "epsilon"));
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
        for (String nonTerminal : grammar.getNonTerminals()) {
            firstSet.put(nonTerminal, new HashSet<>());
            List<List<String>> productionsForNonTerminal = grammar.getProductionForNonterminal(nonTerminal);
            for (List<String> production : productionsForNonTerminal) {
                // if the production starts with a terminal or epsilon, we add it to the first set
                if (grammar.getTerminals().contains(production.get(0)) || production.get(0).equals("epsilon"))
                    firstSet.get(nonTerminal).add(production.get(0));
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
                        } else {
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


    public void FOLLOW() {
        boolean isChanged = true;
        while (isChanged) {
            isChanged = false;
            HashMap<String, Set<String>> newColumn = new HashMap<>();

            for (String nonTerminal : grammar.getNonTerminals()) {
                newColumn.put(nonTerminal, new HashSet<>());
                Set<String> followSetToBeAdded = new HashSet<>(followSet.get(nonTerminal));
                grammar.getProductionsWithNonTerminalInRHS(nonTerminal).forEach((leftSideOfProduction, rightSideOfProduction) -> {
                    for (List<String> production : rightSideOfProduction) {
                        for (int index = 0; index < production.size(); index++) {
                            if (production.get(index).equals(nonTerminal)) {
                                if (index + 1 == production.size()) {
                                    followSetToBeAdded.addAll(followSet.get(leftSideOfProduction));
                                } else {
                                    String symbolToFollow = production.get(index + 1);
                                    if (grammar.getTerminals().contains(symbolToFollow)) {
                                        followSetToBeAdded.add(symbolToFollow);
                                    } else {
                                        for (String symbol : firstSet.get(symbolToFollow)) {
                                            if (symbol.equals("epsilon")) {
                                                followSetToBeAdded.addAll(followSet.get(leftSideOfProduction));
                                            } else {
                                                followSetToBeAdded.addAll(firstSet.get(symbolToFollow));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
                if (!followSetToBeAdded.equals(followSet.get(nonTerminal))) {
                    isChanged=true;
                }
                newColumn.put(nonTerminal, followSetToBeAdded);
            }
            followSet = newColumn;
        }
    }

    public void ParsingTable() {
        List<String> nonTerminals = grammar.getNonTerminals();
        List<String> terminals = grammar.getTerminals();

        List<String> rows = new ArrayList<>();
        rows.addAll(nonTerminals);
        rows.addAll(terminals);
        rows.add("$");

        List<String> cols = new ArrayList<>();
        cols.addAll(terminals);
        cols.add("$");

        // fill in the table with (none, -1)
        for (var row : rows)
            for (var col : cols)
                parsingTable.put(new Pair<>(row, col), new Pair<>("none", -1));

        // now complete the pop for the terminals
        for (var col : cols)
            parsingTable.put(new Pair<>(col, col), new Pair<>("pop", -1));

        // now put the acceptance
        parsingTable.put(new Pair<>("$", "$"), new Pair<>("acc", -1));

        var productions = grammar.getProductions();
        this.productionsRHS = new ArrayList<>();

        for (String key: productions.keySet()) {
            for (List<String> prod: productions.get(key)) {
                if (prod.get(0).equals("epsilon"))
                    productionsRHS.add(new ArrayList<>(List.of("epsilon", key)));
                else
                    productionsRHS.add(new ArrayList<>(prod));
            }
        }

        for (String key: productions.keySet()) {
            for (List<String> prod: productions.get(key)) {
                String firstSymbol = prod.get(0);

                // here we treat the case where the first symbol in the rhs of a production is a terminal,
                // so we verify if it can be added to the parsing table (and add it) or if we have a
                // conflict
                if (terminals.contains(firstSymbol)) {
                    if (parsingTable.get(new Pair<>(key, firstSymbol)).getFirst().equals("none"))
                        parsingTable.put(new Pair<>(key, firstSymbol), new Pair<>(String.join(" ", prod), productionsRHS.indexOf(prod) + 1));
                    else {
                        try {
                            throw new Exception("CONFLICT: " + key + ", " + firstSymbol);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                // here we treat the case where the first symbol in the rhs of a production is a nonTerminal,
                else if (nonTerminals.contains(firstSymbol)) {

                    // if the production rhs has only one nonTerminal, we get FIRST of that nonTerminal and then,
                    // for each element of FIRST we verify if it can be added to the parsing table (and add it)
                    // or if we have a conflict
                    if (prod.size() == 1) {
                        for (var symbol: firstSet.get(firstSymbol)) {
                            if (parsingTable.get(new Pair<>(key, symbol)).getFirst().equals("none"))
                                parsingTable.put(new Pair<>(key, symbol), new Pair<>(String.join(" ", prod), productionsRHS.indexOf(prod) + 1));
                            else {
                                try {
                                    throw new Exception("CONFLICT: " + key + ", " + symbol);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // if not, we take the next symbol in the current production
                    else {
                        var nextSymbol = prod.get(1);
                        var firstOfCurrentProduction = firstSet.get(firstSymbol);
                        var i = 1;

                        // while we have not parsed all the symbols of the current production
                        // and the next symbol is a nonTerminal, we check if the current FIRST of the
                        // production contains epsilon, and if so, we remove it and add the first of the next symbol
                        while (i < prod.size() && nonTerminals.contains(nextSymbol)) {
                            var firstOfNextSymbol = firstSet.get(nextSymbol);

                            if (firstOfCurrentProduction.contains("epsilon")) {
                                firstOfCurrentProduction.remove("epsilon");
                                firstOfCurrentProduction.addAll(firstOfNextSymbol);
                            }

                            i++;
                            if (i < prod.size())
                                nextSymbol = prod.get(i);
                        }

                        // now we take all the symbols in the FIRST of the current production, and if in the corresponding
                        // position in the parsing table we add it, otherwise we have a conflict
                        for (var symbol : firstOfCurrentProduction) {
                            if (symbol.equals("epsilon"))
                                symbol = "$";
                            if (parsingTable.get(new Pair<>(key, symbol)).getFirst().equals("none"))
                                parsingTable.put(new Pair<>(key, symbol), new Pair<>(String.join(" ", prod), productionsRHS.indexOf(prod) + 1));
                            else {
                                try {
                                    throw new Exception("CONFLICT: " + key + ", " + symbol);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                // otherwise, if all the first symbols in the production is a terminal, we take the follow and if
                // in the corresponding position in the parsing table we add it, otherwise we have a conflict
                else {
                    var follow = followSet.get(key);
                    for (var symbol : follow) {
                        if (symbol.equals("epsilon")) {
                            symbol = "$";
                            if (parsingTable.get(new Pair<>(key, symbol)).getFirst().equals("none")) {
                                var p = new ArrayList<>(List.of("epsilon", key));
                                parsingTable.put(new Pair<>(key, symbol), new Pair<>("epsilon", productionsRHS.indexOf(p) + 1));
                            }
                            else {
                                try {
                                    throw new Exception("CONFLICT: " + key + ", " + symbol);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (parsingTable.get(new Pair<>(key, symbol)).getFirst().equals("none")) {
                            var p = new ArrayList<>(List.of("epsilon", key));
                            parsingTable.put(new Pair<>(key, symbol), new Pair<>("epsilon", productionsRHS.indexOf(p) + 1));
                        }
                        else {
                            try {
                                throw new Exception("CONFLICT: " + key + ", " + symbol);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void initializeFollowSet() {
        for (String nonTerminal : grammar.getNonTerminals()) {
            followSet.put(nonTerminal, new HashSet<>());
        }
        followSet.get(grammar.getStartSymbol()).add("epsilon");
    }

    public void printFollowSet() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Follow Set:\n");

        for (Map.Entry<String, Set<String>> entry : followSet.entrySet()) {
            stringBuilder.append("Key: ").append(entry.getKey()).append(", Values: ").append(entry.getValue()).append("\n");
        }

        System.out.println(stringBuilder);
    }

    public void printFirstSet() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("First Set:\n");

        for (Map.Entry<String, Set<String>> entry : firstSet.entrySet()) {
            stringBuilder.append("Key: ").append(entry.getKey()).append(", Values: ").append(entry.getValue()).append("\n");
        }

        System.out.println(stringBuilder);
    }

    public void printParsingTable() {
        List<String> nonTerminals = grammar.getNonTerminals();
        List<String> terminals = grammar.getTerminals();

        List<String> rows = new ArrayList<>();
        rows.addAll(nonTerminals);
        rows.addAll(terminals);
        rows.add("$");

        List<String> cols = new ArrayList<>();
        cols.addAll(terminals);
        cols.add("$");

        int rowHeaderWidth = 3;
        int colHeaderWidth = 10;

        // Print column headers
        System.out.printf("%-" + rowHeaderWidth + "s |", ""); // Empty space for row header column
        for (String col : cols) {
            System.out.printf(" %-" + colHeaderWidth + "s |", col);
        }
        System.out.println();

        // Print separator
        int totalWidth = rowHeaderWidth + 3 + (colHeaderWidth + 3) * cols.size(); // 3 accounts for spaces and vertical bars
        for (int i = 0; i < totalWidth; i++) {
            System.out.print("-");
        }
        System.out.println();

        // Print rows with their respective data
        for (String row : rows) {
            System.out.printf("%-" + rowHeaderWidth + "s |", row);
            for (String col : cols) {
                Pair<String, Integer> cell = parsingTable.get(new Pair<>(row, col));
                System.out.printf(" %-" + colHeaderWidth + "s |", removeSpacesFromString(cell.getFirst()) + ", " + cell.getSecond());
            }
            System.out.println();
        }
    }

    public List<Integer> parseSequence(List<String> sequence) {
        Stack<String> alpha = new Stack<>();
        Stack<String> beta = new Stack<>();
        List<Integer> result = new ArrayList<>();

        // initialize the alpha stack with $ and the sequence
        alpha.push("$");
        for (var i = sequence.size() - 1; i >= 0; i--)
            alpha.push(sequence.get(i));

        // initialize the beta stack with $ and the starting symbol
        beta.push("$");
        beta.push(grammar.getStartSymbol());

        // while we do not have reached accept
        while (! (alpha.peek().equals("$") && beta.peek().equals("$"))) {
            // we get the top of the stacks and the corresponding element from the table
            String alphaTopElement = alpha.peek();
            String betaTopElement = beta.peek();
            Pair<String, String> key = new Pair<>(betaTopElement, alphaTopElement);
            Pair<String, Integer> value = parsingTable.get(key);

            // if in the parsing table we have none it means that the sequence is not accepted by the grammar
            if (value.getFirst().equals("none")) {
                System.out.println("Syntax error for: " + key);
                result = new ArrayList<>(List.of(-1));
                return result;
            }

            // if we have pop, we have to pop the top of the stacks
            if (value.getFirst().equals("pop")) {
                alpha.pop();
                beta.pop();
            }
            // otherwise we pop the top of beta and if the value from the table is not epsilon, we add the values to
            // beta and the corresponding number of the production to the productions string
            else {
                beta.pop();
                if (!value.getFirst().equals("epsilon")) {
                    String[] symbols = value.getFirst().split(" ");
                    for (var i = symbols.length - 1; i >= 0; i--) {
                        beta.push(symbols[i]);
                    }
                }
                result.add(value.getSecond());
            }
        }
        return result;
    }

    private String removeSpacesFromString(String s) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ') {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}
