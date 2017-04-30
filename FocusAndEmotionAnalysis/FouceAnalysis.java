package EmotionAnalysis.myAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;



public class FouceAnalysis {

	/**
	 * 关注点分析
	 * 读取的是一部电影的所有记录（评分，时间，评论）
	 * 修改：针对豆瓣诸如10.0的评分数据，修改243行的匹配规则，及256行的提取方法，修改middle和high
	 * @param args
	 */

	 JavaSparkContext sc;
	 JavaRDD<String> inputRDD;
	//List<double[]> resultList;
	static final int high = 40;
	static final int middle = 20;
	public double[] totalComList;
	static String[] fouce;
	
	public FouceAnalysis(String path) throws IOException{
		SparkConf conf = new SparkConf().setMaster("local").setAppName("FouceAnalysis");
		this.sc = new JavaSparkContext(conf);
		this.inputRDD = sc.textFile(path);
		this.totalComList = totalComAnalysis();
		
//		this.fouce = new String[]{ "制作", "出品公司", "选景", "导演","编剧","主题", "风格", "题材内容" ,
//				"剧情", "开头", "发展", "结局", "笑点", "泪点", "视听", "动作", "画面","镜头", "音乐", "角色", "男主角", "女主角", "反派", "配角"};
//		for(int i = 0; i < this.fouce.length; i ++){
//			System.out.println(this.fouce[i] + "  " + this.totalComList[i]);
//		}
//		
		this.sc.close();
	}
	
	public static HashMap setHashMap(Boolean wordIndex){
		//设置哈希树，是关注点与下标的对应			 
		//若wordIndex  = true，创建【关注点：下标】映射
		String[] fouces = new String[]{ "制作", "出品公司", "选景", "导演","编剧","主题", "风格", "题材内容" ,
				"剧情", "开头", "发展", "结局", "笑点", "泪点", "视听", "动作", "画面","镜头", "音乐", "角色", "男主角", "女主角", "反派", "配角"};
		if(wordIndex){
			HashMap<String, Integer> WordIndex = new HashMap<String, Integer>();
		   for(int i = 0; i < fouces.length; i ++){
			   WordIndex.put(fouces[i], i); 
		   }
		   return WordIndex;			
		   //否则，创建【下标：关注点】映射
		}else{
			HashMap<Integer, String> IndexWord = new HashMap<Integer, String>();
			   for(int i = 0; i < fouces.length; i ++){
				   IndexWord.put(i, fouces[i]); 
			   }
			   return IndexWord;					
		}
	}
	public double[] totalComAnalysis() throws IOException{
		/*
		 * 计算一部电影所有评论的关注点
		 * */		
		JavaRDD<String[]> splitRDD = inputRDD.map(new splitRecord()).filter(new filterEmpty());
		//System.out.println("splitRDD " + splitRDD.count());
		JavaPairRDD<Integer, String> pairRDD = splitRDD.mapToPair(new createPair());
		//System.out.println("pairRDD<Integer, String> " + pairRDD.count());
    
		JavaPairRDD<String, Integer[]> totalComScoreList = pairRDD.values().mapToPair(new stringArrayPair());
		JavaRDD<Tuple2<String, Integer[]>> tResult = totalComScoreList.map(new findFouces());
		double[] tFinalCalculate = finalCalculate(tResult);

		return tFinalCalculate;
	}
	public static List<double[]> threeLevelFoucs(JavaPairRDD<Integer, String> pairRDD) throws IOException{
		/*
		 * 对一部电影的评论分高中低三类分析关注点
		 * 把所有评论分为高\中\低三个RDD:  highPointPairRDD  middlePointPairRDD  lowPointPairRDD
		 * 并计算每条评论的２４个关注点得分，得到三个RDD:　hResult mResult lResult 
		 *  通过finalCalculate(RDD)计算得到每一类最后的得分数组
		 * */
		//分类，得到三个ＲＤＤ
		JavaPairRDD<Integer, String> highPointPairRDD = pairRDD.filter(new filterHighPoint());
		JavaPairRDD<Integer, String> middlePointPairRDD = pairRDD.filter(new filterMiddlePoint());
		JavaPairRDD<Integer, String> lowPointPairRDD = pairRDD.filter(new filterLowPoint());
	   //System.out.println("highPontPairRDD  " + highPointPairRDD.count());
	   //System.out.println("middlePontPairRDD  " + middlePointPairRDD.count());
	    //System.out.println("lowPontPairRDD  " + lowPointPairRDD.count());
		JavaPairRDD<String, Integer[]> hComScoreList = highPointPairRDD.values().mapToPair(new stringArrayPair());
		JavaRDD<Tuple2<String, Integer[]>> hResult = hComScoreList.map(new findFouces());
		
		JavaPairRDD<String, Integer[]> mComScoreList = middlePointPairRDD.values().mapToPair(new stringArrayPair());
		JavaRDD<Tuple2<String, Integer[]>> mResult = mComScoreList.map(new findFouces());
		
		JavaPairRDD<String, Integer[]> lComScoreList = lowPointPairRDD.values().mapToPair(new stringArrayPair());
		JavaRDD<Tuple2<String, Integer[]>> lResult = lComScoreList.map(new findFouces());

//		for(Tuple2<String, Integer[]> tu: m){
//			for(Integer i : tu._2){
//				System.out.print(i + "  " );
//			}			
//			System.out.println("\n");
//			System.out.println(tu._1);
//		}	
		List<double[]> resultList = new ArrayList<double[]>();
		double[] hFinalCalculate = finalCalculate(hResult);
		double[] mFinalCalculate = finalCalculate(mResult);
		double[] lFinalCalculate = finalCalculate(lResult);
		resultList.add(hFinalCalculate);
		resultList.add(mFinalCalculate);
		resultList.add(mFinalCalculate);
		//存成键：值形式的文件
		//jsFile json = new jsFile();		
		//json.readAsJson(hFinalCalculate);
		//printResult(hFinalCalculate ,mFinalCalculate , lFinalCalculate );
		return resultList;
	}
	
	public double[] percentage(int[] numbers){
		double[] pres = new double[numbers.length];
		double total = 0.00;
		for(int i : numbers){
			total += i;
		}
		for(int i = 0; i < numbers.length; i ++){
			pres[i] = numbers[i]  / total ;
		}
		return pres;
	}
	
	public static void printResult(double[] hFinalCalculate, double[] mFinalCalculate, double[] lFinalCalculate){
		//输出打印结果：打印三种情况下的关注点
		HashMap<String, Integer> fouceIndexMap = setHashMap(false);
		System.out.println("--------------------------------highScore movies' fouce: -----------------------------------");
		for(int i = 0; i < hFinalCalculate.length; i ++)
			System.out.println(fouceIndexMap.get(i) + "  :   " +  hFinalCalculate[i] );
		System.out.println("--------------------------------middle Score movies' fouce: -----------------------------------");
		for(int i = 0; i < mFinalCalculate.length; i ++)
			System.out.println(fouceIndexMap.get(i) + "  :   " +  mFinalCalculate[i] );
		System.out.println("--------------------------------low Score movies' fouce: -----------------------------------");
		for(int i = 0; i <lFinalCalculate.length; i ++)
			System.out.println(fouceIndexMap.get(i) + "  :   " +  lFinalCalculate[i] );
	}
	
	public static void readResult(int[] high, int[] middle, int[] low){
		//把结果写入文件，写入的是三种情况  					下标：关注点
		HashMap<String, Integer> fouceIndexMap = setHashMap(false);
		try{
			File file = new File("/home/yingying/SparkStatisticData/fouceAnalysis.txt");
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.write("high score movie");
			bw.write("\n");
			for(int i = 0;i < high.length; i++){
				bw.write(fouceIndexMap.get(i)  +  "    "  + String.valueOf(high[i]));
			    bw.write("\n");				
			}
			bw.write("middle score movie");
			bw.write("\n");
			for(int i = 0;i <middle.length; i++){
				bw.write(fouceIndexMap.get(i)  + "   "  +  String.valueOf(middle[i]));
				bw.write("\n");				
			}
		   bw.write("-low score movie");
			bw.write("\n");
			for(int i = 0;i < low.length; i++){
				bw.write(fouceIndexMap.get(i)  + String.valueOf(low[i]));
				bw.write("\n");				
			}
			bw.close();
			System.out.println("file write donne ");
		}catch(IOException w){
			w.printStackTrace();
		}
	}
	

	public static HashMap<String, Integer> createMap(String[] names, HashMap<String, Integer> fouceNumMap){
		HashMap<String, Integer> fouceNum = new HashMap<String, Integer>();
		for(int i = 0; i < names.length; i ++){
			fouceNum.put(names[i], fouceNumMap.get(names[i]));
		}
		return fouceNum;
	}
  static double[] finalCalculate(JavaRDD<Tuple2<String, Integer[]>> hResult){	 
	  /*
	   *  通过aggregate() 的RDD操作把所有的　Tuple2<String, Integer[]>记录进行数组对应下标累加计算，得到一类电影的24个关注点总得分
	   * 每组数据都计算百分数，四舍五入
	   * */
	  int[] initial = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	  Function2<int[], Tuple2<String, Integer[]>, int[]> seqOp = 
			  new Function2<int[], Tuple2<String, Integer[]>, int[]>(){
		  			public int[] call(int[] array, Tuple2<String, Integer[]> tup){
		  				for(int i = 0; i < array.length; i ++){
		  					array[i] += tup._2[i];
		  				}
		  				return array;
		  			}
	  		} ;
	Function2<int[], int[], int[]> combOp =
			new Function2<int[], int[], int[]>(){
				public int[] call(int[] one, int[] two){
					for(int i = 0; i < one.length; i ++){
						one[i] += two[i];
					}
					return one;
				}
			};
	int[] hfinalRDD = hResult.aggregate(initial, seqOp, combOp);
	double[] pres =new double[hfinalRDD.length];
	double total = 0.00;
	for(int i: hfinalRDD)
		total += i;
	DecimalFormat   df   =new   java.text.DecimalFormat("#.0000");  
	for(int k= 0; k < hfinalRDD.length; k ++){
			//pres[k] = Double.valueOf(df.format(hfinalRDD[k]/ total));
		pres[k] = hfinalRDD[k] / total;
	}
	return pres;
  }
	
	static class stringArrayPair implements PairFunction<String, String, Integer[]>{
		//创建键值对：评论－》整型数组
		public Tuple2<String, Integer[]> call(String str){
			return new Tuple2<String, Integer[]>(str, new Integer[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
		}
	}
	static class splitRecord implements Function<String, String[]>{
		//把每一条记录以逗号切分：ｉｄ，评分，时间，评论
		public String[] call(String s){
			return s.split(",");
		}
	}
	
	static class filterEmpty implements Function<String[], Boolean>{
		//数据清洗：确保　评分和评论　的正确性
		public Boolean call(String[] array){
			if(array.length == 3){
				Pattern pattern = Pattern.compile("^[0-9]+(.[0-9]+)?$"); 
			   Matcher isNum = pattern.matcher(array[0]);
			   if( (!array[0].isEmpty()) && (isNum.matches()) && (! array[2].isEmpty()) ){
				   return true;
			}else 
				return false; 	
		}else
			return false;
	}
	}
	static class createPair implements PairFunction<String[], Integer, String>{
		//创建键值对：评分－》评论
		public Tuple2<Integer, String>  call(String[] args){
			String IntScore = args[0].substring(0, args[0].indexOf("."));
			return new Tuple2<Integer, String>(Integer.valueOf(IntScore), args[2]);
		}
	}
	static class filterHighPoint implements Function<Tuple2<Integer, String>, Boolean>{
		//过滤保留高分RDD
		public Boolean call(Tuple2<Integer, String> tp){
			if(tp._1 >= high){
				return true;
			}else
				return false;
		}
	}
	static class filterMiddlePoint implements Function<Tuple2<Integer, String>, Boolean>{
		//过滤保留中分RDD
		public Boolean call(Tuple2<Integer, String> tp){
			if( middle < tp._1 && tp._1 < high){
				return true;
			}else
				return false;
		}
	}
	static class filterLowPoint implements Function<Tuple2<Integer, String>, Boolean>{
		//过滤保留低分RDD
		public Boolean call(Tuple2<Integer, String> tp){
			if( tp._1 <= middle){
				return true;
			}else
				return false;
		}
	}
    private static List<String> readFile(String path) throws IOException {
    	//给定路径和list,读取文件
    	List<String> list = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            //String temp = null;
            String tempLine = null;
            while( (tempLine = br.readLine())!= null ) {
            	//System.out.println(tempLine);
                list.add(tempLine);
            }
            System.out.println("Dict has been read ! " + path);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
                br.close();
        }
		return list;
    }
  
	static class findFouces implements Function<Tuple2<String, Integer[]>, Tuple2<String, Integer[]>>{
		/*
		 * 对每一条评论计算２４个关注点的得分
		 * 该类进行加载字典及匹配
		 * */
		HashMap<String, Integer> fouceIndexMap = setHashMap(true);
		//主题
		//List<String> themeDict;
		List<String> style;//风格
		List<String> themeContent;//题材内容
		//视听
		//List<String> shitingDict;
		List<String> action;//动作
		List<String> frame;//画面
		List<String> cameraLen;//镜头
		List<String> music;//音乐
		//角色
		//List<String> roleDict;
		List<String> hero;//男主角
		List<String> heroine;//女主角
		List<String> villain;//反派
		List<String> costar;//配角
		//制作
		//List<String> manufactureDict;//
		List<String> scriptwriter;//编剧
		List<String> company;//出品公司
		List<String> director;//导演
		List<String> scence;//选景
		//剧情
		//List<String> storyDict;
		List<String> start;//开头
		List<String> develop;//发展
		List<String> end;//结局
		List<String> cryPoint;//泪点
		List<String> laughtPoint;//笑点
		public findFouces() throws IOException{
			//读取字典
			this.themeContent= readFile(pathConfig.themeContent); this.style = readFile(pathConfig.style);
			
			this.action = readFile(pathConfig.action);this.cameraLen= readFile(pathConfig.cameraLen); this.frame= readFile(pathConfig.frame);this.music = readFile(pathConfig.music); 
			
			this.cryPoint = readFile(pathConfig.cryPoint); this.develop= readFile(pathConfig.develop);this.start = readFile(pathConfig.start);this.end = readFile(pathConfig.end);
			 this.laughtPoint = readFile(pathConfig.laughtPoint);
			
			this.scence= readFile(pathConfig.scence); this.scriptwriter = readFile(pathConfig.scriptwriter); this.director = readFile(pathConfig.director);
			this.company = readFile(pathConfig.company);
			
			this.hero = readFile(pathConfig.hero);this.heroine= readFile(pathConfig.heroine); this.villain= readFile(pathConfig.villain);this.costar = readFile(pathConfig.costar); 
		}
		public Tuple2<String, Integer[]> call(Tuple2<String, Integer[]> tup){
			//story
			for(String word: this.style){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("风格")] += 1;
				}
			}
			for(String word: this.themeContent){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("题材内容")] += 1;
				}
			}
			tup._2[fouceIndexMap.get("主题")] = tup._2[ fouceIndexMap.get("风格")] + tup._2[ fouceIndexMap.get("题材内容")];
			//shiting 
			for(String word: this.action){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("动作")] += 1;
				}
			}
			for(String word: this.cameraLen){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("镜头")] += 1;
				}
			}
			for(String word: this.frame){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("画面")] += 1;
				}
			}
			for(String word: this.music){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("音乐")] += 1;
				}
			}
			tup._2[fouceIndexMap.get("视听")] = tup._2[ fouceIndexMap.get("动作")] + tup._2[ fouceIndexMap.get("镜头")] + 
					 tup._2[ fouceIndexMap.get("画面")] + tup._2[ fouceIndexMap.get("音乐")] ;
			//story
			for(String word: this.start){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("开头")] += 1;
				}
			}
			for(String word: this.develop){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("发展")] += 1;
				}
			}
			for(String word: this.end){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("结局")] += 1;
				}
			}
			for(String word: this.cryPoint){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("泪点")] += 1;
				}
			}
			for(String word: this.laughtPoint){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("笑点")] += 1;
				}
			}
			tup._2[fouceIndexMap.get("剧情")] = tup._2[ fouceIndexMap.get("开头")] + tup._2[ fouceIndexMap.get("发展")] + 
					 tup._2[ fouceIndexMap.get("结局")] + tup._2[ fouceIndexMap.get("笑点")]  + tup._2[ fouceIndexMap.get("泪点")] ;
			//manufacture
			for(String word: this.scriptwriter){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("编剧")] += 1;
				}
			}
			for(String word: this.director){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("导演")] += 1;
				}
			}
			for(String word: this.company){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("出品公司")] += 1;
				}
			}
			for(String word: this.scence){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("选景")] += 1;
				}
			}
			tup._2[ fouceIndexMap.get("制作")]= tup._2[ fouceIndexMap.get("导演")] + tup._2[ fouceIndexMap.get("出品公司")] + 
					 tup._2[fouceIndexMap.get("编剧")] +  tup._2[ fouceIndexMap.get("选景")] ;
			//role
			for(String word: this.hero){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("男主角")] += 1;
				}
			}
			for(String word: this.heroine){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("女主角")] += 1;
				}
			}
			for(String word: this.villain){				
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("反派")] += 1;
				}
			}
			for(String word: this.costar){
				if(tup._1.contains(word)){
					tup._2[ fouceIndexMap.get("配角")] += 1;
				}
			}
			tup._2[fouceIndexMap.get("角色")] =  tup._2[ fouceIndexMap.get("男主角")] +tup._2[ fouceIndexMap.get("女主角")] 
					+ tup._2[ fouceIndexMap.get("反派")] +  tup._2[ fouceIndexMap.get("配角")] ;
			return tup;
		}
 	}
	


	public static void main(String[] args) throws IOException {
		FouceAnalysis fa = new FouceAnalysis("/home/yingying/sqlIdFile/XingQiuDaZhan.txt");
	}

}
