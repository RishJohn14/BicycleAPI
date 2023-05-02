//package uk.ac.cam.cares.jps.agent.Carpark;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.http.util.EntityUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 
 
 import java.io.*;
 import java.util.Properties;
 
 import javax.print.attribute.standard.JobHoldUntil;
 
 
 
 public class APIConnector
 {
     private String API_URL = "http://datamall2.mytransport.sg/ltaodataservice/BicycleParkingv2";
     private String Latitude;
     private String Distance;
     private String Longitude;

     private String accountKey;

     private static final String ERRORMSG = "Bicycle Parking data could not be retrieved";
     private static final Logger LOG = LogManager.getLogger(APIAgentLauncher.class);
   
 
     //Standard Constructor to initialise the instance variables
     public APIConnector(String URL, String Lo, String La, String D, String k)
     {
         API_URL=URL;
         Latitude=La;
         Longitude=Lo;
         Distance=D;
         accountKey = k;
     }
     
 
     //Constructor to initialise the variables according to the Properties file
 
     public APIConnector(String filepath) throws IOException
     {
         loadAPIConfigs(filepath);
     }      
 
     // Obtains Weather data in JSON format containing key:value pairs
 
     public JSONObject getReadings()
     {
         try{
             return retrieveData();
 
         }
         catch(IOException e)
         {
             LOG.error(ERRORMSG);
             throw new JPSRuntimeException(ERRORMSG,e);
         }
     }
 
     private JSONObject retrieveData() throws IOException, JSONException
     {  
         String path = API_URL;
         //appending parameters BicycleParkingv2?Lat=1.364897&Long=103.766094&Dist=1.5
         
         path = path+"?Lat="+Latitude+"&Long="+Longitude+"&Dist="+Distance;
 
         try ( CloseableHttpClient httpclient =  HttpClients.createDefault())
         {
             HttpGet readrequest = new HttpGet(path);
             readrequest.setHeader("AccountKey", accountKey);
             try ( CloseableHttpResponse response = httpclient.execute(readrequest))
             {
                 int status = response.getStatusLine().getStatusCode();
 
                 if(status==200) 
                 {
                     return new JSONObject(EntityUtils.toString(response.getEntity()));
 
                 }
                 else
                 {
                     throw new HttpResponseException(status,"Data could not be retrieved due to a server error");
                 }
 
             }
 
         }
 
     }
 
     private void loadAPIConfigs(String filepath) throws IOException
     {
         File file = new File(filepath);
         if(!file.exists())
         {
             throw new FileNotFoundException("There was no file found in the path");
         }
         
         try (InputStream input = new FileInputStream(file))
         {
             Properties prop = new Properties();
             prop.load(input);
 
             if(prop.containsKey("Bicycle.api_url"))
             {
                 this.API_URL = prop.getProperty("Bicycle.api_url");
             }
             else
             {
                 throw new IOException("The file is missing: \"Bicycle.api_url=<api_url>\"");
             }
             
             if(prop.containsKey("Bicycle.accountKey"))
             {
                 this.accountKey = prop.getProperty("Bicycle.accountKey");
             }
             else
             {
                 throw new IOException("The file is missing: \"Bicycle.accountKey=<accountKey>\"");
             }
             
 
         }
     }
 
 }
