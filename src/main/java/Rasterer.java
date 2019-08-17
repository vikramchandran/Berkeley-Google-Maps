import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private MapServer mapserver;

    public Rasterer() {
        mapserver = new MapServer();
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        boolean querysuccess = true;
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double w = params.get("w");
        double h = params.get("h");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
       /* System.out.println("lrlon is: " + lrlon + "; ullon is: " + ullon);
        System.out.println("width is: " + w + "; height is " + h + "; ullat is: " + ullat
                + "; lrlat is: " + lrlat);*/
        if (checkquery(ullat, lrlat, lrlon, ullon)) {
            querysuccess = false;
        }
        double exactdpp = londpp(lrlon, ullon, w);
        int depth = finddepth(exactdpp);
        double widthofeach = (mapserver.ROOT_LRLON - mapserver.ROOT_ULLON) / (Math.pow(2, depth));
        double numrightspaces = Math.floor(Math.abs((mapserver.ROOT_ULLON - ullon)
                / (widthofeach)));
        double heightofeach = (mapserver.ROOT_ULLAT - mapserver.ROOT_LRLAT)
                / (Math.pow(2, depth));
        double numdownspaces = Math.floor(Math.abs((mapserver.ROOT_ULLAT - ullat)
                / (heightofeach)));
        int tempnumcols = (int) (Math.abs((lrlon - ullon)) / (widthofeach));
        //int tempnumcols = 0; //int tempnumrows = 0;
        double tempullon = mapserver.ROOT_ULLON + numrightspaces * widthofeach;
        double tempullat = mapserver.ROOT_ULLAT -  numdownspaces * heightofeach;
        while ((tempullon + (widthofeach * tempnumcols)) < lrlon) {
            tempnumcols += 1;
        }
        int tempnumrows = (int) (Math.abs((ullat - lrlat)) / heightofeach);

        while (tempullat  - heightofeach * tempnumrows > lrlat) {
            tempnumrows += 1;
        }
        String topleft = "d" + depth + "_x" + (int) numrightspaces + "_y"
                + (int) numdownspaces + ".png";
        ArrayList<Integer> xvalues = new ArrayList<>();
        ArrayList<Integer> yvalues = new ArrayList<>();
        int temp = 0;
        for (int i = (int) numrightspaces; i < (int) numrightspaces + tempnumcols; i++) {
            if (i  > (Math.pow(2, depth) - 1)) {
                break;
            }
            xvalues.add(i);
            temp++;
        }
        int temp2 = 0;
        for (int i = (int) numdownspaces; i < (int) numdownspaces + tempnumrows; i++) {
            if (i > (Math.pow(2, depth) - 1)) {
                break;
            }
            yvalues.add(i);
            temp2++;
        }
        String[][] lst = new String[yvalues.size()][xvalues.size()];
        results.put("query_success", querysuccess);
        results.put("depth", depth);
        results.put("raster_lr_lon", tempullon + widthofeach * tempnumcols);
        results.put("raster_lr_lat", tempullat - heightofeach * tempnumrows);
        results.put("raster_ul_lat", mapserver.ROOT_ULLAT -  numdownspaces * heightofeach);
        results.put("raster_ul_lon", mapserver.ROOT_ULLON + numrightspaces * widthofeach);
        results.put("render_grid", lst);

       /* printstatements1(results, widthofeach, numrightspaces, heightofeach,
                numdownspaces,  depth);
        printstatements2(depth, exactdpp,  tempnumrows,
                tempnumcols,  topleft, querysuccess, xvalues, yvalues);*/

        for (int i = 0; i < xvalues.size(); i++) {
            for (int j = 0; j < yvalues.size(); j++) {
                int correctx = xvalues.get(i);
                int correcty = yvalues.get(j);
                lst[j][i] = "d" + depth + "_x" + correctx + "_y" + correcty + ".png";
            }
        }
        //printString(lst);
        return results;
    }

    public double londpp(double lrl, double upl, double width) {
        return (lrl - upl) / width;
    }

    public boolean checkquery(double ullat, double lrlat, double lrlon, double ullon) {
        boolean ans = false;
        if (ullat <= lrlat || lrlon <= ullon) {
            ans = true;
        }
        if (ullat > mapserver.ROOT_ULLAT || lrlat < mapserver.ROOT_LRLAT
                || ullon < mapserver.ROOT_ULLON || lrlon > mapserver.ROOT_LRLON) {
            ans = true;
        }
        return ans;
    }

    public int finddepth(double num) {
        int d = 0;
        double comp = (mapserver.ROOT_LRLON - mapserver.ROOT_ULLON) / (256 * (Math.pow(2, d)));
        if (comp > num) {
            while (comp > num) {
                d += 1;
                comp = (mapserver.ROOT_LRLON - mapserver.ROOT_ULLON) / (256 * (Math.pow(2, d)));
                if (d >= 7) {
                    break;
                }
            }
        }
        return d;
    }

    public void printstatements1(Map<String, Object> results, double widthofeach,
                                double numrightspaces, double heightofeach, double numdownspaces,
                                int depth) {
        System.out.println("raster_lr_lon is: " + results.get("raster_lr_lon"));
        System.out.println("raster_lr_lat is: " + results.get("raster_lr_lat"));
        System.out.println("raster_ul_lat is: " + results.get("raster_ul_lat"));
        System.out.println("raster_ul_lon is: " + results.get("raster_ul_lon"));

        System.out.println("Widthofeach is: " + widthofeach);
        System.out.println("Num right spaces: " + numrightspaces);
        //System.out.println("Num left spaces: " + num_left_spaces);
        System.out.println("Height_of_each is: " + heightofeach);
        System.out.println("Num down spaces: " + numdownspaces);
        //System.out.println("Num up spaces: " + num_up_spaces);
        System.out.println("Depth necessary is: " + depth);
    }
    public void printstatements2(int depth, double exactdpp, int tempnumrows, int tempnumcols,
                     String topleft, boolean querysuccess,
                     ArrayList<Integer> xvalues, ArrayList<Integer> yvalues) {
        System.out.println("Exact DPP is: " + exactdpp);
        System.out.println("Estimate dpp is: " + (mapserver.ROOT_LRLON - mapserver.ROOT_ULLON)
                / (256 * Math.pow(2, depth)));
        System.out.println("Number of rows are: " + tempnumrows);
        System.out.println("Number of cols are: " + tempnumcols);
        System.out.println("Top left is: " + topleft);
        System.out.println("Query success is " + querysuccess);
        System.out.println("Here are the x values: ");
        for (int i : xvalues) {
            System.out.println(i);
        }
        System.out.println("Here are the y values: ");
        for (int i : yvalues) {
            System.out.println(i);
        }

    }

    public void printString(String[][] string) {
        System.out.println("Values of String are: ");
        for (String[] i: string) {
            for (String j: i) {
                System.out.println(j + " ");
            }
        }
    }
}
