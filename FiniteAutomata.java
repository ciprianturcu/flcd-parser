import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class FiniteAutomata {
    private ArrayList<String> states;
    private ArrayList<String> alphabet;
    private Map<Pair<String, String>, ArrayList<String>> transitions;
    private String initialState;
    private ArrayList<String> finalStates;

    public FiniteAutomata() {
        this.states = new ArrayList<>();
        this.alphabet = new ArrayList<>();
        this.transitions = new HashMap<>();
        this.finalStates = new ArrayList<>();
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public ArrayList<String> getAlphabet() {
        return alphabet;
    }

    public Map<Pair<String, String>, ArrayList<String>> getTransitions() {
        return transitions;
    }

    public String getInitialState() {
        return initialState;
    }

    public ArrayList<String> getFinalStates() {
        return finalStates;
    }

    public void readFile(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("states")) {
                    String[] statesList = reader.readLine().split(" ");
                    Collections.addAll(states, statesList);
                } else if (line.equals("initial_state")) {
                    initialState = reader.readLine();
                } else if (line.equals("final_states")) {
                    String[] fs = reader.readLine().split(" ");
                    Collections.addAll(finalStates, fs);
                } else if (line.equals("alphabet")) {
                    String[] alph = reader.readLine().split(" ");
                    Collections.addAll(alphabet, alph);
                } else if (line.equals("transitions")) {
                    String line1;
                    while ((line1 = reader.readLine()) != null) {
                        String[] t = line1.split(" ");
                        if (states.contains(t[0]) && alphabet.contains(t[1]) && states.contains(t[2])) {
                            Pair<String, String> keyPair = new Pair<>(t[0], t[1]);
                            if (transitions.containsKey(keyPair)) {
                                transitions.get(keyPair).add(t[2]);
                            } else {
                                ArrayList<String> values = new ArrayList<>();
                                values.add(t[2]);
                                transitions.put(keyPair, values);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isDeterministic() {
        for (Pair<String, String> key: this.transitions.keySet()) {
            if (this.transitions.get(key).size() > 1) {
                return false;
            }
        }
        return true;
    }

    public boolean isAccepted(String sequence) {
        if (!isDeterministic()) {
            return false;
        }

        String currentState = this.initialState;

        while (sequence.length() > 0) {
            Pair<String, String> key = new Pair<>(currentState, sequence.substring(0,1));
            if (!transitions.containsKey(key)) {
                return false;
            }

            currentState = this.transitions.get(key).get(0);
            sequence = sequence.substring(1);
        }

        return this.finalStates.contains(currentState);
    }
}