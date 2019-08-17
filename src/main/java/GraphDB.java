import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//import java.util.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */



    private HashMap<Long, Node> map = new HashMap<>();
    public HashMap<Node, String> names = new HashMap<>();




    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    public List<String> getLocationsByPrefix (String input) {
        String cleaned = cleanString(input);
        Trie trie = new Trie();
        for(String loc: getLocations()) {
            trie.insert(loc);
        }
        //trie.searchNode(input).kids;

        return null;

    }

    public ArrayList<String> getLocations() {
        ArrayList location = new ArrayList<>();
        for(Node i: names.keySet()) {
            location.add(names.get(i));
        }
        return location;
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> lst = new ArrayList<>();
        for (Long k: map.keySet()) {
            lst.add(k);
        }
        for (long i : lst) {
            if (map.get(i).getedges().size() == 0) {
                map.remove(i);
            }
        }
    }

    public HashMap<Long, Node> getMap() {
        return map;
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return map.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        return map.get(v).getedges();
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        long closest = 0;
        for (Long i: vertices()) {
            if (closest == 0) {
                closest = i;
            }
            double loni = map.get(i).getlon();
            double lati = map.get(i).getlat();
            double closlon = map.get(closest).getlon();
            double closlat = map.get(closest).getlat();
            if (distance(loni, lati, lon, lat) < distance(closlon, closlat, lon, lat)) {
                closest = i;
            }
        }
        return closest;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return map.get(v).getlon();
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return map.get(v).getlat();
    }

    public void addNode(Node node) {
        map.put(node.getid(), node);
    }

    public void addEdge(Node n1, Node n2) {
        n1.getedges().add(n2.getid());
        n2.getedges().add(n1.getid());
    }

    public void removeNode(Node node) {
        map.remove(node.getid());
    }

    public Node getNode(Long l) {
        return map.get(l);
    }



    /*public List<Long> path(double stlon, double stlat,
                           double destlon, double destlat) {

        HashMap<Long, Double> best = new HashMap<>();
        PriorityQueue<SearchNode> heap = new PriorityQueue<>();
        Long source = closest(stlon, stlat);
        Long goal = closest(destlon, destlat);
        best.put(source, 0.0);
        heap.add(new SearchNode(source, 0.0, null, 0, 0, distance(source, goal)));
        SearchNode vertex = heap.peek();

        while (vertex.idnum != goal && !heap.isEmpty()) {
            vertex = heap.remove();
            //System.out.println(vertex.priority);
            for (long e: adjacent(vertex.idnum)) {
                double s_to_v = best.get(vertex.idnum);
                double v_to_edge = distance(vertex.idnum, e);
                double edge_to_goal = distance(e, goal);
                if (!best.containsKey(e) || best.get(e) > (s_to_v + v_to_edge)) {
                    best.put(e, s_to_v + v_to_edge);
                    heap.add(new SearchNode(e, s_to_v + v_to_edge, vertex,
                    s_to_v, v_to_edge, edge_to_goal));
                }
            }
            //System.out.println(goal);
        }

        ArrayList<Long> lst = new ArrayList<>();
        while(vertex != null) {
            lst.add(vertex.idnum);
            vertex = vertex.prev;
        }
        Collections.reverse(lst);
        return lst;

    }*/
}
