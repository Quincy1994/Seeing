package movieCrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class movie_new {
    private String baseUrl = "http://news.sogou.com/news?query=";
    private int pageNum;
    private String Url;
    public String query;
    public List<String> newsList = new LinkedList<String>() ;   //所爬取的信息存储在列表中
    public String homePagecontent;
    public String newsContent = "";   //为方便存储，将所有信息合并成一个字符串。以换行符分隔
    //默认构造函数
    movie_new(){

    }

    movie_new(String q){
        //参数为搜索关键字
        query = q;
        homePagecontent = gethomepageContent(0);
        pageNum = getPageNumbers(homePagecontent);
        JoinNews() ;
        setNewsContent();
    }

    private String gethomepageContent(int pageNumber){
        try {
            Url = baseUrl + URLEncoder.encode(query, "utf-8")+"&page=" + pageNumber;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return call(Url);
    }

    //返回结果：该新闻的总页面数
    public int getPageNumbers(String content){
        String reg = "<div class=\"header-filt\">\n" +
                "\t\n" +
                "    <span class=\"filt-result\">找到相关新闻约(.*?)篇</span>\n";
        String a="";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()){
           a = matcher.group(1);
        }
        return Integer.parseInt(a)/10 ;
   }
    //返回结果：新闻标题 列表
    public List<String> getNewsTitle(String content){
        List<String> newsTitle = new LinkedList<String>();
        String reg = "<h3 class=\"vrTitle\">\n" +
                "<a href=\"(.*?)\".*?>(.*?)</a>\n"+"</h3>\n" ;
        Pattern pattern = Pattern.compile(reg) ;
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()){
            String a1 = matcher.group(1);   //news Link
            String a2 = matcher.group(2).replaceAll("<.*?>","").replaceAll("&.*?;","") ;  //newsTitle
            String a = a2 + "`"  +a1;
            newsTitle.add(a);
        }
        return newsTitle;
    }

    //返回结果：新闻源，与发布时间的列表
    public List<String> getNewsNametime(String content){
        List<String> nameAndtime = new LinkedList<String>();
        String reg = "<div class=\"news-info\">\n" +
                "<p class=\"news-from\">(.*?)&nbsp;(.*?)<";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()){
            String newsName = matcher.group(1).replaceAll("<.*?>","");
            String newsTime = matcher.group(2).replaceAll("<.*?>","");
            nameAndtime.add(newsName +"`"+newsTime);
        }
        return nameAndtime;
    }

    //返回结果：新闻正文
    public List<String> getNewsdetail(String content){
        List<String> Newsdetail = new LinkedList<String>();
        String reg = "<span id=\"summary.*?>(.*?)</span>";
        Pattern pattern = Pattern.compile(reg) ;
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()){
            String newsDetail = matcher.group(1).replaceAll("<.*?>","").replaceAll("&.*?;","") ;
            Newsdetail.add(newsDetail);
        }
        return Newsdetail;
    }

    //返回结果：“新闻标题链接、新闻标题、新闻源、新闻发布时间、新闻正文”组合后的列表
    public void JoinNews(){
        String content;
        for(int i=0;i<pageNum;i++) {
            content = gethomepageContent(i);
            List<String> newsTitle = getNewsTitle(content);
            List<String> newsNameTime = getNewsNametime(content);
            List<String> newsdetail = getNewsdetail(content);
            for (int j = 0; j < newsTitle.size(); j++) {
                String aa = newsTitle.get(j) + "`" + newsNameTime.get(j) + "`" + newsdetail.get(j);
                newsList.add(aa);
            }
        }
    }

    public void setNewsContent(){
        for(int i=0;i<newsList.size();i++){
            newsContent = newsContent + newsList.get(i) + "\n";
        }
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
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"gbk") );
            String line;
            while((line = in.readLine()) != null){
                content += line+ "\n" ;
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
