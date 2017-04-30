package EmotionAnalysis.myAnalysis;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

public class CommentStatistic {

	/**
	 * 统计量计算：日平均分，日评论量
	 * 构造参数：文件地址
	 * 修改：豆瓣评分１０～５０，注释６１行的过滤。
	 * @param args
	 */
	static JavaSparkContext sc = null;
	static JavaRDD<String> inputRDD = null;
	static JavaRDD<String[]> splitRDD = null;
	//static String movieName = null;
	List<Tuple2<String, AvgCount>> avgScoreList = null;
	List<Tuple2<String, Integer>> comNumList = null;
	
	public CommentStatistic(String txtPath){
		this.sc = new JavaSparkContext(new SparkConf().setMaster("local").setAppName("count"));
		//JavaStreamingContext activitySummaryScheduler = new JavaStreamingContext(sc, Durations.seconds(1000));
		this.inputRDD = sc.textFile(txtPath);		
		this.splitRDD = inputRDD.map(new splitRecord()).filter(new dataClean());
		//this.movieName = "测试电影名";
		this.avgScoreList = avgScore();
		this.comNumList = avgCom();
		this.sc.stop();//关闭sc, 否则进行数据库操作时会有多个sc开启的问题
	}
	
	  public  List<Tuple2<String, AvgCount>>  avgScore(){
		//日平均分：时间：平均分	
		JavaPairRDD<String, String> timeScoreRDD = splitRDD.mapToPair(new createPair(0));// time: score
		//JavaPairRDD<String, AvgCount> timeScoreIteratorRDD = timeScoreRDD.filter( new filterEmptyScore()).combineByKey(createAcc, addAndCount, combine);
		JavaPairRDD<String, AvgCount> timeScoreIteratorRDD = timeScoreRDD.combineByKey(createAcc, addAndCount, combine);
		List<Tuple2<String, AvgCount>> result = timeScoreIteratorRDD.collect();
		//并把结果写入文件
		//writeAvgScore(countMap);
		return result;
		}
	  

	  public static void writeAvgScore(Map<String, AvgCount> countMap){
		  //把日均分写入文件
		  	try{
		  			File file = new File("/home/yingying/SparkStatisticData/dailyAveScore.txt");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
		    for(Entry<String, AvgCount> entry: countMap.entrySet()){
		    	bw.write(entry.getKey() + " today's average score is : " + String.valueOf(entry.getValue().avg()));
		    	bw.write("\n");
		    }			
		    bw.close();
		    System.out.println("dailyAvgScore has been written");
		}catch(IOException e){
			e.printStackTrace();
		}
	  }
	  public static List<Tuple2<String, Integer>> avgCom (){
		  //日评论量：时间：评论量	
		JavaPairRDD<String, String> timeCommentRDD = splitRDD.mapToPair(new createPair(2));// time: comment
		JavaPairRDD<String, Iterable<String>> timeComIterator = timeCommentRDD.filter(new filterEmptyComment()).groupByKey();
		JavaPairRDD<String, Integer> timeComNum = timeComIterator.mapValues(countDailyComs);
		List<Tuple2<String, Integer>>  comm = timeComNum.collect();			
		//并把结果写入文件
		//writeAvgCom(commMap);
		return comm;
	  }
	  
	 static  Function<Iterable<String>, Integer> countDailyComs = new Function<Iterable<String>, Integer>(){
		  //计算一天的评论量，从iterable<string>到integer
			public Integer call(Iterable<String> strs){
				int totalNum = 0;
				for(String s: strs){
					totalNum += 1;
				}
				return totalNum;
			}				
		};
	  public static void writeAvgCom(Map<String, Integer> commMap){
			try{
				File file = new File("/home/yingying/SparkStatisticData/dailyComNum.txt");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			for(Entry<String, Integer> entry2: commMap.entrySet()){
				bw.write(entry2.getKey() + "  today's total number of comments is : " + entry2.getValue());
				bw.write("\n");
			}		
			bw.close();
			 System.out.println("dailyComNum  has been written");
		}catch(IOException e){
			e.printStackTrace();
		}	  
	  }

	  static class filterEmptyComment implements Function<Tuple2<String, String>, Boolean>{
		  //清除掉空的comment
		 public Boolean call(Tuple2<String, String> tup){
			 if(tup._2.equals(""))
				 return false;
			 else return true;
		 }
	 };
	  
	public static class createPair implements PairFunction<String[], String, String>{
		//创建键值对，取下标时间为键，下标 lastIndex为值
		//int firstIndex;
		int lastIndex ;
		createPair( int second){
			//this.firstIndex = first;
			this.lastIndex = second;
		}
		public Tuple2<String, String> call(String[] s){
			//System.out.println(s[1].substring(0, 10) + "   score: " + s[this.lastIndex]);
			return new Tuple2<String, String>(s[1].substring(0, 10), s[this.lastIndex]);
		}
	}
	
	public static class splitRecord implements Function<String, String[]>{
		//把一条记录切分
		public String[] call(String s){
			String[] result = s.split(",");
			return result;
		}
	}
	public static class dataClean implements Function<String[], Boolean>{
		//数据清洗，切分后数组长度为3
		public Boolean call(String[] s){
			if(s.length == 3)
				return true;
			else return false;
		}
	}

	  static class filterEmptyScore implements Function<Tuple2<String, String>, Boolean>{
		  //主要对分数进行判断：清除掉空的分数和非数字
		 public Boolean call(Tuple2<String, String> tup){
			   Pattern pattern = Pattern.compile("[0-9]*"); 
			   Matcher isNum = pattern.matcher(tup._2);
			 if(tup._2.equals("") || !isNum.matches())
				 return false;
			 else return true;
		 }
	 };
	  
		 static class AvgCount implements Serializable{
			double total_;
			double num_;
			AvgCount(double total, double num){
				this.total_ = total;
				this.num_ = num;
			}
			float avg(){
				return (float) (this.total_ / this.num_);
			}
			void printAvgCount(){
				System.out.println(this.num_ + "  " + this.total_);
			}
		 };
		
		static Function<String, AvgCount> createAcc = new Function<String, AvgCount>(){
			 public AvgCount call(String x){    
				return new AvgCount(Double.parseDouble(x), 1);
			}
		};
		
		static Function2<AvgCount, String, AvgCount> addAndCount = 
				new Function2<AvgCount, String, AvgCount>(){
			 public AvgCount call(AvgCount a, String x){
				a.total_ += Double.parseDouble(x);
				a.num_ += 1;
				return a;
			}
		};
		
		static Function2<AvgCount, AvgCount, AvgCount> combine = 
				new Function2<AvgCount, AvgCount, AvgCount>(){
			 public AvgCount call(AvgCount a, AvgCount b){
				a.total_ += b.total_;
				a.num_ += b.num_;
				return a;
			}
		};
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CommentStatistic test = new CommentStatistic("/home/yingying/sqlIdFile/3434070.txt");

	}
}


