package main.java;

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

//import com.fasterxml.jackson.databind.JsonNode;
//import com.ning.http.client.AsyncHttpClient;

public class CreateGeoJson {
	static HashMap<String, ArrayList<String>> ortUriMap = new HashMap<>();
	static HashMap<String, ArrayList<String>> ortWdAndLatLonMap = new HashMap<>();

	public static void main(String... args) {
		CSVParser csvParser;
		try {
			csvParser = CSVFormat.DEFAULT
					.parse(new InputStreamReader(new FileInputStream("content_export1567751077.csv")));
			for (CSVRecord record : csvParser) {
				String ort = record.get(12);
				String idUri = record.get(16);
				System.out.println(ort + "," + idUri);
				String orte[];
				if ((orte = ort.split("\\b")) != null) {
					for (String o : orte) {
						if (o.length() > 3) {
							// ortUriMap.put(o,ortUriMap.get(o));
							if (ortUriMap.containsKey(o))
								ortUriMap.get(o).add(idUri);
							else {
								ArrayList<String> list = new ArrayList<String>();
								list.add(idUri);
								ortUriMap.put(o, list);
							}
							System.out.println(o + " =" + ortUriMap.get(o).toString());
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String ort : ortUriMap.keySet()) {
			String wdId;
			if ((wdId = lookupWikidataGetId(ort)) != null) {
				lookupWikidataParseGeoData(ort, wdId);
			}

		}
		// @formatter:off
		String geoJsonHead=
				"{\n" + 
				"  \"type\": \"FeatureCollection\",\n" + 
				"  \"features\": [\n";
		String geoJsonEntry= //
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
				"}},"
				;
		// @formatter:on
		
	try {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("out.geojson"), StandardCharsets.UTF_8));
			writer.write(geoJsonHead);
		for (String ort : ortWdAndLatLonMap.keySet()) {
			System.out.println(ort+","+ortWdAndLatLonMap.get(ort).toString());
				writer.write(String.format(geoJsonEntry, ortWdAndLatLonMap.get(ort).get(1),
						ortWdAndLatLonMap.get(ort).get(2), ort, ortWdAndLatLonMap.get(ort).get(0),
						ortUriMap.get(ort).toString()));
		}
		writer.write("]}");
			writer.close();
		} catch (IOException e) {
		e.printStackTrace();
	}
	}

	/**
	 * Lookups a wikidata entity and, transform this to geo-nwbib-cache structure
	 * and load into elasticsearch.
	 * 
	 * @param QID the wikidata Q-ID
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
			System.out.println("Can't get wikidata entities ");
		}
		return null;
	}

	private static final String HTTP_WWW_WIKIDATA_ORG_ENTITY = "http://www.wikidata.org/entity/";

	public static void lookupWikidataParseGeoData(final String ORT, final String WDID) {
		String newWdUrl = HTTP_WWW_WIKIDATA_ORG_ENTITY + WDID;
		System.out.println("lookup:" + newWdUrl);
		try (AsyncHttpClient client = new AsyncHttpClient()) {
			JsonNode jnode = toApiResponseGet(client, newWdUrl);
//			stream(jnode).map(transform2lobidWikidata()) //
//					.forEach(index2Es());
			String lat = jnode.findValue("latitude").asText();
			String lon = jnode.findValue("longitude").asText();

			System.out.println("lon,lat=" + lon + "," + lat);
			ArrayList<String> list = new ArrayList<String>();
			list.add(0, WDID);
			list.add(1, lon);
			list.add(2, lat);
			ortWdAndLatLonMap.put(ORT, list);
		} catch (Exception e) {
			System.out.println("Can't get wikidata entities ");
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

//	public static Function<JsonNode, Pair<String, JsonNode>> transform2lobidWikidata() {
//		return node -> {
//			ObjectNode root = null;
//			String id;
//			try {
//				JsonNode geoNode = node.findPath("P625").findPath("mainsnak")
//						.findPath("datavalue").findPath("value");
//				ObjectMapper mapper = new ObjectMapper();
//				root = mapper.createObjectNode();
//				ObjectNode focus = mapper.createObjectNode();
//				ObjectNode geo = mapper.createObjectNode();
//				root.set(FOCUS, focus);
//				JsonNode aliasesNode = node.findPath("aliases").findPath("de");
//				if (!aliasesNode.isMissingNode())
//					root.set("aliases", aliasesNode);
//				ArrayNode type = mapper.createObjectNode().arrayNode();
//				try {
//					id = node.with("entities").fieldNames().next();
//				} catch (NoSuchElementException ex) {
//					id = node.findPath("id").asText();
//				}
//				focus.put("id", HTTP_WWW_WIKIDATA_ORG_ENTITY + id);
//				root.put("id", NWBIB_SPATIAL_PREFIX + id);
//				root.set("type", conceptNode);
//				root.put("label",
//						node.findPath("labels").findPath("de").findPath("value").asText());
//				if (notationMap.containsKey(id))
//					root.put("notation", notationMap.get(id));
//				root.set("source", sourceNode);
//				if (!geoNode.isMissingNode()
//						&& !geoNode.findPath("latitude").isMissingNode()) {
//					focus.set("geo", geo);
//					geo.put("lat", geoNode.findPath("latitude").asDouble(0.0));
//					geo.put("lon", geoNode.findPath("longitude").asDouble(0.0));
//				} else {
//					LOG.info("No geo coords for " + node.findPath("labels").findPath("de")
//							.findPath("value").asText() + ": "
//							+ node.findPath("id").asText());
//				}
//				String locatedInId = node.findPath("P131").findPath("mainsnak")
//						.findPath("datavalue").findPath("value").findPath("id").asText();
//				if (!locatedInId.isEmpty()) {
//					JsonNode locatedInNode;
//					if (locatedInMapCache.containsKey(locatedInId)) {
//						locatedInNode = locatedInMapCache.get(locatedInId);
//					} else {
//						try (AsyncHttpClient client = new AsyncHttpClient()) {
//							JsonNode jnode = toApiResponseGet(client,
//									HTTP_WWW_WIKIDATA_ORG_ENTITY + locatedInId);
//							String locatedInLabel = jnode.findPath("labels").findPath("de")
//									.findPath("value").asText();
//							LOG.debug("Found locatedIn id:" + locatedInId + " with label "
//									+ locatedInLabel);
//							locatedInNode = jnode.findPath("labels").findPath("de");
//							locatedInMapCache.put(locatedInId, locatedInNode);
//						}
//					}
//					root.set("locatedIn", locatedInNode);
//				}
//				List<JsonNode> typeNode = node.findValues("P31");
//				if (!typeNode.isEmpty()) {
//					typeNode.parallelStream()
//							.forEach(e -> e.findValues("mainsnak")
//									.forEach(e1 -> type.add(HTTP_WWW_WIKIDATA_ORG_ENTITY
//											+ e1.findPath("datavalue").findPath("id").asText())));
//					focus.set("type", type);
//				}
//				LOG.debug("Wikidata-Type extracted for type " + type.toString());
//			} catch (Exception e) {
//				LOG.error("Couldn't build a json document from the wd-entity ", e);
//				return null;
//			}
//			return Pair.of(id, root);
//		};
//	}
}
