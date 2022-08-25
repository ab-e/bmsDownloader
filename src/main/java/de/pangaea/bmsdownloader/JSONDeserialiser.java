package de.pangaea.bmsdownloader;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.pangaea.bmsharvester.BMSDatasets;

public class JSONDeserialiser {

	private static HashMap<String,BMSDatasets> bmsMap;
	
	public static HashMap<String,BMSDatasets> deserialiseBMS(){
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		String json = FileIO.readFileForString("bmsArchives.json");
		Object obj = new JsonParser().parse(json);
		JsonArray jArray = (JsonArray) obj;
		bmsMap = new HashMap<String,BMSDatasets>();
		for(int i = 0; i < jArray.size(); i++) { 
			BMSDatasets bd = gson.fromJson((JsonObject) jArray.get(i), BMSDatasets.class);
			bmsMap.put(bd.getProviderId()+"_"+bd.getDatasetId(), bd);
		}
		return bmsMap;
	}

}
