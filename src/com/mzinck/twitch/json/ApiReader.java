package com.mzinck.twitch.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;

public class ApiReader {
    
    public static Map<String, String> netflixInfo(String title  ) {
        Map<String, String> map = new HashMap<String, String>();
                    
        JsonParserFactory factory=JsonParserFactory.getInstance();
        JSONParser parser=factory.newJsonParser();
        try {
            map = parser.parseJson(getJSON(title));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return map;
    }
    
    private static String getJSON(String title) throws Exception {
        BufferedReader reader = null;
        title = "http://www.omdbapi.com/?t=" + title + "&y=&plot=short&r=json";
        try {
            URL url = new URL(title);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read); 

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
    
}
