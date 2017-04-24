package movieCrawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class movie_review {
    public String reviewsContent = "";
    private String reviewUrl ;
    private String query;    //电影名（在此为关键字）
    public List<String> reviewPageUrl = new LinkedList<String>();
    public List<String> reviewsList = new LinkedList<String>();
    movie_review(){}
    movie_review(String q){
        query = q;
        play();
    }

    //作用：根据所要搜索的电影名，得到该电影影评的链接。
    public void setReviewUrl(){
        String urlId="";
        String fristurl = null;
        try{
            fristurl = "https://www.douban.com/search?q=" + URLEncoder.encode(query,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String content = call(fristurl);
        String reg = " <span>\\[电影\\]</span>.*?&nbsp;<a href=\"(https:.*?)\".*?sid:(.*?)}.*?>"+query +".*?</a>";
        String aa  ="";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()) {
            urlId = matcher.group(2).replaceAll(" ","");
            break;
        }
        reviewUrl = "https://movie.douban.com/subject/" + urlId + "/reviews";
    }

    //根据该电影影评的首页链接，得到该电影影评前五页的链接
    public void setReviewPageurl(){
        reviewPageUrl.add(reviewUrl);
        String reg1 = "<a href=\"(\\?start=.*?)\"";
        Pattern pattern1 = Pattern.compile(reg1);
        Matcher matcher1 = pattern1.matcher(call(reviewUrl));
        while(matcher1.find()){
            String url = matcher1.group(1);
            reviewPageUrl.add(reviewUrl + url);
        }
    }

    //根据前五页的网页链接，保存影评者和影评内容的相关信息。
    public void getReviewList(){
        int pageNumber = 0;

        if(reviewPageUrl.size() >= 5){
            pageNumber = 5;
        }
        else
            pageNumber = reviewPageUrl.size();

        for(int i = 0;i< pageNumber ;i++){
            String reg = "<span property=\"v:reviewer\">(.*?)</span>.*?class=\"short-content\">(.*?)<a class";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(call(reviewPageUrl.get(i)));
            while(matcher.find()){
                String reviewer = matcher.group(1);
                String reviewContent = matcher.group(2).replaceAll(" ","").replaceAll("<.*?>","").replaceAll("&.*?;","").replaceAll("htt.*?m","").replaceAll("(ht.*?)","") ;
                if(reviewContent.indexOf("这篇影评可能有剧透没关系")==-1 ){
                    reviewsList.add(reviewer + "`" + reviewContent);
                }
            }
        }
    }

    public void setReviewsContent(){
        for(int i=0;i<reviewsList.size();i++ ){
            reviewsContent = reviewsContent + reviewsList.get(i) + "\n";
        }
    }

    public void play(){
        setReviewUrl();
        setReviewPageurl() ;
        getReviewList() ;
        setReviewsContent();
    }

    //参数：网页链接
    //返回结果：网页源代码
    public String call(String url){
        //参数为网页链接
        //返回内容为网页源码全部内容
        String content = "";
        BufferedReader in = null;
        try{
            URL readUrl = new URL(url);
            URLConnection connection = readUrl.openConnection(); //请求连接
            connection.connect();
            //注意网页的编码
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8") );
            String line;
            while((line = in.readLine()) != null){
                content += line;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(in != null){
                    in.close();
                }
            }catch(Exception e2){
                e2.printStackTrace();
            }
        }
        return content;
    }
}
