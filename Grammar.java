import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Grammar {
    private List<String> nonTerminals ;
    private List<String> terminals;
    private String startSymbol;
    private Map<String, List<List<String>>> productions;

    public Grammar() {
        nonTerminals = new ArrayList<>();
        terminals = new ArrayList<>();
        productions = new HashMap<>();
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


    //TODO - check CFG
    //TODO - output (toString or custom printing function for the fields )
}
