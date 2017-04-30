package EmotionAnalysis.myAnalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class SplitMovieFile {

	/**
	 * 把数据分出来，以电影id为文件名
	 * @param args
	 */
	static JavaSparkContext sc = null;
	static JavaRDD<String> inputRDD = null;
	
	public SplitMovieFile(){
		this.sc = new JavaSparkContext(new SparkConf().setMaster("local").setAppName("count"));
		this.inputRDD = sc.textFile("/home/yingying/下载/厦门大数据/bigdata.csv");			
		classifyMovie();
	}

	public void classifyMovie(){
		JavaRDD<String[]>  splitRDD = inputRDD.map(new splitRecord()).filter(new dataClean());//把每一行记录都切分成四部分
		JavaPairRDD<String, Iterable<String>> pairRDD = splitRDD.mapToPair(new createPair()).groupByKey();//以id为键，其他三部分（分，时，评）为值
	    List<Tuple2<String, Iterable<String>>> pairCollection = pairRDD.collect();//collect()出来，每一条二元组是一部电影的全部信息
	    for(Tuple2<String, Iterable<String>> tuple: pairCollection){//写入文件
	    	fileWrite(tuple);
	    }
	}

	static class splitRecord implements Function<String, String[]>{
		//把每一行记录切分
		public String[] call(String s){
			return s.split(",");
		}
	}
	static class dataClean implements Function<String[], Boolean>{
		//数据清洗，保留取切分后长为4且id正常者
		public Boolean call(String[] array){
			if(array.length == 4 && array[0].length() == 24 && array[0].startsWith("5"))
				return true;
			else return false;
		}
	}
	static class createPair implements PairFunction<String[], String, String>{
		//创建键值对：id: score, time, comment
		public Tuple2<String, String> call(String[] s){
			return new Tuple2<String, String>(s[0], s[1] +"," +  s[2] + "," +  s[3]);
		}
	}
	

		public static void fileWrite (Tuple2<String, Iterable<String>>  tup){
			//写入文件
			try{
				File file = new File("/home/yingying/idFile/" + tup._1() + ".txt");
				if(!file.exists()){
					file.createNewFile();
				}
				FileWriter fileWritter = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fileWritter);
				for(String s: tup._2()){
					//System.out.println(s);
					bw.write(s + "\n");
				}
				bw.close();			
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	
	public static void main(String[] args) {
		SplitMovieFile test = new SplitMovieFile();
	}

}