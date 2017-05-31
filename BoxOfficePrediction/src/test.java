import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.feature.StandardScaler;
import org.apache.spark.ml.feature.StandardScalerModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.VectorUDT;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import scala.Tuple2;


public class test{
	
	public static void main(String[] args){
		SparkConf conf = new SparkConf().setAppName("SVM").setMaster("local");
		conf.set("spark.testing.memory", "2147480000");
		SparkContext sc = new SparkContext(conf);
		
		String path = "/home/quincy/result.txt";
		JavaRDD<LabeledPoint> testData = MLUtils.loadLibSVMFile(sc, path).toJavaRDD();
		
		//转化DataFrame数据类型
		JavaRDD<Row> jrow =testData.map(new LabeledPointToRow());
		StructType schema = new StructType(new StructField[]{
					new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
					new StructField("features", new VectorUDT(), false, Metadata.empty()),
		});
		SQLContext jsql = new SQLContext(sc);
		DataFrame dataFrame = jsql.createDataFrame(jrow, schema);
//		
		StandardScaler scaler = new StandardScaler()
			.setInputCol("features")
			.setOutputCol("scaledFeatures")
			.setWithMean(true)
			.setWithStd(false);
		StandardScalerModel scalerModel = scaler.fit(dataFrame);
		DataFrame scaledData = scalerModel.transform(dataFrame).select("label","scaledFeatures");
//		PolynomialExpansion polyExpansion = new PolynomialExpansion()
//			.setInputCol("features")
//			.setOutputCol("ployFeatures")
//			.setDegree(150);
//		DataFrame ployDF = polyExpansion.transform(dataFrame);
		
//		DCT dct = new DCT().setInputCol("features").setOutputCol("featuresDCT").setInverse(false);
//		DataFrame dctDF = dct.transform(dataFrame);
		
//		ChiSqSelector selector = new ChiSqSelector()
//			.setNumTopFeatures(60)
//			.setFeaturesCol("features")
//			.setOutputCol("selectedfeatures");
//		DataFrame selectDF = selector.fit(dataFrame).transform(dataFrame);
		
		DataFrame[] splits = scaledData.randomSplit(new double[]{0.7, 0.3}, 1234L);
		DataFrame train = splits[0];
		DataFrame test = splits[1];
		int[] layers = new int[]{89,60,30,3};
		MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
			.setLayers(layers)
			.setBlockSize(128)
			.setSeed(1234L)
			.setFeaturesCol("scaledFeatures")
			.setMaxIter(100);
		
		final MultilayerPerceptronClassificationModel model = trainer.fit(train);
		JavaRDD<Row> testrdd = test.javaRDD();
		JavaRDD<LabeledPoint> testset = testrdd.map(new RowToLabelPoint());
		JavaRDD<Tuple2<Double, Double>> predictResult = testset.map(new Prediction(model)) ;
		
		double accuracy = predictResult.filter(new PredictAndScore()).count() * 1.0 / predictResult.count();
		System.out.println(accuracy);
		sc.stop();
	}
	
	static class RowToLabelPoint implements Function<Row, LabeledPoint> {
		
		public LabeledPoint call(Row r) throws Exception {
			Vector features = r.getAs(1);
			double label = r.getDouble(0);
			return new LabeledPoint(label, features);
		}
	}
	
	static class Prediction implements Function<LabeledPoint, Tuple2<Double , Double>> {
		
		public MultilayerPerceptronClassificationModel model;
		
		public Prediction(MultilayerPerceptronClassificationModel model){
			this.model = model;
		}
		public Tuple2<Double, Double> call(LabeledPoint p) throws Exception {
			Double score = model.predict(p.features());
			return new Tuple2<Double , Double>(score, p.label());
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
	
	static class LabeledPointToRow implements Function<LabeledPoint, Row> {
		
		public Row call(LabeledPoint p) throws Exception {
			double label = p.label();
			Vector vector = p.features();
			return RowFactory.create(label, vector);
		}
	}
}
