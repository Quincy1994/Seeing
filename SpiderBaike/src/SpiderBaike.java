import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.hankcs.hanlp.HanLP;


public class SpiderBaike {
	
	String movie = null;
	
	String url = null;
	String baikeUrl = null;
	String doubanUrl = null;
	String m1905Url = null;
	String mtimeUrl = null;
	
	String content = null;
	String doubanContent = null;
	String cbcMovieContent = null;
	String mtimeContent = null;
	String mtimeActorContent = null;
	
	Introduction introduction =null; //介绍
	Information information = null; //内容
	MainTheme mainTheme = null; //主题
	Manufacture manufacture = null; //制作
	List<Role> roleList = null; //角色
	
	Store store = null;
	
	public SpiderBaike(String movie, String decode) throws MyUrlException{
		this.movie = movie;
		this.url = "http://baike.baidu.com/item/"+movie;
//		this.url = "http://baike.baidu.com/link?url=BSCt9d577u5aAf1EyrruPfIUB4AOZqBGiqrnYEaibFzI759RRjYbsHs7tXx5eF3raVLOWV7lmWcLQZRTOeUf9Ale1y8MX6U-zncQ4J9ykqLRkN3zx52QuYQcgw9J4zH6sBD8NHe9d5uPd4ap2TUZATdrhGUusVcObux57dol9CeBfSLMpHdG_ERhH0eNQeJtGetm9DOCTP6IRoLgQBpEyq";
		try {
			this.content = getContent(this.url, decode);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//出现多义词情况，选定只有电影的
		if(!content.contains("演职员表")){
			String searchUrl = "https://www.baidu.com/s?wd=" + movie + "电影百度百科";
			String searchContent = "";
			try {
				searchContent = get(searchUrl,"utf-8");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String regexUrl = "<h3 class=\"t c-gap-bottom-small\"><a href=\"(.*?)\" target=\"_blank\"><em>" + movie +"</em>_百度百科</a>";
			Pattern pattern = Pattern.compile(regexUrl);
			Matcher matcher = pattern.matcher(searchContent);
			if(matcher.find()){
				this.url =matcher.group(1);
				System.out.println(this.url);
				try {
					this.content = getContent(this.url, decode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//创建保存电影的地方
		this.store = new Store(movie);
		
		System.out.println(this.content);
		this.doubanContent = this.getDoubanMovie(movie);
//		this.m1905Content = this.getM1905Movie(movie);
		this.introduction = this.getIntroduction();
		this.information = this.getInformation();
		this.mainTheme = this.getMainTheme();
		this.mtimeContent = this.getMtimeMovie();
		this.manufacture = this.getManfacture();
		this.roleList = this.getRoleList();
	}
	
	public static void main(String[] agrs){
	    List<String> movieList = getMovies();
//		String[] movieList = new String[]{"乘风破浪"};
		for(String movie: movieList){
			if(!movie.isEmpty()){
			try {
				spider(movie);
			} catch (MyUrlException e) {
				// TODO Auto-generated catch block
				System.out.println("url is empty");
				System.out.println(Store.deleteDir(movie));
			}
			}
		}
	}
	
	public static List<String> getMovies(){
		File file = null;
		List<String> movieList = new ArrayList<String>();
		String movieFile = "movie.txt";
		try{
			file = new File(movieFile);
			BufferedReader fr = new BufferedReader(new FileReader(file));
			String movie;
			while((movie = fr.readLine())!= null){
				movieList.add(movie);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return movieList;
	}
	public static void spider(String movie) throws MyUrlException{
		
		String decode = "utf-8";
		SpiderBaike sb = new SpiderBaike(movie, decode);
		System.out.println("电影:" + movie);
		System.out.println("--------------------------------------------------------------------");
		System.out.println("基本介绍: ");
		System.out.println("电影评分: "+ sb.introduction.score);
		System.out.println("电影票房: " + sb.introduction.boxOffice);
		System.out.println("上映时间: " + sb.introduction.release_time);
		System.out.println("国家地区: " + sb.introduction.area);
		System.out.println("片长: " + sb.introduction.length);
		System.out.println("图片: " + sb.introduction.imgUrl);
		List<String> keywords = sb.introduction.keywords;
		System.out.println("关键词列表: ");
		for(String keyword: keywords){
			System.out.print(keyword + " ");
		}
		System.out.println();
		System.out.println();
		System.out.println("--------------------------------------------------------------------");
		System.out.println("内容:");
		System.out.println("剧情解析:"+ sb.information.story);
		System.out.println("豆瓣评论url:"+ sb.information.doubanUrl);
		System.out.println("影评评价:"+ sb.information.comment);
		System.out.println("获奖情况: ");
		List<String> prizes = sb.information.prize;
		for(String one: prizes){
			System.out.println(one);
		}
		
		System.out.println();
		System.out.println();
		System.out.println("--------------------------------------------------------------------");
		System.out.println("主题:");
		System.out.println("风格:" + sb.mainTheme.style);
		System.out.println("题材内容:" + sb.mainTheme.theme);
		System.out.println("相关电影:");
		for(String smovie : sb.mainTheme.similarMovie){
			System.out.print(smovie + " ");
		}
		
		System.out.println();
		System.out.println();
		System.out.println("--------------------------------------------------------------------");
		System.out.println("制作:");
		System.out.println("出品公司");
		for(Company company: sb.manufacture.companyList){
			System.out.println("名称:" + company.cname);
			System.out.print("未来作品:");
			for(String  represent : company.futureRepresent){
				System.out.print(represent + " ");
			}
			System.out.println();
			System.out.print("过去作品:");
			for(String represent: company.pastRepresent){
				System.out.print(represent + " ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("导演:");
		for(Director director: sb.manufacture.directors){
			System.out.println("名称:" + director.dname);
			System.out.println("图片:" + director.imgurl);
			System.out.println("评分:" + director.rate);
			System.out.print("代表作品:");
			for(String rp : director.represent){
				System.out.print(rp + " ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("编剧:");
		for(ScriptWriter scriptwriter: sb.manufacture.scriptwriters){
			System.out.println("名称:" + scriptwriter.sname);
			System.out.print("代表作品:");
			for(String rp : scriptwriter.represent){
				System.out.print(rp + " ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		System.out.println("--------------------------------------------------------------------");
		System.out.println("角色:");
		for(Role role : sb.roleList){
			System.out.println("角色名:" + role.name);
			System.out.println("角色图片:" + role.imgurl);
			System.out.println("角色介绍:" + role.introduction);
			System.out.println("对应的演员:" + role.actor.name);
			System.out.println("演员出生日期:" + role.actor.birthday);
			System.out.println("演员国籍:" + role.actor.country);
			System.out.println("演员图片:" + role.actor.imgurl);
			System.out.println("演员受欢迎程度:" + role.actor.like);
			System.out.print("演员代表作品:");
			for(String rp: role.actor.represent){
				System.out.print(rp + " ");
			}
			System.out.println();
		}
		if(sb.information.story.equals("")){
			System.out.println("error ,  store fail ");
			return ;
		}
		Store.storeMovie(sb);
	}
	public static String getContent(String url, String decode){
		String content = "";
		while(true){
			try{
				content = get(url, decode);
				break;
			}catch(Exception e){
				System.out.println("time out!");
			}
		}
		return content;
	}
	
	public static String getContent(String url){
		String content = "";
		while(true){
			try{
				content = get(url);
				
				break;
			}catch(Exception e){
				System.out.println("time out!");
			}
		}
		return content;
	}
	public static String get(String url, String decode) throws Exception {
		String content = "";
		BufferedReader in = null;
		System.out.println(url);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
				URL realUrl = new URL(url);
				URLConnection connection = realUrl.openConnection();
				connection.setConnectTimeout(2000);
				connection.setReadTimeout(5000);
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0");
				connection.connect(); 
				in = new BufferedReader(new InputStreamReader(connection.getInputStream(), decode));
				String line;
				while((line = in.readLine()) != null){
					content += line;
				}
		}
		finally {
			try {
				if( in != null){
					in.close();
				}
			}catch(Exception e2){
				e2.printStackTrace();
			}
		}
		content = content.replace("&nbsp", "");
		System.out.println(" OK !");
		return content;
	}
	public static String  get(String url) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        CloseableHttpClient httpclient = HttpClients.createDefault(); 
//        httpclient.getParams().setIntParameter("http.socket.timeout", 3000);
        String content = "";
        try {  
            // 创建httpget.    
            HttpGet httpget = new HttpGet(url);  
            httpget.setProtocolVersion(HttpVersion.HTTP_1_0);
            httpget.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
            System.out.println("executing request " + httpget.getURI());  
           
            // 执行get请求.    
            CloseableHttpResponse response = httpclient.execute(httpget);  
            try {  
                // 获取响应实体    
                HttpEntity entity = response.getEntity();  
//                System.out.println("--------------------------------------");  
                // 打印响应状态    
                System.out.println(response.getStatusLine());  
                if (entity != null) {  
                    // 打印响应内容长度    
//                    System.out.println("Response content length: " + entity.getContentLength());  
                    // 打印响应内容    
//                    System.out.println("Response content: " + EntityUtils.toString(entity)); 
                    try{
                   	 content =  EntityUtils.toString(entity);
                    }catch(Exception e){
                   	 System.out.println("download error");  
                    }
                }  
//                System.out.println("------------------------------------");  
            } finally {  
                response.close();  
            }  
        } catch (ClientProtocolException e) {  
            System.out.println("download error");  
        } catch (Exception e) {  
            System.out.println("download error");  
        } finally {  
            // 关闭连接,释放资源    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
                System.out.println("download error");  
            }  
        } 
        return content;
    }  
	public  String getDoubanMovie(String movie) throws MyUrlException{
		String dbSearchUrl = "https://movie.douban.com/subject_search?search_text=" +  movie;
		String searchContent = getContent(dbSearchUrl, "utf-8");
		String regexdbMovie = " <a class=\"nbg\" href=\"(.*?)\" onclick=\".*?\" title=\""+movie+"\">";
		Pattern pattern = Pattern.compile(regexdbMovie);
		Matcher matcher = pattern.matcher(searchContent);
		String dbMovieUrl = "";
		if(matcher.find()){
			dbMovieUrl = matcher.group(1);
			Pattern pattern_2 = Pattern.compile("https://movie.douban.com/subject/.*?/");
			Matcher matcher_2 = pattern_2.matcher(dbMovieUrl);
			if(matcher_2.find()){
				dbMovieUrl = matcher_2.group();
			}
		}
		this.doubanUrl = dbMovieUrl;
		checkUrl(this.doubanUrl);
		String movieContent = getContent(dbMovieUrl, "utf-8");
		return movieContent;
	}
	public static void checkUrl(String url) throws MyUrlException {
		if(url.isEmpty()){
			throw new MyUrlException(url);
		}
	}
	public String getM1905Movie(String movie){
		String m1905Url = "http://www.1905.com/search/?q=" + movie ;
		String searchContent = getContent(m1905Url);
		searchContent = searchContent.replace("\n", "");
		String regexM1905Movie = "<a href=\"(.*?)\" target=\"_blank\" class=\"img-a\">.*?alt=\""+movie+"\" title=\""+ movie+"\"></a>";
		Pattern pattern = Pattern.compile(regexM1905Movie);
//		System.out.println(searchContent);
		Matcher matcher = pattern.matcher(searchContent);
		String m1905MovieUrl = "123";
		if(matcher.find()){
			m1905MovieUrl = matcher.group(1);
		}
		this.m1905Url = m1905MovieUrl;
		String movieContent = getContent(this.m1905Url,"utf-8");
//		String movieContent = "";
		return movieContent;
	}
	
	public String getMtimeMovie(){
		String movie = this.movie;
		String mtimeUrl = "http://service.channel.mtime.com/Search.api?Ajax_CallBack=true&Ajax_CallBackType=Mtime.Channel.Services&Ajax_CallBackMethod=GetSearchResult&Ajax_CrossDomain=1&Ajax_RequestUrl=http://search.mtime.com/search/?q="+movie+"&t=1&t=2016113021433167701&Ajax_CallBackArgument0="+movie+"&Ajax_CallBackArgument1=1&Ajax_CallBackArgument2=290&Ajax_CallBackArgument3=0&Ajax_CallBackArgument4=1";
		String searchContent = getContent(mtimeUrl, "utf-8");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String regexId = "\"movieId\":(.*?),\"movieTitle\"";
		Pattern pattern = Pattern.compile(regexId);
		Matcher matcher = pattern.matcher(searchContent);
		String id = "";
		if(matcher.find()){
			id = matcher.group(1);
		}
		this.mtimeUrl = "http://movie.mtime.com/" + id ;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("download error");
		}
//		String mtimeContent = getContent(this.mtimeUrl, "utf-8");
		String mtimeContent = "";
		return mtimeContent;
	}
	
	public  Introduction getIntroduction() throws MyUrlException{
		String score = this.getScore();  //获取电影评分
		String boxOffice = this.getBoxOffice(); //获取票房成绩
		String release_time = this.getRelease_time(); //获取上映时间
		String area = this.getArea();//获取地区
		String length = this.getLength(); //获取片长
		List<String> keywords = this.getKeyword(); //获取关键词列表
		String imgurl =getImgurl(); //获取电影图片
		try {
			imgurl = this.store.downloadImage(imgurl, "movieImg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("download error");
		}
		Introduction introduction = new Introduction(score, boxOffice, release_time,area, length, keywords, imgurl);
		return introduction;
	}
	

	public Information getInformation(){
		String story = this.getStory();
		String  comment = this.getComment();
		String doubanUrl = this.doubanUrl;
		List<String> prize = this.getPrize();
		Information information = new Information(story, comment, doubanUrl, prize);
		return information;
	}

	public MainTheme getMainTheme(){
		String style = this.getStyleStr(); //获取风格
		String theme = this.getThemeStr();//获取主题
		List<String> similarMovies = this.getSimilarMovie();//获取相关电影
		MainTheme mainTheme  = new MainTheme(style, theme, similarMovies);
		return mainTheme;
	}
	
	public Manufacture getManfacture(){
		List<Company> companyList = this.getCompanyList() ;
		List<Director> directors = this.getDirectorList();
		List<ScriptWriter> scriptwriters = this.getSriptWriterList();
		Manufacture manufacture = new Manufacture(companyList, directors, scriptwriters);
		return manufacture;
	}
	
	public String getDescription(){
		String regexIntro = "<meta name=\"description\" content=\"(.*?)\">";
		Pattern pattern = Pattern.compile(regexIntro);
		Matcher matcher = pattern.matcher(this.content);
		String description = "";
		while(matcher.find()){
			description += matcher.group(1);
		}
		return description;
	}
	
	public List<String> getKeyword(){
//		String regexKeywords = "<strong.*?>电影基因:</strong>.*?<p>(.*?)</p> ";
//		String keyWords = "";
//		Pattern pattern = Pattern.compile(regexKeywords);
//		Matcher matcher = pattern.matcher(this.m1905Content);
//		if(matcher.find()){
//			keyWords= matcher.group(1);
//		}
//		keyWords = keyWords.replace("</a>", "`");
//		keyWords = keyWords.replaceAll("<.*?>", "");
//		keyWords = keyWords.replace(" ", "");
//		keyWords = keyWords.replace("：", "");
		String keyWords ="";
		if(keyWords.equals("")){
			String regexContent = "<span property=\"v:summary\".*?>(.*?)</span>";
			Pattern pattern = Pattern.compile(regexContent);
			Matcher matcher = pattern.matcher(this.doubanContent);
			String tmpContent = "";
			if(matcher.find()){
				tmpContent = matcher.group(1);
				List<String> keywordList = HanLP.extractKeyword(tmpContent, 5);
				return keywordList;
			}
		}
		List<String> keyWordList = new ArrayList<String>();
		String[] keys = keyWords.split("`");
		for(String word: keys){
			keyWordList.add(word);
		}
		
		return keyWordList;
	}
	
	public String getBoxOffice() throws MyUrlException{
		String boxOffice ="";
		String boxOfficeUrl = "http://www.cbooo.cn/search?k="+this.movie;
		String cboContent = getContent(boxOfficeUrl, "utf-8");
		String regexboxMovie = "<a target=\"_blank\" href=\"(.*?)\" title=\""+ this.movie +"\">";
		Pattern pattern = Pattern.compile(regexboxMovie);
		Matcher matcher = pattern.matcher(cboContent);
		String cbcmovieUrl = "";
		if(matcher.find()){
			cbcmovieUrl = matcher.group(1);
			Pattern pattern_2 = Pattern.compile("http://www.cbooo.cn/m/\\d+");
			Matcher matcher_2 = pattern_2.matcher(cbcmovieUrl);
			if(matcher_2.find()){
				cbcmovieUrl = matcher_2.group();
			}
		}
//		cbcmovieUrl = "http://www.cbooo.cn/m/655647";
		checkUrl(cbcmovieUrl);
		this.cbcMovieContent = getContent(cbcmovieUrl, "utf-8");
		String regexBoxOffice = "<span class=\"m-span\">累计票房<br />(.*?)</span>";
		pattern = Pattern.compile(regexBoxOffice);
		matcher = pattern.matcher(cbcMovieContent);
		if(matcher.find()){
			boxOffice = matcher.group(1);
		}
		return boxOffice;
	}
	public String getScore(){
		String regexScore = " <strong class=\"ll rating_num\" property=\"v:average\">(.*?)</strong> ";
		Pattern pattern = Pattern.compile(regexScore);
		Matcher matcher = pattern.matcher(this.doubanContent);
		String score = "";
		if(matcher.find()){
			score = matcher.group(1);
		}
		return score;
	}
	
	public String getRelease_time(){
		String regexRealeaseTime = "<span class=\"pl\">上映日期:</span> <span property=\"v:initialReleaseDate\" content=\"(.*?)\">";
		Pattern pattern = Pattern.compile(regexRealeaseTime);
		Matcher matcher = pattern.matcher(this.doubanContent);
		String release_time = "";
		if(matcher.find()){
			release_time = matcher.group(1);
		}
		return release_time;
	}
	
	public String getArea(){
		String regexArea = "<span class=\"pl\">制片国家/地区:</span>(.*?)<br/>";
		Pattern pattern = Pattern.compile(regexArea);
		Matcher matcher = pattern.matcher(doubanContent);
		String area = "";
		if(matcher.find()){
			area = matcher.group(1);
		}
		return area;
	}
	
	public String getLength(){
		String regexLength = "<span class=\"pl\">片长:</span> <span property=\"v:runtime\" content=\".*?\">(.*?)</span>";
		Pattern pattern = Pattern.compile(regexLength);
		Matcher matcher = pattern.matcher(doubanContent);
		String length = "";
		if(matcher.find()){
			length = matcher.group(1);
		}
		return length;
	}
	public String getImgurl(){
		String regexImgurl = "<img style=\".*?\" src=\"(.*?)\" alt=\""+this.movie+"\" />";
		Pattern pattern = Pattern.compile(regexImgurl);
		Matcher matcher = pattern.matcher(this.cbcMovieContent);
		String imgurl = "";
		if(matcher.find()){
			imgurl = matcher.group(1);
		}
		if(imgurl.equals("")){
			String regeximgurl = "<img src=\"http://e.hiphotos.baidu.com(.*?)jpg>";
			pattern = Pattern.compile(regeximgurl);
			matcher = pattern.matcher(this.content);
			if(matcher.find()){
				imgurl =matcher.group(1);
				imgurl = "http://e.hiphotos.baidu.com"+ imgurl +"jpg";
			}
			
		}
		return imgurl;
	}
	
	public String getStory(){
		String regexPlot = "<h2 class=\"title-text\".*?剧情简介</h2>.*?<div class=\"para\" label-module=\"para\">(.*?)</div><div class=\"anchor-list\">";
		Pattern pattern = Pattern.compile(regexPlot);
		content = content.replace("\n", "");
		Matcher matcher = pattern.matcher(content);
		String story = "";
		if(matcher.find()){
			story = matcher.group(1);
		}
		story = story.replaceAll("<.*?>", "");
		return story;
	}
	
	public String getComment(){
		String regexComment = "<a name=\"影片评价\" class=\"lemma-anchor \" ></a>(.*?)<span class=\"title-text\">电影评价</span>";
		Pattern pattern = Pattern.compile(regexComment);
		Matcher matcher = pattern.matcher(content);
		String commentStr = "";
		if(matcher.find()){
			commentStr = matcher.group(1);
		}
		commentStr = commentStr.replaceAll("<.*?>", "");
		return commentStr;
	}
	
	public List<String> getPrize(){
		String regexPrize = " <ul class=\"award\">.*?<li>.*?<a href=\".*?\">(.*?)</a>.*?</li>.*?<li>(.*?)</li>.*?<li>";
		Pattern pattern = Pattern.compile(regexPrize);
		Matcher matcher = pattern.matcher(this.doubanContent);
		List<String> prize = new ArrayList<String>();
		while(matcher.find()){
			String one = "";
			one += matcher.group(1) + " " + matcher.group(2);
			prize.add(one);
		}
		return prize;
	}
	public String getStyleStr(){
		String regexStyle = "<span property=\"v:genre\">(.*?)</span>";
		Pattern pattern = Pattern.compile(regexStyle);
		Matcher matcher = pattern.matcher(this.doubanContent);
		String styleStr = "";
		while(matcher.find()){
			styleStr += matcher.group(1) + " ";
		}
		return styleStr;
	}
	
	public String getThemeStr(){
		String regexTheme = "<div class=\"lemmaWgt-lemmaSummary lemmaWgt-lemmaSummary-light\">.(.*?)</div>";
		Pattern pattern = Pattern.compile(regexTheme);
		Matcher matcher = pattern.matcher(this.content);
		String themeStr = "";
		if(matcher.find()){
			themeStr = matcher.group(1);
			themeStr = themeStr.replaceAll("<.*?>", "");
		}
		return themeStr;
	}
	
	public List<String> getSimilarMovie(){
		String regexSimilarMovieBlock = " <div class=\"recommendations-bd\">(.*?)</div>";
		Pattern pattern = Pattern.compile(regexSimilarMovieBlock);
		Matcher matcher = pattern.matcher(this.doubanContent);
		String block = "";
		if(matcher.find()){
			block = matcher.group(1);
		}
		String regexSimilarMovie = "<a href=\".*?\" class=\"\" >(.*?)</a>";
		pattern = Pattern.compile(regexSimilarMovie);
		matcher = pattern.matcher(block);
		List<String> similarMovie = new ArrayList<String>();
		while(matcher.find()){
			similarMovie.add(matcher.group(1));
		}
		return similarMovie;
	}
	
	public List<Company> getCompanyList(){
		String url = this.mtimeUrl + "/details.html#company";
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("download error");
		}
		String companyContent = getContent(url, "utf-8");
		String regexCompanyBlock = "<h4>制作公司</h4>.*?<ul>(.*?)</ul>.*?<h4>发行公司</h4>";
		Pattern pattern = Pattern.compile(regexCompanyBlock);
		Matcher matcher = pattern.matcher(companyContent);
		String block = "";
		if(matcher.find()){
			block = matcher.group(1);
		}
		String regexCompany = "<li>.*?<a href=\"(.*?)\" target=\"_blank\">(.*?)</a>.*?</li> ";
		pattern = Pattern.compile(regexCompany);
		matcher = pattern.matcher(block);
		List<Company> companyList = new ArrayList<Company>();
		while(matcher.find()){
			String companyUrl = matcher.group(1);
			String cname = matcher.group(2).replace("&#183;", "");
			Company company = this.getCompany(companyUrl, cname);
			companyList.add(company);
		}
		return companyList;
	}
	
	
	public List<Director> getDirectorList(){
		String actorUrl = this.mtimeUrl + "/fullcredits.html";
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("download error");
		}
		this.mtimeActorContent = getContent(actorUrl, "utf-8" );
		String regexDirectorblock = "<h4>导演 Director</h4>(.*?)<h4>编剧 Writer</h4>";
		Pattern pattern = Pattern.compile(regexDirectorblock);
		Matcher matcher = pattern.matcher(this.mtimeActorContent);
		String block = "";
		if(matcher.find()){
			block = matcher.group(1);
		}
		//第一种获取导演的方法
		String regexDirectorList = "<a title=\"(.*?)\" target=\"_blank\" href=\"(.*?)\">.*?<img.*?src=\"(.*?)\" /></a>";
		pattern = Pattern.compile(regexDirectorList);
		matcher = pattern.matcher(block);
		List<Director> directorList = new ArrayList<Director>();
		while(matcher.find()){
			String dname = matcher.group(1);
			String directorurl = matcher.group(2);
		    People people = this.getPeople(directorurl);
		    List<String> represent = people.represent;
		    String rate = people.rate;
		    String imgurl = people.imgurl;
		    try {
				imgurl = this.store.downloadImage(imgurl, dname);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("download error");
			}
		    Director director = new Director(dname, represent, rate, imgurl);
			directorList.add(director);
		}
		
		//第二种获取导演的方法
		regexDirectorList = "<p><a href=\"(.*?)\" target=\"_blank\">(.*?)</a></p>";
		pattern = Pattern.compile(regexDirectorList);
		matcher = pattern.matcher(block);
		while(matcher.find()){
			String dname = matcher.group(2);
			String directorurl = matcher.group(1);
			String imgurl = "";
		
		    People people = this.getPeople(directorurl);
		    List<String> represent = people.represent;
		    String rate = people.rate;
		    try {
				imgurl = people.imgurl;
				imgurl = store.downloadImage(imgurl, dname);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("download error");
			}
		    Director director = new Director(dname, represent, rate, imgurl);
			directorList.add(director);
		}
    	return directorList;
    }
	
	public List<ScriptWriter> getSriptWriterList(){
		String regexblock = "<h4>编剧 Writer</h4>(.*?)</div>";
		Pattern pattern = Pattern.compile(regexblock);
		Matcher matcher = pattern.matcher(this.mtimeActorContent);
		String block = "";
		if(matcher.find()){
			block = matcher.group(1);
		}
		String regexDirectorList = "<a target=\"_blank\" href=\"(.*?)\">(.*?)</a>";
		pattern = Pattern.compile(regexDirectorList);
		matcher = pattern.matcher(block);
		List<ScriptWriter> scriptWriterList = new ArrayList<ScriptWriter>();
		int num = 3;
		while(matcher.find() && num >= 0){
			String writerurl = matcher.group(1);
			String wname = matcher.group(2);
			People people = this.getPeople(writerurl);
			List<String> represent = people.represent;
			ScriptWriter scriptwriter = new ScriptWriter(wname, represent);
			scriptWriterList.add(scriptwriter);
			num -= 1;
		}
    	return scriptWriterList;
	}
	public  People  getPeople(String url){
		String content = getContent(url, "utf-8");
		String regexImgurl = "<a href=\"http://people.mtime.com/.*?/photo_gallery/\" target=\"_self\"><img src=\"(.*?)\" alt=";
		Pattern pattern = Pattern.compile(regexImgurl);
		Matcher matcher = pattern.matcher(content);
		String imgurl = "";
		if(matcher.find()){
			imgurl = matcher.group(1);
		}
		
		String regexId = "http://people.mtime.com/(.*?)/";
	    pattern = Pattern.compile(regexId);
	    matcher = pattern.matcher(url);
	    String pid = "";
	    if(matcher.find()){
	    	pid = matcher.group(1);
	    }
		String peopleUrl = "http://people.mtime.com/"+pid+"/filmographies/";
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("download error");
		}
		String peopleContent = getContent(peopleUrl,"utf-8");
		String regexMovie = "<a href=\"http://movie.mtime.com/.*?\" target=\"_blank\">(.*?)</a>";
		pattern = Pattern.compile(regexMovie);
		matcher = pattern.matcher(peopleContent);
		List<String> represent = new ArrayList<String>();
		while(matcher.find()){
			represent.add(matcher.group(1));
		}
	    String ratingUrl = "http://service.library.mtime.com/Person.api?Ajax_CallBack=true&Ajax_CallBackType=Mtime.Library.Services&Ajax_CallBackMethod=GetPersonRatingInfo&Ajax_CrossDomain=1&Ajax_RequestUrl=http://people.mtime.com/"+pid+"/&Ajax_CallBackArgument0="+pid;
	    String ratingContent = getContent(ratingUrl,"utf-8");
	    String regexRating = "finalRating\":(.*?),\"ratingCount";
	    pattern = Pattern.compile(regexRating);
	    matcher = pattern.matcher(ratingContent);
	    String rate= "";
	    if(matcher.find()){
	    	rate = matcher.group(1);
	    }
	    People people = new People(pid, represent, rate, imgurl);
	    return people;
	}
	
	
    public Company getCompany(String companyUrl, String cname){
    	List<String>  futureRepresent = new ArrayList<String>();
    	List<String>  pastRepresent = new ArrayList<String>();
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("download error");
		}
    	String representContent = getContent(companyUrl, "utf-8");
    	String regexRepresent = "<div class=\".*?\" movieid=\".*?\" year=\"(.*?)\" month=\".*?\" day=\".*?\" >	.*?<a href=\".*?\" class=\"img\" target=\"_blank\" title=\"(.*?)\">";
    	Pattern pattern = Pattern.compile(regexRepresent);
		Matcher matcher = pattern.matcher(representContent);
		while(matcher.find()){
			int year = Integer.parseInt(matcher.group(1));
			String represent = matcher.group(2);
			if(year > 2016){
				futureRepresent.add(represent);
			}
			else{
				pastRepresent.add(represent);
			}
		}
		 Company company = new Company(cname,  futureRepresent,  pastRepresent,  representContent);
		 return company;
    }
    
    public List<Role> getRoleList(){
    	String regexRole = "<li class=\"roleIntroduction-item \">(.*?)</li>";
    	Pattern pattern = Pattern.compile(regexRole);
    	Matcher matcher = pattern.matcher(this.content);
    	List<Role> roleList = new ArrayList<Role>();
    	while(matcher.find()){
    		String block = matcher.group(1);
    		String  regexImg = "<img src=\"(.*?)\"";
    		Pattern pattern_2 = Pattern.compile(regexImg);
    		Matcher matcher_2 = pattern_2.matcher(block);
    		String imgurl = "123";
    		if(matcher_2.find()){
    			imgurl = matcher_2.group(1);
    		}
    		String regexRoleName = "<div class=\"role-name\"><span class=\"item-value\">(.*?)</span>";
    		pattern_2 = Pattern.compile(regexRoleName);
    		matcher_2 = pattern_2.matcher(block);
    		String roleName = "";
    		if(matcher_2.find()){
    			roleName = matcher_2.group(1);
    			roleName = roleName.replaceAll("<.*?>", "");
    		}
    		String regexIntro = "<dd class=\"role-description\">(.*?)</dd>";
    		pattern_2 =  Pattern.compile(regexIntro);
    		matcher_2 = pattern_2.matcher(block);
    		String introduction = "";
    		if(matcher_2.find()){
    			introduction = matcher_2.group(1);
    		}
    		String regexActor = "<span class=\"item-key\">演员</span><span class=\"item-value\">(.*?)</span>";
    		pattern_2 = Pattern.compile(regexActor);
    		matcher_2 = pattern_2.matcher(block);
    		String aid = null;
    		String actorName = "";
    		Actor actor = null;
    		if(matcher_2.find()){
    			aid = matcher_2.group(1);
    			aid = aid.replaceAll("<.*?>", "");
    			String[] tokens = null;
    			if(aid.contains("；")){
        			tokens = aid.split("；");
        			aid = tokens[0];
    			}
    			System.out.println(aid);
    			if(aid != null){
    				actor = this.getActor(aid);
    				try {
						imgurl = this.store.downloadImage(imgurl, roleName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("download error");
					}
    				if(actor != null){
	    				Role role = new Role(roleName,  introduction,actor, imgurl);
	    	    		roleList.add(role);
    				}
    			}
    		}
    	}
    	return roleList;
    }
    
    public Actor getActor(String aid){
    	String actUrl = "http://baike.baidu.com/item/"+ aid;
    	String actContent ="";
		try {
			actContent = get(actUrl, "utf-8");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			return null;
		}
    	System.out.println(actContent);
    	String regexName = "<title>(.*?)_百度百科</title>";
    	Pattern pattern = Pattern.compile(regexName);
    	Matcher matcher = pattern.matcher(actContent);
    	String actorname = "";
    	if(matcher.find()){
    		actorname = matcher.group(1);
    	}
    	String regexBirthday = "dt class=\"basicInfo-item name\">出生日期</dt><dd class=\"basicInfo-item value\">(.*?)</dd>";
    	pattern = Pattern.compile(regexBirthday);
    	matcher = pattern.matcher(actContent);
    	String birthday = "";
    	if(matcher.find()){
    		birthday = matcher.group(1);
    		birthday = birthday.replaceAll("<.*?>", "");
    	}
    	String regexRepresent = "<dt class=\"basicInfo-item name\">代表作品</dt><dd class=\"basicInfo-item value\">(.*?)</dd>";
    	pattern = Pattern.compile(regexRepresent);
    	matcher = pattern.matcher(actContent);
    	String represent = "";
    	if(matcher.find()){
    		represent = matcher.group(1);
    	}
    	represent = represent.replaceAll("<.*?>", "");
    	String[] tokens = represent.split("、"); 
    	List<String> representList = new ArrayList<String>();
    	for(String rp : tokens){
    		representList.add(rp);
    	}
    	//第一种识别图片方法
    	String regexImg = "data-src=\"http(.*?)jpg\"";
    	pattern = Pattern.compile(regexImg);
    	matcher = pattern.matcher(actContent);
    	String imgurl = "";
    	if(matcher.find()){
    		imgurl = matcher.group(1);
    		imgurl = "http" + imgurl +"jpg";
    		try{
    			imgurl = store.downloadImage(imgurl, aid);
    		}catch(Exception e){
    			
    			System.out.println("download error!");
    		}
    	}
    	//第二种识别图片的方法
    	if(imgurl.equals("")){
	    	regexImg = "<div class=\"summary-pic\">.*?<img src=\"(.*?)\" /><button class=\"picAlbumBtn\"><em></em><span>图集</span></button>";
	    	pattern = Pattern.compile(regexImg);
	    	matcher = pattern.matcher(actContent);
	    	imgurl = "";
	    	if(matcher.find()){
	    		imgurl = matcher.group(1);
	    		try{
	    			imgurl = store.downloadImage(imgurl, aid);
	    		}catch(Exception e){
	    			System.out.println("download error!");
	    		}
	    	}
    	}
    	String likeUrl = "http://service.channel.mtime.com/Search.api?Ajax_CallBack=true&Ajax_CallBackType=Mtime.Channel.Services&Ajax_CallBackMethod=GetSearchResult&Ajax_CrossDomain=1&Ajax_RequestUrl=http://search.mtime.com/search/?q=" +aid + "&Ajax_CallBackArgument0=" + aid + "&Ajax_CallBackArgument1=0&Ajax_CallBackArgument2=290&Ajax_CallBackArgument3=0&Ajax_CallBackArgument4=1";
    	String likeContent = getContent(likeUrl, "utf-8");
    	String like = "";
    	pattern = Pattern.compile("\"love\":(.*?),\"personTitle\"");
    	matcher = pattern.matcher(likeContent);
    	if(matcher.find()){
    		like = matcher.group(1);
    	}
    	String regexCountry = "国.*?籍</dt><dd class=\"basicInfo-item value\">(.*?)</dd>";
    	pattern = Pattern.compile(regexCountry);
    	matcher = pattern.matcher(actContent);
    	String country= "";
    	if(matcher.find()){
    		country = matcher.group(1);
    		country = country.replaceAll("<.*?>", "");
    	}
    	try {
			imgurl = this.store.downloadImage(imgurl, actorname);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("download error");
		}
    	Actor actor = new Actor(actorname, birthday, imgurl, country, like,  representList);
    	return actor;
    }
    
}

	
class Introduction{

	String score; 			//豆瓣评分
	String boxOffice;			//票房成绩
	String release_time;		//上映时间
	String area;						//国家地区
	String length;					//片长
	List<String> keywords;//关键词列表
	String imgUrl;					//图片链接
	
	public Introduction(String score, String boxOffice, String release_time, String area, String length, List<String> keywords, String imgUrl){
		this.score = score;
		this.boxOffice = boxOffice;
		this.release_time = release_time;
		this.area = area;
		this.length = length;
		this.keywords = keywords;
		this.imgUrl = imgUrl;
	}
	
}

class Information{
	String story = null; //剧情解析
	String comment = null;  //影片评论
	String doubanUrl = null; //豆瓣地址
	List<String> prize = null; //获得奖项
	
	public Information(String  story, String comment, String doubanUrl, List<String> prize ){
		this.story = story;
		this.comment = comment;
		this.doubanUrl = doubanUrl;
		this.prize = prize;
	}
}



 class MainTheme{
	 	String style; 	// 风格
	 	String  theme; 	//主题
	 	List<String> similarMovie;	//相关电影
	 	public MainTheme(String style, String theme, List<String> similarMovie){
	 		this.style = style;
	 		this.theme = theme;
	 		this.similarMovie  = similarMovie;
	 	}
 }
 
class Manufacture{
	
	List<Company> companyList ;
	List<Director> directors;
	List<ScriptWriter> scriptwriters;
	
	public Manufacture(List<Company> companyList, List<Director> directors, List<ScriptWriter> scriptwriters){
		this.companyList = companyList;
		this.directors = directors;
		this.scriptwriters = scriptwriters;
	}
}

class Company{
	String cname = null; //公司名字
	List<String> futureRepresent = null; //未来作品
	List<String> pastRepresent = null;//过去作品
	String representContent = null;
	public Company(String cname, List<String> futureRepresent, List<String> pastRepresent, String representContent){
		this.cname = cname;
		this.futureRepresent = futureRepresent;
		this.pastRepresent = pastRepresent;
		this.representContent = representContent;
	}
}
	
class  Director{
	String dname= null;//导演名称
	List<String> represent = null;//代表作品
	String rate = null; //导演评分
	String imgurl = null;//图片地址
	public Director(String dname,  List<String> represent , String rate, String imgurl){
		this.dname = dname;
		this.represent = represent;
		this.imgurl = imgurl;
		this.rate = rate;
	}
}
	
class ScriptWriter{
	String sname = null;//编剧名称
	List<String> represent = null;//代表作品
	public ScriptWriter(String sname,  List<String> represent){
		this.sname = sname;
		this.represent = represent;
	}
	
}

class People{
	String id; 		//mtime网上的id号
	List<String> represent ;			//代表作品
	String rate;			//评分
	String imgurl;   //照片
	public People(String id , List<String> represent, String rate,   String imgurl){
		this.id = id ;
		this.represent = represent;
		this.rate = rate;
		this.imgurl = imgurl;
	}
}

 class Role{
	 String name; //角色名称
	 String introduction; //角色介绍
	 String imgurl; //角色图片
	 Actor actor; //演员
	 
	 public Role(String name, String introduction, Actor actor, String imgurl){
		 this.name = name;
		 this.introduction = introduction.replaceAll("<.*?>", "");
		 this.actor  = actor;
		 this.imgurl = imgurl;
		 
	 }
	 
 }
 
 class Actor{
	 String name;  //演员名字
	 String birthday;  //演员出生日期
	 String imgurl;  //图片
	 String country; //国籍
	 List<String> represent; //代表作品
	 String like; //喜爱程度
	 public Actor(String name,String birthday, String imgurl , String country,  String like, List<String> represent){
		 this.name = name;
		 this.birthday = birthday;
		 this.imgurl = imgurl;
		 this.country = country;
		 this.like = like;
		 this.represent = represent;
	 }
 }

