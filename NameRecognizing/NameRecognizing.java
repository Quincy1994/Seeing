package Algorithm;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import scala.Tuple2;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import antlr.collections.impl.Vector;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dependency.CRFDependencyParser;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.common.Term;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import emotion.EmotionAnalysis;

public class NameRecognizing {

	JavaRDD<String[]>cleanData;
	private static String curFilmID = ""; //当前电影ＩＤ，用于抽取特定电影样本集;
//	public static Segment segment = HanLP.newSegment();//分词器
	private static Segment segment = new CRFSegment();//CRF分类器
	 public static DecisionTreeModel treeClassifier ;// 决策树分类器
//	 public static RandomForestModel randomForest;// 决策树分类器
	//词典
	private static  HashMap<String,Double> wordToNum = new HashMap<String,Double>();//将词转化为对应的数值标签
	static public Map<String,Object> wordsMap;
	
	public static void main(String[] args) {
//		 TODO Auto-generated method stub
//		JavaSparkContext sc = createSparkInstance();//产生ｓｐａｒｋ对象
//		classifierTraining(sc);
//		List<String> prenames = NameRecognizing.extractName(loadComment("film_comment/5645c9fd756a5d75424ca26a.txt",sc), sc);
//		HashMap<String,ArrayList<String>> synonymDict = synonymRecognizing(prenames);
//		List<String> names = new ArrayList<String>();
//		for(String name:prenames){
//			if(treeClassifier.predict(Vectors.dense(toArray(name))) == 1){
//				names.add(name);
//			}
//		}
//		for(String name:names){
//			System.out.println(name);
//		}
//		JavaRDD<String []> cleanData = getcleanData(sc);//获取数据集
//		cleanData.cache(); //对数据集作缓存
//		classifierTraining(sc);
//		List<String> idSet = loadDict("data/filmID.txt");
//		for(String id:idSet){
//			List<String> wordList = getFilmPrename(id,cleanData,sc);
//			HashMap<String,ArrayList<String>> synonymDict = synonymRecognizing(wordList);
//			List<String> names = new ArrayList<String>();
//			for(String name:wordList){
//				System.out.println(name+ " "+treeClassifier.predict(Vectors.dense(toArray(name))));
//			}
//			for(String name:wordList){
//				//创建ｔｘｔ文档
//				File fileName = new File("data/FilmName/"+id+".txt");
//				  try{  
//					   if(!fileName.exists()){  
//					    fileName.createNewFile();  
//					    System.out.println("create File succeed");
//					   }  
//					  }
//				  catch(Exception e){ 
//					  System.out.println(id+"　文件已存在");
//					  continue; //若文件存在这不对该电影评论作分析;
//					   
//					  }  
//				
//				
//				if(treeClassifier.predict(Vectors.dense(toArray(name))) == 1){
//					names.add(name);
//				}
//			 }
//		   String rows = "";
//		   int i = 0;
//			for(String name:names){
//				String row = new String();
//				row += name + " ";
//				try{
//					for(String word:synonymDict.get(name)){
//						row += word+" ";
//					}
//				}
//				catch(Exception e){
//					;
//				}
//				row += "\n";
//				rows+=row;
//				i++;
//				if(i == 20) break;
//				}
//			//写入数据;
//		  FileWriter writer;
//		  try {
//		            writer = new FileWriter("data/FilmName/"+id+".txt",false);
//		            writer.write(rows);
//					writer.close();
//		   } 
//		  catch (IOException e) {
//		            e.printStackTrace();
//		        }
//		  System.out.println(id+ "     分析完成\n\n\n\n\n\n\n");
//		}
//		for(String name : synonymDict.keySet()){
//			System.out.print(name+ ":");
//			for(String word:synonymDict.get(name)){
//				System.out.print(word+ "  ");
//			}
//			System.out.println(" ");
//		}
//		sc.close();
	}
	/**
	 * 利用ＣＲＦ提取人名词
	 * @param commentsRDD
	 * @param sc
	 * @return
	 */
	public List<String>  CRF(JavaRDD<String> commentsRDD,JavaSparkContext sc){
		return 	this.extractName(commentsRDD, sc);
	}
	
	/**
	 * 利用决策书过滤人名
	 * @param names
	 * @return
	 */
	public List<String> Tree(List<String> names){
		List<String> bestNames=new ArrayList<String>();
		for(String name:names){
			if(treeClassifier.predict(Vectors.dense(toArray(name))) == 1){
				bestNames.add(name);
			}
		}
		return bestNames;
	}
//	
//	public List<String> Forest(List<String> names){
//		List<String> bestNames=new ArrayList<String>();
//		for(String name:names){
//			if(randomForest.predict(Vectors.dense(toArray(name))) == 1){
//				bestNames.add(name);
//			}
//		}
//		return bestNames;
//	}

    private static List<String> loadDict(String fileName){
        //读取字典，并保存为Dictionary结构
    	List<String> Dict = new ArrayList<String>(); 
        File file = new File(fileName);
        try {
            //判断文件是否存在
            InputStreamReader read = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
            	lineTxt = lineTxt.replaceAll("\n", "");
                Dict.add(lineTxt);
                } 
            bufferedReader.close();
            } 
        catch (Exception e) {  
                e.printStackTrace(); 
        }   
        
    	return Dict;
    }

    public static JavaSparkContext createSparkInstance(){
    	//创建ｓｐａｒｋ实例
		SparkConf conf = new SparkConf().setMaster("local") .setAppName("analysis").registerKryoClasses(new Class[] {Term.class});
		JavaSparkContext sc = new JavaSparkContext(conf);
		return sc;
    }
    
//	public static void storeCleanSample(){
//		//进行数据清洗，并将数据集以类的形式保存到磁盘
//		SparkConf conf = new SparkConf().setMaster("local") .setAppName("analysis");
//		JavaSparkContext sc = new JavaSparkContext(conf);
//		JavaRDD<String> csvData = sc.textFile("/home/monkeys/下载/第10题赛题数据.csv");
//		JavaRDD<String[]>teamData = csvData.map(new RddSplit()).filter(new DataCleaning());
//		teamData.saveAsObjectFile("src/CleanSample");
//		sc.close();
//	}
	
	public static JavaRDD<String> loadComment(String path,JavaSparkContext sc){
		//读取评论txt
		JavaRDD<String> commentRDD = sc.textFile(path);
//		List<String> comments = commentRDD.collect();
//		for(String comment:comments){
//			System.out.println(comment);
//		}
		return commentRDD;
	}
	
	public static  JavaRDD<String[]> getcleanData(JavaSparkContext sc ){
		//获取已清洗数据（ＲＤＤ）
		JavaRDD<String[]>cleanData = sc.objectFile("src/CleanSample");
		
		return cleanData;
	}
	
	public static JavaRDD<List<Term>> segRDD(JavaRDD<String> commentsRDD,JavaSparkContext sc){
		JavaRDD<List<Term>> commentWords = commentsRDD.map(new segmentation());
		return commentWords;
	}
	
	private static List<String> extractName(JavaRDD<String> filmComment,JavaSparkContext sc){
		JavaRDD<List<Term>>comment_words =  filmComment.map(new segmentation()).filter(new getName())//抽取含有人名评论
				.flatMap(//抽取命名实体
				new FlatMapFunction<List<Term>,List<Term>>(){
						public Iterable <List<Term>> call(List<Term> terms){
							List<Term> names = new ArrayList();
							for (Term term:terms){
								if (term.nature.toString() == "nr" ||term.nature.toString() == "nrf" || term.nature .toString()== "nrj"|| term.nature .toString()== "ns"||term.nature .toString()== "nz") {
									names.add(term);
								}
							}
							return Arrays.asList( names);
						}
			});
		JavaPairRDD<String,Integer>names = comment_words.mapToPair(
					new PairFunction<List<Term>,String,Integer>(){
						public Tuple2<String,Integer> call(List<Term> list){
							return new Tuple2(list.get(0).word,1);
						}
					}
		);
		 	wordsMap = names.countByKey();
//		 	for(String word: wordsMap.keySet()){
//		 		System.out.print(word+" ");
//		 		System.out.println(wordsMap.get(word));
//		 	
//		 	}	 	
		 	//词典根据values值进行排序
			Object[] freqArray =  names.countByKey().values().toArray();
			Arrays.sort(freqArray);//对词频进行排序
			Map<Object,List<String>>freqWords = new HashMap<Object,List<String>>();// 格式　词频：{word0,word1}
//			System.out.println(freqWords.get(1312));
			for(Object  i: freqArray){
				if (freqWords.get(i) == null){
					freqWords.put(i, new ArrayList<String>());
				}
			}
			for(String key:wordsMap.keySet()){
				freqWords.get(wordsMap.get(key)).add(key);
			}
			List<String> sortedName = new ArrayList<String>();
			for(String str : freqWords.get(freqArray[freqArray.length-1])){ 
				sortedName.add(str);	
				}
			for(int i=freqArray.length-2;i>=0;i--){
				if (freqArray[i] != freqArray[i+1]){
					for(String str : freqWords.get(freqArray[i])){
						if(!sortedName.contains(str)){
							sortedName.add(str);	
						}
						}
				}
			}
			return sortedName;
	}
	
	public  List<String> getFilmPrename(String filmID, JavaRDD<String[]> cleanData,JavaSparkContext sc){
	    NameRecognizing.curFilmID = filmID;
	    JavaRDD<String[]>filmData ;
	    if( NameRecognizing.curFilmID != ""){
	    	filmData = cleanData.filter(new FilmExtraction()); //抽取电影样本
	    }
	    else{
	    	filmData =  cleanData ;
	    }
		JavaRDD<String>filmComment = filmData.map(new CommentExtraction());//抽取评论
			return 	this.extractName(filmComment, sc);
	}
	
	private static void addWordToNum(List<String> dict){
		//扩充类变量wordToNum,用于字转化为可计算的数值型数据
		for(String word: dict){
			for(int i =0;i<word.length();i++){
				if(wordToNum.get(word.subSequence(i, i+1)) == null){
					wordToNum.put(word.substring(i, i+1),wordToNum.keySet().size()*1.0);
				}
			}
		}
	}
	
	private static  double[] toArray(String word){
		//将词转化为字，并将字映射为数值型，存至数组上，为分类器训练作准备
		int len = word.length();
		double[] array = new double[8]; //若名字超过8个字，默认以前八个字作为人名。
		for(int i =0;i<8;i++){
			array[i] = -1;
		}
		for(int i=0;i<len&&i<8;i++){
			if(wordToNum.get(word.substring(i, i+1)) != null){
				array[i] = wordToNum.get(word.substring(i, i+1));
			}
			else array[i] = -1;
		}
		return array;
	}
	
	public static void classifierTraining(JavaSparkContext sc){
		List<String>asianNameDict = loadDict("data/Name.txt");//读取人名
		List<String> notNameDict = loadDict("data/NotName.txt");//读取非人名词典
		NameRecognizing.addWordToNum(asianNameDict);//根据人名词典中存在的字,添加至wordToNum;
		NameRecognizing.addWordToNum(notNameDict);//根据非人名词典中存在的字,添加至wordToNum;
		
		List<List<String>> dictSet = new ArrayList<List<String>>();
		dictSet.add(notNameDict);
		dictSet.add(asianNameDict);
		List<LabeledPoint> trainingSet= new ArrayList<LabeledPoint>();
		for(int i = 0; i<dictSet.size();i++){
			for(String word:dictSet.get(i)){
				LabeledPoint pos  = new LabeledPoint(i, Vectors.dense(toArray(word)));
					trainingSet.add(pos);
				}
			}
			JavaRDD<LabeledPoint>training= sc.parallelize(trainingSet);

		      Map<Integer, Integer> categoricalFeaturesInfo = new HashMap();
		      String impurity = "gini";//对于分类问题，我们可以用熵entropy或Gini来表示信息的无序程度 ,对于回归问题，我们用方差(Variance)来表示无序程度，方差越大，说明数据间差异越大
		      Integer maxDepth = 10;//数深
		      Integer maxBins = 100000;//最大划分数
		      Integer numClasses = 2;//类别数量
		      treeClassifier = DecisionTree.trainClassifier(training, numClasses,categoricalFeaturesInfo, impurity, maxDepth, maxBins);//构建模型
//		      Integer numTrees = 3; // Use more in practice.
//		      String featureSubsetStrategy = "auto"; // Let the algorithm choose.
//		      Integer seed = 12345;
//		      randomForest= RandomForest.trainRegressor(training,
//		    	        categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins, seed);//参数与决策数基本一致，除了seed
//		      
	}
	
	public  static HashMap<String,ArrayList<String>> synonymRecognizing(List<String> wordList){
		   HashMap<String,ArrayList<String>> synonymDict = new HashMap<String,ArrayList<String>>();
		   HashMap<List<Pinyin>,String> pinyinDict = new  HashMap<List<Pinyin>,String> ();
		    for(int i =0;i<wordList.size();i++){
				String word = wordList.get(i);
				
				if(word.length()>2){
					if(synonymDict.keySet().contains(word.substring(0, word.length()-1))){
						synonymDict.get(word.substring(0, word.length()-1)).add(word);
						wordList.remove(word);
					}
					else{
						synonymDict.put(word, new ArrayList<String>());
						synonymDict.get(word).add(word);
//						System.out.println("haha");
					}		
					}
			    else{
			    	   synonymDict.put(word, new ArrayList<String>());
						synonymDict.get(word).add(word);
			    }
		   }

		    for(int i =0;i<wordList.size();i++){
		    	String word = wordList.get(i);
				List<Pinyin> pinyinList = HanLP.convertToPinyinList(word);
				String trueWord; 
				try{
					trueWord = pinyinDict.get(pinyinList);
					synonymDict.get(trueWord).add(word);
					wordList.remove(word);
				}
				catch(Exception e){
					pinyinDict.put(pinyinList, word);
				}
		    }
		    return synonymDict;
	}

	//RDＤ操作类
	private static class DataCleaning implements Function<String[],Boolean>{
		//ＲＤＤ数据清洗
		public Boolean call(String[]  row){//

			if (row.length == 4  &&  row[0].length() == 24){
				return true;
			}
			return false;
		}	
	}

	private static class FilmExtraction implements Function<String[],Boolean>{
		//抽取ＲＤＤ电影样本
		public Boolean call(String[]  row){
			if (row[0].equals(NameRecognizing.curFilmID)){
				return true;
			}
			return false;
		}	
	}
	
	private static class CommentExtraction implements Function<String[],String>{
		//ＲＤＤ抽取样本中的评论
		public String call(String[]  s){
			return s[3];
		}
	}
	
	private static class  segmentation implements Function<String, List<Term>>{
		public List<Term> call(String s){
			return segment.seg(s);
		}
	}
	
	private static class getName implements Function<List<Term>,Boolean>{
		public Boolean call(List<Term> terms){
			List<Term> names = new ArrayList();
			for (Term term:terms){
				if (term.nature.toString() == "nr" ||term.nature.toString() == "nrf" || term.nature .toString()== "nrj"||term.nature .toString()== "ns"||term.nature .toString()== "nz") {
					return true;
				}
			}
			return false;
		}
	}
	
	private static class RddSplit implements Function<String,String[]>{
		//样本结构转换
		public String[] call(String  s){
			return s.split(",",4);
		}
	}
//		for(String comment:filmComment.collect()){
//			System.out.println(comment);
}



