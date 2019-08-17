public class SearchNode {
    private Long idnum;
    private double distsource;
    private SearchNode prev;
    private double priority;

    public SearchNode(Long nm, double disttosource, SearchNode prv, double stov,
                      double vtoe, double edgetogoal) {
        idnum = nm;
        distsource = disttosource;
        prev = prv;
        priority = (stov + vtoe + edgetogoal);
    }


    /*public int compareTo(SearchNode n) {
        return Double.compare(priority, n.priority);
    }*/
}
