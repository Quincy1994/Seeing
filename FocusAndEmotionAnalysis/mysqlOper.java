package EmotionAnalysis.myAnalysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;
import EmotionAnalysis.myAnalysis.CommentStatistic.AvgCount;
import EmotionAnalysis.myAnalysis.EmotionAnalysis.score;

public class mysqlOper {

	/**
	 * 向mysql中插入、更新数据（日平均分，日评论量，日情感分）
	 * 构造函数参数是文件txt地址，用来初始化commentStatistic和emotionAnalysis类，得到对应的数据
	 * @param args
	 */
	JavaSparkContext sc;
	CommentStatistic comStat ;//日统计量
	String movieName;//该部电影的名字
	EmotionAnalysis emAna ;//日情感

	public mysqlOper(String path){
		//通过截取路径得到电影名
		this.movieName = getMovieName(path);
		this.comStat= new CommentStatistic(path);
		this.emAna= new EmotionAnalysis(path);
		//this.movieName = "ShenQiDongWuZaiNaLi";
		
		//获得关于一部电影的所有统计量
		List<Tuple2<String, AvgCount>> avgScoreList = this.comStat.avgScoreList;
		List<Tuple2<String, Integer>> comNumList = this.comStat.comNumList;
		List<Tuple2<String, score>> emotionList = this.emAna.result;
		/* *  
		for(Tuple2<String, AvgCount> tup : avgScoreList)
			System.out.println(tup._1 + "    avgScore: " + tup._2.avg());
		System.out.println("\n");
		for(Tuple2<String, Integer> tup : comNumList)
			System.out.println(tup._1 + "    number: " + tup._2);
		System.out.println("\n");
		for(Tuple2<String, score> tup : emotionList)
			System.out.println(tup._1 + "    positive: " + tup._2.positive + "   negative: " + tup._2.negative);
		* */
		//把统计量转化为sql语句
		List<String> avgScoreSql =  createAvgScoreSqls(avgScoreList);
		List<String> comNumSql = createComNumSqls(comNumList);
		List<String> emotionSql = createEmotionSqls(emotionList);
		
		//执行sql语句，完善数据库
		executeSqls(avgScoreSql);
		executeSqls(comNumSql);
		executeSqls(emotionSql);
	}
	
	private String getMovieName(String path) {
		//截取路径得到电影名称
		String[] array = path.split("/");
		String name = array[array.length - 1].replace(".txt", "");
		return name;
	}

	  public  List<String> createAvgScoreSqls(List<Tuple2<String, AvgCount>> result){
		  //创建向数据库插入日均分的sql语句
		  List<String> sqls = new ArrayList<String>();
		  for(Tuple2<String, AvgCount> entry: result){
			  String s = "insert into daysComment(movie_name, daytime, score, commentNum, goodScore, badScore) values(\"" + 
		  this.movieName + "\", \"" + entry._1() + "\"," + String.valueOf(entry._2().avg()) + ",0,0,0);";
			  sqls.add(s);
		  }
		  return sqls;
	  }
	
	  public static List<String> createComNumSqls (List<Tuple2<String, Integer>>  comm  ){
		//创建向数据库插入日评论量的sql语句
		  List<String> sqls = new ArrayList<String> ();
		  for(Tuple2<String, Integer> tup: comm){
			  String s = "update daysComment set commentNum = " + tup._2() + " where daytime = \"" + tup._1() + "\";";
			  sqls.add(s);
		  }
		  return sqls;
	  }
	  
	    public static List<String> createEmotionSqls(List<Tuple2<String, score>> result){
	    	//创建向数据库插入日情感分的sql语句
	    	List<String> sqls = new ArrayList<String>();
	    	double posi = 0;
	    	double negat = 0;
	    	for(Tuple2<String, score> tup: result){
	    		String p = "update daysComment set goodScore =" + tup._2().positive +" where daytime=\"" + tup._1() + "\";";
	    		String n = "update daysComment set badScore =" + tup._2().negative +" where daytime=\"" + tup._1() + "\";";
	    		// "\",badScore =\"" + tup._2().negative + \"\"
	    		sqls.add(p);
	    		sqls.add(n);
	    	}
			return sqls;
	    }
	
		public static  void executeSqls(List<String> sqls){
			//执行sql语句
			   String driver = "com.mysql.jdbc.Driver";
				String url = "jdbc:mysql://192.168.235.20:3306/seeing?characterEncoding=utf8";
				String user = "root";
				String password = "iiip";
			try{
				//加载驱动程序
				Class.forName(driver);
				//连接数据库
				Connection conn = DriverManager.getConnection(url, user, password);
				if( !conn.isClosed()){
					System.out.println("Succeed !");
					//创建statement类对象，用来执行sql语句
					Statement statement = conn.createStatement();
					for(String sql: sqls){
						 statement.execute(sql);
					}									
					conn.close();
				}
			}catch(Exception e){
				e.printStackTrace();
				
			}
		}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		mysqlOper dbOp = new mysqlOper("/home/yingying/下载/挑战杯/movieIdFile/神奇动物在哪里.txt");
	}

}
