import java.util.HashMap;
import java.util.ArrayList;


// @source - Implementation of trie class is from programcreek.com.
public class Trie {

    public TrieNode label;

    private class TrieNode {
        public char chr;
        public HashMap<Character, TrieNode> kids;
        public boolean end;

        public TrieNode() {
            kids = new HashMap<>();
            end = false;
        }

        public TrieNode(char input) {
            kids = new HashMap<>();
            chr = input;
            end = false;
        }
    }

    public Trie() {
        label = new TrieNode();
    }

    public void insert(String word) {

        HashMap<Character, TrieNode> kids = label.kids;

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            TrieNode trien;
            if (kids.containsKey(c)) {
                trien = kids.get(c);
            } else {
                trien = new TrieNode(c);
                kids.put(c, trien);
            }
            kids = trien.kids;
            if (i == word.length() - 1) {
                trien.end = true;
            }
        }
    }

    public TrieNode searchNode(String word) {
        HashMap<Character, TrieNode> kids = label.kids;
        TrieNode trien = null;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (kids.containsKey(c)) {
                trien = kids.get(c);
                kids = trien.kids;
            } else {
                return null;
            }
        }
        return trien;
    }

    public ArrayList<String> words() {
        return null;

    }



    public static void main(String[] args) {
        Trie temp = new Trie();
        temp.insert("wowza");
        TrieNode boy = temp.searchNode("wow");
        System.out.println(boy);
    }
}
