import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MyScanner {
    private ArrayList<String> operators = new ArrayList<>(List.of("==", ">=", "<=", "!=", "+", "-", "*", "/", "=", ">", "<" ));
    private ArrayList<String> separators = new ArrayList<>(List.of("{", "}", "(", ")", "[", "]", ",", ";", ".",
            ":", "'", "\""));
    private ArrayList<String> reservedWords = new ArrayList<>(List.of("begin", "end", "int", "char", "string",
            "array", "if", "then", "else", "for", "while", "write", "read", "return", "len"));
    private SymbolTable<String> symbolTable;
    private PIF pif;
    private String file;

    private String line;
    private int index;
    private int currentLine;

    private FiniteAutomata faInteger;

    private FiniteAutomata faIdentifier;

    public MyScanner(String file) {
        this.symbolTable = new SymbolTable<>();
        this.pif = new PIF();
        this.file = file;
        this.index = 0;
        this.currentLine = 0;
        this.faInteger = new FiniteAutomata();
        faInteger.readFile("integerFA.txt");
        this.faIdentifier = new FiniteAutomata();
        faIdentifier.readFile("identifierFA.txt");
    }

    private void skipSpaces() {
        while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
            index++;
        }
    }

    public boolean stringConstantCase() {
        var stringConstantRegex = Pattern.compile("^\"[a-zA-Z][a-zA-Z_0-9]*\"");
        var matcher = stringConstantRegex.matcher(line.substring(index));

        if (!matcher.find()) {
            return false;
        }

        var stringConstant = matcher.group(0);
        index += stringConstant.length();
        symbolTable.add(stringConstant);
        var position = symbolTable.searchPosition(stringConstant);
        pif.addToPifList(new Pair<>("stringConstant", position));
        return true;
    }

    public boolean intConstantCase() {
        StringBuilder intConstant = new StringBuilder();
        ArrayList<String> digits = new ArrayList<>(List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "-"));
        int i=index;
        while (digits.contains(String.valueOf(line.charAt(i)))) {
            intConstant.append(line.charAt(i));
            i++;
        }

        if (faInteger.isAccepted(String.valueOf(intConstant))) {
            index = i;
            symbolTable.add(intConstant.toString());
            var position = symbolTable.searchPosition(intConstant.toString());
            pif.addToPifList(new Pair<>("integerConst", position));
            return true;
        }
        return false;
    }

    public boolean charConstantCase() {
        var charConstantRegex = Pattern.compile("^'[a-zA-Z0-9]'");
        var matcher = charConstantRegex.matcher(line.substring(index));

        if (!matcher.find()) {
            return false;
        }

        var invalidIntConstantRegex = Pattern.compile("^'([a-zA-Z0-9]{2,}|_)'");
        if (invalidIntConstantRegex.matcher(line.substring(index)).find()) {
            return false;
        }

        var charConstant = matcher.group(0);
        index += charConstant.length();
        symbolTable.add(charConstant);
        var position = symbolTable.searchPosition(charConstant);
        pif.addToPifList(new Pair<>("charConstant", position));
        return true;
    }

    public boolean identifierCase() {

        StringBuilder identifier = new StringBuilder();
        ArrayList<String> digits = new ArrayList<>(List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d",
                "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
                "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
                "y", "z", "_"));
        int i = index;
        while (digits.contains(String.valueOf(line.charAt(i)))) {
            identifier.append(line.charAt(i));
            i++;
        }
        if (faIdentifier.isAccepted(String.valueOf(identifier))) {
            // see if it is already in the symbol table
            var position = symbolTable.searchPosition(String.valueOf(identifier));
            if (position != -1) {
                index = i;
                pif.addToPifList(new Pair<>("identifier", position));
                return true;
            }
            index = i;
            symbolTable.add(String.valueOf(identifier));
            position = symbolTable.searchPosition(String.valueOf(identifier));
            pif.addToPifList(new Pair<>("identifier", position));
            return true;
        }
        return false;

    }

    public boolean tokenCase() {
        String possibleToken = line.substring(index).split(" ")[0];
        for (var op: operators) {
            if (Objects.equals(op, possibleToken)) {
                index += op.length();
                pif.addToPifList(new Pair<>(op, -1));
                return true;
            }
            else if (possibleToken.startsWith(op)) {
                index += op.length();
                pif.addToPifList(new Pair<>(op, -1));
                return true;
            }
        }

        for (var sep: separators) {
            if (Objects.equals(sep, possibleToken)) {
                index += sep.length();
                pif.addToPifList(new Pair<>(sep, -1));
                return true;
            }
            else if (possibleToken.startsWith(sep)) {
                index += sep.length();
                pif.addToPifList(new Pair<>(sep, -1));
                return true;
            }
        }

        for (var res: reservedWords) {
            if (possibleToken.startsWith(res)) {
                var invalidRegex = Pattern.compile("^[a-zA-Z0-9_]*" + res + "[a-zA-Z0-9_]+");
                if (invalidRegex.matcher(possibleToken).find()) {
                    return false;
                }
                index += res.length();
                pif.addToPifList(new Pair<>(res, -1));
                return true;
            }
        }
        return false;
    }

    public void next() throws Exception {
        skipSpaces();
        if (index == line.length()) {
            return;
        }
        if (tokenCase()) {
            return;
        }
        if (stringConstantCase()) {
            return;
        }
        if (intConstantCase()) {
            return;
        }
        if (charConstantCase()) {
            return;
        }
        if (identifierCase()) {
            return;
        }
        throw new Exception("Lexical error at line: " + currentLine);
    }

    public void scan() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            this.index = 0;
            while ((line = reader.readLine()) != null) {
                this.currentLine++;
                this.index = 0;
                line = line.strip();

                while (index < line.length()) {
                    next();
                }
            }
            FileWriter fileWriter = new FileWriter("PIF.out");
            for (int i = 0; i < pif.length(); i++) {
                fileWriter.write(pif.get(i).getFirst() + " => " + pif.get(i).getSecond() + "\n");
            }
            fileWriter.close();
            fileWriter = new FileWriter("ST.out");
            fileWriter.write(symbolTable.string());
            fileWriter.close();
            System.out.println("lexically correct");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}