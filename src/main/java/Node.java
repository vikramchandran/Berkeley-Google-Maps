import java.util.ArrayList;

public class Node implements Comparable<Node> {
    private Long idnum;
    private double lat;
    private double lon;
    private ArrayList<Long> edges;
    private int nummoves;
    private Long prev;

    public Node(Long idinput, double latinput, double longinput) {
        idnum = idinput;
        lat = latinput;
        lon = longinput;
        edges = new ArrayList<>();
        nummoves = 0;
        prev = null;
    }

    public Node(Long idinput, double latinput, double longinput, int nmvs, Long prv) {
        idnum = idinput;
        lat = latinput;
        lon = longinput;
        edges = new ArrayList<>();
        nummoves = nmvs;
        prev = prv;
    }



    public Long getid() {
        return idnum;
    }

    public double getlat() {
        return lat;
    }
    public double getlon() {
        return lon;
    }

    public int getnummoves() {
        return nummoves;
    }

    public ArrayList<Long> getedges() {
        return edges;
    }



    public int priority() {
        return 0;
    }

    public int compareTo(Node node) {
        return 0;
    }


}
