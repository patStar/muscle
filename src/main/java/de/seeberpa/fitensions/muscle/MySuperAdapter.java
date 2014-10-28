package de.seeberpa.fitensions.muscle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import fit.Fixture;
import fit.Parse;

public class MySuperAdapter extends Fixture {	
	
	private List<JSONArray> configs;
	private JSONArray request;
	
	@Override
	public void doTable(Parse table) {
		
		request = new JSONArray();
		configs = new ArrayList<JSONArray>();
		
		if(args.length > 0){
			for(String arg : args){
				try {
					loadConfigFile(arg);
				} catch (IOException e) {
					e.printStackTrace();			
				}
			}
		}
		
		super.doTable(table);
			
		System.out.println(request.toString(1));
	}	
	
	private void loadConfigFile(String location) throws IOException {			
		
		String content = FileUtils.readFileToString(new File(location));
		content.replaceAll("^[ \t]*//.*", "");
		configs.add(new JSONArray(content));		
	}

	@Override
	public void doCell(Parse cell, int column) {
		String content = Parse.unformat(cell.body).replaceAll("\\[[?]\\]", "");
		
		String sentenceStructure = content.replaceAll("\\[[^]]*\\]", "[?]");

		List<String> allMatches = new ArrayList<String>();
		Pattern p = Pattern.compile("\\[[^]]*\\]");
		Matcher m = p.matcher(content);
		while (m.find()) {		
		   allMatches.add(m.group().substring(1, m.group().length()-1));
		}

		for(JSONArray jsonArray : configs){
			for(int i=0; i<jsonArray.length(); i++){
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				String type = (String) jsonObject.get("type");				
				if(type.equals("SentenceConfig")){
					String sentence = (String) jsonObject.get("sentence");
					JSONArray params = (JSONArray) jsonObject.get("params");
					if(allMatches.size() == params.length() && sentenceStructure.trim().equals(sentence.trim())){
						Map map = new HashMap<String, String>();
						for(int j=0; j<params.length(); j++){
							map.put(params.get(j),allMatches.get(j));
						}
						JSONObject obj = new JSONObject(map);		
						request.put(obj);
						right(cell);
						return;
					}
				}
			}
		}		
		
	}	
}
