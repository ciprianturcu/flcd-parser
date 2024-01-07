import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class PIF {
    private ArrayList<Pair<String, Integer>> pifList;

    public PIF() {
        this.pifList = new ArrayList<>();
    }

    public void addToPifList(Pair<String, Integer> p) {
        this.pifList.add(p);
    }

    public int length() {
        return this.pifList.size();
    }

    public Pair<String, Integer> get(int position) {
        return this.pifList.get(position);
    }

    public static List<String> readFromPifOutput(String filename) {
        try{
            List<String> tokens = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while(line!=null){
                tokens.add(line.split("=>")[0].strip());
                line = reader.readLine();
            }
            reader.close();
            return tokens;
        }
        catch (Exception exception) {
            return new ArrayList<>();
        }
    }
}
