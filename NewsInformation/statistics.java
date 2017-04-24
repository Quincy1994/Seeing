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

public class statistics {
    public String base_url  = "http://www.cbooo.cn/search?k=";
    public String  statisticsContent = "";
    public String Usefull_url;
    public String query;
    public List<String> statistic = new LinkedList<String>();

    statistics(){

    }

    statistics(String a){
        query = a;
        play();
    }


    //参数：网页源代码
    //返回结果：可以找到票房数据的网页链接
    public void setUsefull_url(String content){
        List<String> Usefull = new LinkedList<String>() ;
        String reg = null;
        reg = "<a target=\"_blank\" href=\"(.*?)\" title=";

        Pattern pattern = Pattern.compile(reg) ;
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()){
            Usefull.add(matcher.group(1));
        }
        Usefull_url = Usefull.get(0);
    }

    public void setStatistic(){
        //将需要的数据匹配，存入列表
        //第一次匹配，获取累计票房和今日实时票房（如果有的话）；
        String content = call(Usefull_url);
        String reg1 = " <span class=\"m-span\">(.*?)<br />(.*?)</span>";
        Pattern pattern1 = Pattern.compile(reg1);
        Matcher matcher1 = pattern1.matcher(content) ;
        while(matcher1.find()){
            String aa = matcher1.group(1).replaceAll("<.*?>","") + "`"+ matcher1.group(2);
            statistic.add(aa);
        }

        //第二次匹配，获取内地票房的总票房数
        String reg2 = "内地票房&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "                                <a href=.*?>(总票房：.*?万)</a>\n" ;
        Pattern pattern2 = Pattern.compile(reg2);
        Matcher matcher2 = pattern2.matcher(content);
        while(matcher2.find()){
            String aa = matcher2.group(1).replaceAll("<.*?>","").replaceAll("&nbsp;","") ;
            statistic.add("内地票房`"+aa+"`" +
                    "(货币单位：人民币)");
        }

        //
        statistic.add("场均人次（人）"+"`"+"单周票房（万）"+"`"+"累计票房（万）"+"`"+"上映天数");

        //第三次匹配，获取每一周中场均人次（人）`单周票房（万）`累计票房（万）`上映天数的具体数据
        String reg3  = "<span>(第.*?日)\n" +
                "                                </td>\n" +
                "                                <td class=\"arrow\">(.*?)</td>\n" +
                "                                <td>(.*?)</td>\n" +
                "                                <td>(.*?)</td>\n" +
                "                                <td class=\"last\">(.*?)</td>";
        Pattern pattern3 = Pattern.compile(reg3);
        Matcher matcher3 = pattern3.matcher(content);
        while(matcher3.find()){
            String aa = matcher3.group(1).replace("</span>"," ") + "`" + matcher3.group(2) + "`" + matcher3.group(3) + "`" + matcher3.group(4) + "`" + matcher3.group(5);
            statistic.add(aa);
        }

        //
        statistic.add("香港票房`(货币单位：港币)");
        statistic.add("本周排名`单周票房（万）`累计票房（万）");

        //第四次匹配，获取该部电影香港票房的本周排名`单周票房（万）`累计票房（万）。
        String reg4 = "<span>(第.*?日)\n" +
                "                                </td>\n" +
                "                                    <td class=\"arrow\">(.*?)</td>\n" +
                "                                    <td>(.*?)</td>\n" +
                "                                    <td class=\"lasta\">(.*?)</td>";
        Pattern pattern4 = Pattern.compile(reg4);
        Matcher matcher4 = pattern4.matcher(content);
        while(matcher4.find()){
            String aa = matcher4.group(1).replace("</span>"," ") + "`" + matcher4.group(2) + "`" + matcher4.group(3) + "`" + matcher4.group(4);
            statistic.add(aa);
        }
    }

    public void setStatisticsContent(){
        for(int i=0;i<statistic.size();i++){
            statisticsContent +=  statistic.get(i) + "\n";
        }
    }

    public void play(){
        String content = call(base_url + query);
        setUsefull_url(content) ;
        setStatistic();
        setStatisticsContent();
    }

    //参数：网页链接
    //返回结果：网页源代码
    public String call(String url) {
        //参数为网页链接
        //返回内容为网页源码全部内容
        String content = "";
        BufferedReader in = null;
        try {
            URL readUrl = new URL(url);
            URLConnection connection = readUrl.openConnection(); //请求连接
            connection.connect();
            //注意网页的编码
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                content += line + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return content;
    }
}
