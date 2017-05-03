import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


public class Store {
	
	String movieDir = "";
	String imgDir = "";
	String movieFile = "";

	public Store(String movie) {
		// TODO Auto-generated constructor stub
		this.movieDir = movie;
		this.imgDir = movie + "/image";
		makeDir(this.movieDir);  			//创建电影文件夹
		makeDir(this.imgDir);					//创建电影对应的图片文件夹
	}



	public static boolean makeDir(String folderName){
		File folder = new File(folderName);
		return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
	}
	
	public static boolean deleteDir(String folderName){
		File folder = new File(folderName);
		deleteFile(folder);
		return true;
	}
	
	public static   void deleteFile(File file) {  
	    if (file.exists()) {//判断文件是否存在  
	    	if (file.isFile()) {//判断是否是文件  
	    		file.delete();//删除文件   
	    	} else if (file.isDirectory()) {//否则如果它是一个目录  
	    		File[] files = file.listFiles();//声明目录下所有的文件 files[];  
	    		for (int i = 0;i < files.length;i ++) {//遍历目录下所有的文件  
	    			deleteFile(files[i]);//把每个文件用这个方法进行迭代  
	    		}  
	    		file.delete();//删除文件夹  
	    	}  
	    	} else {  
	    		System.out.println("所删除的文件不存在");  
	    	}  
	   }  
	
	public  String downloadImage(String imgUrl, String imageName) throws IOException{
		String imagePath = this.imgDir + "/" +imageName + ".jpg";
		File file = new File(imagePath);
		FileOutputStream out =  null;
		InputStream  in = null;
		try{
			URL url = new URL(imgUrl);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0");
			connection.connect();
			in = connection.getInputStream();
			out = new FileOutputStream(file);
			int c;
			while((c = in.read()) != -1){
				out.write(c);
			}
		}finally{
			if(in != null){
				in.close();
			}
			if(out != null){
				out.close();
			}
		}
		return imagePath;
	}
	
	public static  void storeMovie(SpiderBaike sb){
		PrintWriter pw = null;
		String movieFile = sb.movie +"/" + sb.movie +".txt";
		if(!check(sb)){
			System.out.println("error! store fail");
			deleteDir(sb.movie);
			return;
		}
		try{
				pw = new PrintWriter(new FileWriter(movieFile));
				pw.println("电影:" + sb.movie);
				pw.println("--------------------------------------------------------------------");
				pw.println("基本介绍: ");
				pw.println("电影评分: "+ sb.introduction.score);
				pw.println("电影票房: " + sb.introduction.boxOffice);
				pw.println("上映时间: " + sb.introduction.release_time);
				pw.println("国家地区: " + sb.introduction.area);
				pw.println("片长: " + sb.introduction.length);
				pw.println("图片: " + sb.introduction.imgUrl);
				List<String> keywords = sb.introduction.keywords;
				pw.println("关键词列表: ");
				for(String keyword: keywords){
					pw.print(keyword + " ");
				}
				pw.println();
				pw.println("--------------------------------------------------------------------");
				pw.println("内容:");
				pw.println("剧情解析:"+ sb.information.story);
				pw.println("豆瓣评论url:"+ sb.information.doubanUrl);
				pw.println("影评评价:"+ sb.information.comment);
				pw.println("获奖情况: ");
				List<String> prizes = sb.information.prize;
				for(String one: prizes){
					pw.println(one);
				}
				pw.println();
				pw.println("--------------------------------------------------------------------");
				pw.println("主题:");
				pw.println("风格:" + sb.mainTheme.style);
				pw.println("题材内容:" + sb.mainTheme.theme);
				pw.println("相关电影:");
				for(String smovie : sb.mainTheme.similarMovie){
					pw.print(smovie + " ");
				}
				pw.println();
				pw.println("--------------------------------------------------------------------");
				pw.println("制作:");
				pw.println("出品公司");
				for(Company company: sb.manufacture.companyList){
					pw.println("出品公司名称:" + company.cname);
					pw.print("未来作品:");
					for(String  represent : company.futureRepresent){
						pw.print(represent + " ");
					}
					pw.println();
					pw.print("过去作品:");
					for(String represent: company.pastRepresent){
						pw.print(represent + " ");
					}
					pw.println();
				}
				pw.println();
				pw.println("导演:");
				for(Director director: sb.manufacture.directors){
					pw.println("导演名称:" + director.dname);
					pw.println("导演图片:" + director.imgurl);
					pw.println("评分:" + director.rate);
					pw.print("导演代表作品:");
					for(String rp : director.represent){
						pw.print(rp + " ");
					}
					pw.println();
				}
				pw.println();
				pw.println("编剧:");
				for(ScriptWriter scriptwriter: sb.manufacture.scriptwriters){
					pw.println("编剧名称:" + scriptwriter.sname);
					pw.print("编剧代表作品:");
					for(String rp : scriptwriter.represent){
						pw.print(rp + " ");
					}
					pw.println();
				}
			
				pw.println();
				pw.println("--------------------------------------------------------------------");
				pw.println("角色:");
				for(Role role : sb.roleList){
					pw.println("角色名:" + role.name);
					pw.println("角色图片:" + role.imgurl);
					pw.println("角色介绍:" + role.introduction);
					pw.println("对应的演员:" + role.actor.name);
					pw.println("演员出生日期:" + role.actor.birthday);
					pw.println("演员国籍:" + role.actor.country);
					pw.println("演员图片:" + role.actor.imgurl);
					pw.println("演员的受欢迎程度:" + role.actor.like);
					pw.print("演员代表作品:");
					for(String rp: role.actor.represent){
						pw.print(rp + " ");
					}
					pw.println();
				}
					pw.println();
					pw.close();
					System.out.println("Store ok!");
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	public static boolean check(SpiderBaike sb){
		if(sb.introduction.score.equals("")){
			return false;
		}
		if(sb.introduction.boxOffice.equals("")){
			return false;
		}
		if(sb.introduction.release_time.equals("")){
			return false;
		}
		if(sb.introduction.area.equals("")){
			return false;
		}
		if(sb.introduction.length.equals("")){
			return false;
		}
		if(sb.introduction.keywords.isEmpty()){
			return false;
		}
		if(sb.information.story.isEmpty()){
			return false;
		}
		if(sb.information.comment.isEmpty()){
			return false;
		}
		if(sb.manufacture.companyList.isEmpty()){
			return false;
		}
		if(sb.manufacture.directors.isEmpty()){
			return false;
		}
		if(sb.manufacture.scriptwriters.isEmpty()){
			return false;
		}
		if(sb.roleList.isEmpty()){
			return false;
		}
		for(Role role : sb.roleList){
			if(role.actor == null){
				return false;
			}
		}
		return true;
	}
}

