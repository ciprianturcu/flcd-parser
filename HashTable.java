import java.util.ArrayList;

public class HashTable<K> {

    int capacity;
    int size;
    HashNode<K>[] table;

    public HashTable() {
        this.capacity = 100;
        this.size = 0;
        this.table = new HashNode[this.capacity];
    }

    private int hashFunction(K key){
        int sumOfCharacters = 0;
        String keyString = key.toString();
        for(char c: keyString.toCharArray()){
            sumOfCharacters += c;
        }
        return sumOfCharacters % this.capacity;
    }

    public void insertNode(K key) {
        HashNode<K> temp = new HashNode<>(key);
        int hashIndex = hashFunction(key);
        while (this.table[hashIndex] != null) {
            if (this.table[hashIndex].key.equals(key)) {
                return;
            }
            hashIndex++;
            hashIndex %= this.capacity;
        }
        this.size++;
        this.table[hashIndex] = temp;
    }

    public int searchPosition(K key) {
        int hashIndex = hashFunction(key);
        int counter = 0;
        while (this.table[hashIndex] != null) {
            if (counter > this.capacity) {
                return -1;
            }
            if (this.table[hashIndex].key.equals(key)) {
                return hashIndex;
            }
            hashIndex++;
            hashIndex %= this.capacity;
            counter++;
        }
        return -1;
    }

    public K searchByPosition(int position) {
        return this.table[position].key;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < capacity; i++) {
            if (table[i] != null) {
                str.append(table[i].key.toString()).append(" => ").append(i).append("\n");
            }
        }
        return str.toString();
    }
}