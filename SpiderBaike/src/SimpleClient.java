import java.io.IOException;
import java.text.ParseException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

 public class SimpleClient {
     public static String  getContent(String url, String decode) {  
         CloseableHttpClient httpclient = HttpClients.createDefault();  
         String content = "";
         try {  
             // 创建httpget.    
             HttpGet httpget = new HttpGet(url);  
             System.out.println("executing request " + httpget.getURI());  
             // 执行get请求.    
             CloseableHttpResponse response = httpclient.execute(httpget);  
             try {  
                 // 获取响应实体    
                 HttpEntity entity = response.getEntity();  
                 System.out.println("--------------------------------------");  
                 // 打印响应状态    
                 System.out.println(response.getStatusLine());  
                 if (entity != null) {  
                     // 打印响应内容长度    
                     System.out.println("Response content length: " + entity.getContentLength());  
                     // 打印响应内容    
//                     System.out.println("Response content: " + EntityUtils.toString(entity)); 
                     try{
                    	 content =  EntityUtils.toString(entity);
                    	 System.out.println(content);
                     }catch(Exception e){
                    	 e.printStackTrace();  
                     }
                 }  
                 System.out.println("------------------------------------");  
             } finally {  
                 response.close();  
             }  
         } catch (ClientProtocolException e) {  
             e.printStackTrace();  
         } catch (Exception e) {  
             e.printStackTrace();  
         } finally {  
             // 关闭连接,释放资源    
             try {  
                 httpclient.close();  
             } catch (IOException e) {  
                 e.printStackTrace();  
             }  
         } 
         return content;
     }  
     public static void main(String[] agrs){
    	 String Content = getContent("http://www.1905.com/search/?q=疯狂动物城","utf-8");
     }
 } 