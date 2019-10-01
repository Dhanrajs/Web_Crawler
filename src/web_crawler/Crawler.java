package web_crawler;
import java.util.List;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.html.xpath.*;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class Crawler {
	public static void main(String args[])
	{
		WebClient client=new WebClient(BrowserVersion.FIREFOX_3);
		client.setCssEnabled(false);
		client.setJavaScriptEnabled(false);
		String searchURL = "https://boston.craigslist.org/search/ata";

		try{
			HtmlPage page = client.getPage(searchURL);
			HtmlElement element = (HtmlElement)page.getFirstByXPath("//a[@class='button next']");
			String link = element.getAttribute("href"); 
			
			if(!link.isEmpty())
			{
				HtmlAnchor anchor = page.getAnchorByHref(link);
				System.out.println("Link not empty: Anchor is..." + anchor);
				anchor.click();
			}
			else
			{
				System.out.println("link may be empty! please do check!");
			}
			
			client.setJavaScriptEnabled(false);
			System.out.println("Link:" + link);
					
			List<HtmlElement> items = (List<HtmlElement>)page.getByXPath("//li[@class='result-row']");
			if(items.equals(null)){
				System.out.println("null - No items found yet!");
		 	}
			else
			{
				for(HtmlElement htmlItem : items)
				{
					Product product = new Product();
					
					try
					{
						HtmlElement spanPrice = ((HtmlElement) htmlItem.getFirstByXPath(".//span[@class='result-price']")) ;
						String itemPrice = (String)spanPrice.asText();
						product.setPrice(new Integer(itemPrice.replace("$","")));
						System.out.println(itemPrice);
					}
					catch(Exception e)
					{
						e.printStackTrace();
						HtmlAnchor itemAnchor = null;
						System.out.println(itemAnchor.asText());
					}
					
					HtmlAnchor itemAnchor = ((HtmlAnchor) htmlItem.getFirstByXPath(".//p[@class='result-info']/a"));
					product.setTitle(itemAnchor.asText());
					System.out.println("Product Name:" + product.getTitle());
					System.out.println("Product Price:" + product.getPrice());
					
					MongoClient mongoClient = new MongoClient("localhost",27017);
					DB db = mongoClient.getDB("productDB");
					DBCollection collection = db.getCollection("product");

					BasicDBObject document = new BasicDBObject();
					document.put("Title", product.getTitle());
					document.put("Price",product.getPrice());
					collection.insert(document);
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
       