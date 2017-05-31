import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import scala.Tuple2;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class BoxOfficePrediction {


	String trainFile = "";
	String testFile = "";
	String movieNameFile = "";
	List<String> resultList ; //接收预测结果
	Map<Integer,String> movieName;
	
	public BoxOfficePrediction(String trainFile, String testFile,String movieNameFile){
		this.trainFile = trainFile;
		this.testFile = testFile;
		this.movieNameFile = movieNameFile;
		this.loadMovieName();
	}
	
	public void loadMovieName() {
		this.movieName = new HashMap<Integer, String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(this.movieNameFile)));
			String line;
			int num = 0;
			while((line=br.readLine())!=null){
				String movie = line;
				this.movieName.put(num, movie);
				num += 1;
			}
			br.close();
		}catch(Exception e){
			System.out.println("error");
		}
	}
	
	
	public void predict() throws IOException{
		SparkConf conf = new SparkConf().setAppName("SVM").setMaster("local");
		conf.set("spark.testing.memory", "2147480000");
		SparkContext sc = new SparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		
		
		// Load and parse the data file, converting it to a DataFrame.
		DataFrame trainData = sqlContext.read().format("libsvm").load(this.trainFile);
		DataFrame testData = sqlContext.read().format("libsvm").load(this.testFile);

		// Index labels, adding metadata to the label column.
		// Fit on whole dataset to include all labels in index.
		StringIndexerModel labelIndexer = new StringIndexer()
		  .setInputCol("label")
		  .setOutputCol("indexedLabel")
		  .fit(trainData);
		// Automatically identify categorical features, and index them.
		// Set maxCategories so features with > 4 distinct values are treated as continuous.
		VectorIndexerModel featureIndexer = new VectorIndexer()
		  .setInputCol("features")
		  .setOutputCol("indexedFeatures")
		  .setMaxCategories(4)
		  .fit(trainData);

		// Split the data into training and test sets (30% held out for testing)
//		DataFrame[] splits = trainData.randomSplit(new double[] {0.9, 0.1});
//		trainData = splits[0];
//		testData = splits[1];

		// Train a RandomForest model.
		RandomForestClassifier rf = new RandomForestClassifier()
			.setLabelCol("indexedLabel")
			.setFeaturesCol("indexedFeatures")
			.setNumTrees(20);

		// Convert indexed labels back to original labels.
		IndexToString labelConverter = new IndexToString()
		  .setInputCol("prediction")
		  .setOutputCol("predictedLabel")
		  .setLabels(labelIndexer.labels());

		// Chain indexers and forest in a Pipeline
		Pipeline pipeline = new Pipeline()
		  .setStages(new PipelineStage[] {labelIndexer, featureIndexer, rf, labelConverter});

		// Train model. This also runs the indexers.
		PipelineModel model = pipeline.fit(trainData);
		
		// Make predictions.
		DataFrame predictions = model.transform(testData);

		// Select example rows to display.
		predictions.select("predictedLabel", "label", "features").show(200);

		// Select (prediction, true label) and compute test error
		MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
		  .setLabelCol("indexedLabel")
		  .setPredictionCol("prediction")
		  .setMetricName("precision");
		double accuracy = evaluator.evaluate(predictions);
		System.out.println("Test Error = " + (1.0 - accuracy));

		RandomForestClassificationModel rfModel = (RandomForestClassificationModel)(model.stages()[2]);
//		System.out.println("Learned classification forest model:\n" + rfModel.toDebugString());
		
		DataFrame resultDF = predictions.select("predictedLabel");
		JavaRDD<Row> resultRow = resultDF.toJavaRDD();
		JavaRDD<String> result = resultRow.map(new Result());
		this.resultList = result.collect();
		for(String one: resultList){
			System.out.println(one);
		}
	}
	
	public void save() throws Exception{
		Mongo connection = new Mongo("192.168.235.20", 27017);
		DB db = connection.getDB("BoxOffice");
		boolean ok = db.authenticate("root","iiip".toCharArray());
		
		if(ok){
			System.out.println("db connection success!");
			DBCollection collection = db.getCollection("boxOffice");
			int length = this.resultList.size();
			for(int i=0;i<length;i++){
				String level = this.resultList.get(i);
				System.out.println("level:" + level);
				String boxoffice = "";
				if(level.equals("3.0")){
					boxoffice = "超过10亿元";
				}
				else if (level.equals("2.0")){
					boxoffice = "1亿元~~10亿元";
				}
				else if (level.equals("1.0")){
					boxoffice = "1千万~~1亿元";
				}
				else if(level.equals("0.0")){
					boxoffice = "低于1千万";
				}
				String movie = this.movieName.get(i);
				BasicDBObject document = new BasicDBObject();
				document.put("_id", movie);
				document.put("boxoffice", boxoffice);
				collection.insert(document);
			}
			System.out.println("save OK!");
		}
	}
	
	static class Result implements Function<Row,String>{
		
		public String call(Row r) throws Exception {
			String predict = r.getAs(0);
			return predict;
		}
	}
	
	static class Prediction implements Function<Row, Tuple2<Double , Double>> {
		
		public Tuple2<Double, Double> call(Row r) throws Exception {
			Double score = r.getDouble(0);
			Double label = r.getAs(1);
			return new Tuple2<Double , Double>(score, label);
		}
	}
	
	static class PredictAndScore implements Function<Tuple2<Double, Double>, Boolean> {
		public Boolean call(Tuple2<Double, Double> t) throws Exception {
			double score = t._1();
			double label = t._2();
			System.out.println("score:" + score + ", label:"+ label);
			if(score == label) return true;
			else return false;
		}
	}
	
	public static void main(String[] agrs) throws Exception {
		String trainFile = "data/training.txt";
		String movieFile = "data/movieName.txt";
		String testFile = "data/test.txt";
		BoxOfficePrediction boxOffice = new BoxOfficePrediction(trainFile, testFile, movieFile);
		boxOffice.predict();
		boxOffice.save();
	}
}
