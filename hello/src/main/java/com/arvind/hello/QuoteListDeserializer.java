package com.arvind.hello;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.arvind.model.Quote;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public class QuoteListDeserializer extends StdDeserializer<List<Quote>> { 
	 
    public QuoteListDeserializer() { 
        this(null); 
    } 
 
    public QuoteListDeserializer(Class<?> vc) { 
        super(vc); 
    }

	@Override
	public List<Quote> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<Quote> quotes = new ArrayList<>();
		
		JsonNode node = jp.getCodec().readTree(jp);
//        String pathPrefix = currentPath.isEmpty() ? "" : currentPath + "-";
		JsonNode nodeTicker = node.get("Meta Data");
		String ticker = nodeTicker.get("2. Symbol").asText();
		
		JsonNode nodeQ = node.get("Time Series (Daily)");
        Iterator<Map.Entry<String, JsonNode>> iter = nodeQ.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();            
            LocalDate qDate = LocalDate.parse(entry.getKey(), formatter);
    		BigDecimal pricePs = new BigDecimal(entry.getValue().get("4. close").asText());
    		
    		Quote quote = new Quote();
    		quote.setTicker(ticker);
    		quote.setPricePs(pricePs);
    		quote.setQuoteDate(qDate);
    		quotes.add(quote);
        }

		//		String ticker = node.get("Global Quote").get("01. symbol").asText();
//		BigDecimal pricePs = new BigDecimal(node.get("Global Quote").get("05. price").asText());
//		LocalDate qDate = LocalDate.parse(node.get("Global Quote").get("07. latest trading day").asText(), formatter);
//
//		Quote quote = new Quote();
//		quote.setTicker(ticker);
//		quote.setPricePs(pricePs);
//		quote.setQuoteDate(qDate);
		
//		ObjectMapper mapper = new ObjectMapper();
//		JsonNode root = mapper.readTree(json);
//		Map<String, String> map = new HashMap<>();
//		addKeys("", nodeQ, map, new ArrayList<>());
//		map.entrySet().forEach(System.out::println);		

		return quotes;
	}
 
	private void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> map, List<Integer> suffix) {
	    if (jsonNode.isObject()) {
	        ObjectNode objectNode = (ObjectNode) jsonNode;
	        Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
//	        String pathPrefix = currentPath.isEmpty() ? "" : currentPath + "-";

	        while (iter.hasNext()) {
	            Map.Entry<String, JsonNode> entry = iter.next();
				addKeys(currentPath/* pathPrefix + entry.getKey() */, entry.getValue(), map, suffix);
	        }
	    } else if (jsonNode.isArray()) {
	        ArrayNode arrayNode = (ArrayNode) jsonNode;

	        for (int i = 0; i < arrayNode.size(); i++) {
	            suffix.add(i + 1);
	            addKeys(currentPath, arrayNode.get(i), map, suffix);

	            if (i + 1 <arrayNode.size()){
	                suffix.remove(arrayNode.size() - 1);
	            }
	        }

	    } else if (jsonNode.isValueNode()) {
	        if (currentPath.contains("-")) {
	            for (int i = 0; i < suffix.size(); i++) {
	                currentPath += "-" + suffix.get(i);
	            }

	            suffix = new ArrayList<>();
	        }

	        ValueNode valueNode = (ValueNode) jsonNode;
	        map.put(currentPath, valueNode.asText());
	    }
	}

//    @Override
//    public Quote deserialize(JsonParser jp, DeserializationContext ctxt) 
//      throws IOException, JsonProcessingException {
//        JsonNode node = jp.getCodec().readTree(jp);
//        int id = (Integer) ((IntNode) node.get("id")).numberValue();
//        String itemName = node.get("itemName").asText();
//        int userId = (Integer) ((IntNode) node.get("createdBy")).numberValue();
// 
//        return new Quote();
//    }
}