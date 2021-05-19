package co.hudson.rossHtest;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class htmlToJsonReader {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String url = "https://dev-test.hudsonstaging.co.uk";
		downloadHTML(url);
	}
	
	public static String downloadHTML(String url) throws IOException
	{
		String outputHTML = "";
		StringBuilder sb = new StringBuilder();
		try {
			  
            // Create URL object
            URL Link = new URL(url);
            BufferedReader readr = 
              new BufferedReader(new InputStreamReader(Link.openStream()));
                
            // read each line from stream till end
            String line;
            while ((line = readr.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
  
            readr.close();
            outputHTML = sb.toString();
        }
  
        // Exceptions
        catch (MalformedURLException mue) {
            System.out.println("Malformed URL Exception raised");
        }
        catch (IOException ie) {
            System.out.println("IOException raised");
        }
		
		return cutHTML(outputHTML);
	}
	
	public static String cutHTML(String inputHTML)
	{
		StringUtils.substringBetween(inputHTML, "<body>", "</body>");
		String text = "";
		text = StringUtils.substringBetween(inputHTML, "<body>", "</body>").replaceAll("(?m)^​.*", "");
		//System.out.println(text);
		return getTags(text);
	}
	
	public static String getTags(String filteredHTML)
	{
		
		String HTML = filteredHTML;
		Document document = Jsoup.parse(HTML);
		StringBuilder rawData = new StringBuilder();
		
		Elements divs = document.select("div.product-tile");
	    for (Element div : divs) {
	    	Elements images = div.select("img");
			String textInsideWCart = div.text();
			String textInsideNoCart = textInsideWCart.replace(" Add to cart","");
	    	rawData.append(textInsideNoCart + "\n" + images.attr("abs:src") + "\n");
	        }
	    filteredHTML = rawData.toString();
		return mapToJson(filteredHTML);
	}
	
	public static String mapToJson(String plainText)
	{
		JSONArray jsa = new JSONArray();
		Matcher m = Pattern.compile("([^ \\r\\n]*) Quantity: ?(\\d+) Price: ?\\$?(\\d+(?:\\.\\d*)?)\\r?\\n(http[^ \\r\\n]*)").matcher(plainText);
		while (m.find()) {
		    String productName = m.group(1);
		    String quantity = m.group(2);
		    String price = m.group(3);
		    String img = m.group(4);

			JSONObject Product = new JSONObject();
			Product.put("product",productName);

			JSONObject metaData = new JSONObject();
			metaData.put("image_url",img);
			metaData.put("quantity",quantity);
			metaData.put("price",price);

			Product.put("metadata",metaData);
			jsa.put(Product);
		}
		return formatJSON(jsa.toString());
	}

	public static String formatJSON(String jsonArray)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement jsonElement =  new JsonParser().parse(jsonArray);
		PrintWriter out1 = null;

		String productsJSON = gson.toJson(jsonElement);
		System.out.println(productsJSON);
		try {
			out1 = new PrintWriter(new FileWriter("Products.json"));
			out1.write(productsJSON);
			out1.close();
		} catch (Exception ex) {
			System.out.println("error: " + ex.toString());
		}		return jsonArray;
	}

}
