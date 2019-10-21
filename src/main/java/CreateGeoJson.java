/* Copyright 2019 hbz, Pascal Christoph. Licensed under the EPL 2.0*/
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/* Parses the euregio-history csv, mstches the region_places to wikidata entities, get the geo data from them, generates a geojson file out if.
 * The output can be used like in "map.html" to visualize the data on a map.
 * 
 * @author: dr0i
 */
public class CreateGeoJson {
    static HashMap<String, ArrayList<String>> ortUriMap = new HashMap<>();
    static HashMap<String, ArrayList<String>> ortWdAndLatLonMap = new HashMap<>();

    private static String getUriTitelHref(final String ID_URI, final String TITEL) {
        return "</br><a href=\\\"" + ID_URI + "\\\">\\\"" + TITEL + "\\\"</a>";
    }

    public static void main(String... args) {
        CSVParser csvParser;
        try {
            csvParser = CSVFormat.DEFAULT
                    .parse(new InputStreamReader(new FileInputStream("content_export1567751077.csv")));
            for (CSVRecord record : csvParser) {
                String titel = record.get(3).trim();
                String ort = record.get(12).trim();
                String idUri = record.get(16).trim();
                System.out.println(ort + "," + idUri);
                String orte[];
                if ((orte = ort.split("\\b")) != null) {
                    for (String o : orte) {
                        if (o.length() > 3) {
                            // ortUriMap.put(o,ortUriMap.get(o));
                            if (ortUriMap.containsKey(o))
                                ortUriMap.get(o).add(getUriTitelHref(idUri, titel));
                            else {
                                ArrayList<String> list = new ArrayList<String>();
                                list.add(getUriTitelHref(idUri, titel));
                                ortUriMap.put(o, list);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String ort : ortUriMap.keySet()) {
            String wdId;
            if ((wdId = lookupWikidataGetId(ort)) != null) {
                lookupWikidataParseGeoData(ort, wdId);
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("euregioHistory.geojson"), StandardCharsets.UTF_8));
            writer.write(geoJsonHead);
            boolean first = true;
            for (String ort : ortWdAndLatLonMap.keySet()) {
                if (!first)
                    writer.write(",\n");
                getJsonEntry(ort, writer);
                first = false;
            }
            writer.write("]}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // @formatter:off
    static String geoJsonHead = 
          "{\n" +
          "  \"type\": \"FeatureCollection\",\n" +
          "  \"features\": [\n";
    static String geoJsonEntry = //
          "{\n" +
          "      \"type\": \"Feature\",\n" +
          "      \"geometry\": {\n" +
          "        \"type\": \"Point\",\n" +
          "        \"coordinates\": [%s,%s]\n" +
          "      },\n" +
          "      \"properties\": {\n" +
          "        \"ort\": \"%s\",\n" +
          "        \"wd\": \"%s\",\n" +
          "        \"target\": \"%s\"\n" +
          "}}";

    // @formatter:on
    private static void getJsonEntry(final String ORT, final BufferedWriter WRITER) throws IOException {
        System.out.println(ORT + "," + ortWdAndLatLonMap.get(ORT).toString());
        WRITER.write(String.format(geoJsonEntry, ortWdAndLatLonMap.get(ORT).get(1), ortWdAndLatLonMap.get(ORT).get(2),
                ORT, ortWdAndLatLonMap.get(ORT).get(0),
                ortUriMap.get(ORT).toString().substring(1, ortUriMap.get(ORT).toString().length() - 1)));
    }

    /**
     * Looks up a wikidata entity based on names of the locality
     * 
     * @param ORT
     *            the name of the locality
     */
    static String wdLookup = "https://www.wikidata.org/w/api.php?action=wbgetentities&sites=dewiki&titles=%s&props=descriptions&languages=de&format=json";

    public static String lookupWikidataGetId(final String ORT) {
        String newWdUrl = String.format(wdLookup, ORT);
        System.out.println("lookup:" + newWdUrl);
        try (AsyncHttpClient client = new AsyncHttpClient()) {
            JsonNode jnode = toApiResponseGet(client, newWdUrl);
            JsonNode wdId = jnode.findValue("id");
            return wdId.asText();
        } catch (Exception e) {
            System.out.println("Can't get wikidata entity for " + ORT);
        }
        return null;
    }

    private static final String HTTP_WWW_WIKIDATA_ORG_ENTITY = "http://www.wikidata.org/entity/";

    public static void lookupWikidataParseGeoData(final String ORT, final String WDID) {
        String newWdUrl = HTTP_WWW_WIKIDATA_ORG_ENTITY + WDID;
        System.out.println("lookup:" + newWdUrl);
        try (AsyncHttpClient client = new AsyncHttpClient()) {
            JsonNode jnode = toApiResponseGet(client, newWdUrl);
            String lat = jnode.findValue("latitude").asText();
            String lon = jnode.findValue("longitude").asText();
            ArrayList<String> list = new ArrayList<String>();
            list.add(0, WDID);
            list.add(1, lon);
            list.add(2, lat);
            ortWdAndLatLonMap.put(ORT, list);
        } catch (Exception e) {
            System.out.println("Can't get wikidata geo data for " + WDID);
        }
    }

    private static final String JSON_ACCEPT_HEADER = "application/json";

    private static JsonNode toApiResponseGet(final AsyncHttpClient CLIENT, final String API)
            throws InterruptedException, ExecutionException, JsonParseException, JsonMappingException, IOException {
        Thread.sleep(200); // be nice throttle down
        Response response = CLIENT.prepareGet(API).setHeader("Accept", JSON_ACCEPT_HEADER).setFollowRedirects(true)
                .execute().get();
        return new ObjectMapper().readValue(response.getResponseBodyAsStream(), JsonNode.class);
    }

}
