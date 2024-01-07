public class SymbolTable<K> {
    private HashTable<K> hashTable;

    public SymbolTable() {
        this.hashTable = new HashTable<>();
    }

    public HashTable<K> getHashTable() {
        return hashTable;
    }

    public void add(K t) {
        this.hashTable.insertNode(t);
    }

    public int searchPosition(K t) {
        return this.hashTable.searchPosition(t);
    }

    public K searchByPosition(int pos) {
        return this.hashTable.searchByPosition(pos);
    }
    public int getSize() {
        return this.hashTable.getSize();
    }

    public String string() {
        return this.hashTable.toString();
    }
}
