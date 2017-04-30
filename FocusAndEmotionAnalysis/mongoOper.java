package EmotionAnalysis.myAnalysis;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.hankcs.hanlp.corpus.document.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class mongoOper {

	/**
	 * 把一部电影的高中低三类评分关注点存入mongodb
	 * 需要调用FouceAnalysis类，构造该类时放入电影文档地址
	 * @param args
	 */
	String[] fouces;//设置关注点序列，注意，一定要保证与FouceAnalysis类一样的顺序
	FouceAnalysis fouceAn; //要先把原文件.txt通过FouceAnalysis的分析
	String name;
	double[] numList;
	
	public mongoOper(String path) throws IOException{
		this.name = getName(path);
		//this.name = "XingQiuDaZhan";
		System.out.println("name : " + name);
		this.fouces = new String[]{ "制作", "出品公司", "选景", "导演","编剧","主题", "风格", "题材内容" ,
				"剧情", "开头", "发展", "结局", "笑点", "泪点", "视听", "动作", "画面","镜头", "音乐", "角色", "男主角", "女主角", "反派", "配角"};
		this.fouceAn = new FouceAnalysis(path);
		this.numList = this.fouceAn.totalComList;
		for(int i = 0; i < this.numList.length; i ++)
			System.out.println(this.fouces[i] + "   " + this.numList[i]);
		
		Mongo connection = new Mongo("192.168.235.20", 27017);
		DB db = connection.getDB("totalFouceAnalysis");
		boolean ok = db.authenticate("root", "iiip".toCharArray());
		if(ok){
			System.out.println("db connection success !");
			DBCollection collection = db.getCollection("movieFouces");
			BasicDBObject document = new BasicDBObject();
			document.put("_id", this.name);
			for(int i = 0; i < this.numList.length; i ++){
				document.put(this.fouces[i], this.numList[i]);
			}
			collection.insert(document);
			System.out.println("document has been inserted ");
		}else{
			System.out.println("db connection fail");
		}
//	InsertData(this.numList);
//		List<double[]> arrayList = new ArrayList<double[]>();
//		arrayList.add(numList);
//		FullMongo(this.name, arrayList);
	}

	private String getName(String path) {
		//通过截取路径得到电影名字
		String[] array = path.split("/");	
		return array[array.length - 1].replace(".txt", "");
	}
	public void InsertData(double[] totalNumArray) throws UnknownHostException, MongoException{
		Mongo connection = new Mongo("192.168.235.20", 27017);
		DB db = connection.getDB("totalFouceAnalysis");
		boolean ok = db.authenticate("root", "iiip".toCharArray());
		if(ok){
			System.out.println("db connection success !");
			DBCollection collection = db.getCollection("movieFouces");
			BasicDBObject document = new BasicDBObject();
			document.put("_id", this.name);
			for(int i = 0; i < totalNumArray.length; i ++){
				document.put(this.fouces[i], totalNumArray[i]);
			}
			collection.insert(document);
			System.out.println("document has been inserted ");
		}else{
			System.out.println("db connection fail");
		}
	}
	
	public  void FullMongo(String name, List<double[]> numArray){
		//输入高中低三条关注点列表，插入数据库
		try{
			Mongo connection = new Mongo("192.168.235.20", 27017);
			DB db = connection.getDB("totalFocusAnaly");
			boolean ok = db.authenticate("root", "iiip".toCharArray());
			if(ok){
				System.out.println("three level numbers, db connection success ");
				DBCollection collection = db.getCollection(name);
				System.out.println(name  + "   collection has been created");
				//如果numArray的长度为三，说明是有高中低三类分析数据。否则长度为一，是总体的分析。
				if(numArray.size() == 3){
					String[] types = new String[]{"high", "middle", "low"};
					for(int i = 0; i < 3; i ++){
						double[] oneArray = numArray.get(i);
						BasicDBObject document = new BasicDBObject();
						document.put("_id", types[i]);
						for(int j = 0; j < oneArray.length; j ++){
							//if(oneArray[j] != 0.0)零值也要插入
								document.put(fouces[j], oneArray[j]);
						}
						//如果是要替换原文件，用save
						//collection.save(document);
						collection.insert(document);
						System.out.println(types[i] + "   doucment insert success");
					}
				}else if(numArray.size() == 1){
					BasicDBObject docum = new BasicDBObject();
					docum.put("_id", name);
					double[] total = numArray.get(0);
					for(int i = 0; i < total.length; i ++){
						docum.append(fouces[i], total[i]);
					}
					collection.insert(docum);
					System.out.println(name + "   doucment insert success");
				}
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		mongoOper test = new mongoOper("/home/yingying/sqlIdFile/chineseName/太空旅客.txt");
		//test.InsertData(totalNumArray)
	}

}
