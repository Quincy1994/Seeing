package com.ansj.vec;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.hive.ql.parse.HiveParser_IdentifiersParser.booleanValue_return;

import com.ansj.vec.domain.WordEntry;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;

public class Word2VEC {
	
	private HashMap<String, float[]> wordMap = new HashMap<String, float[]>();
	private int words;
	private int size;
	private int topNSize = 800;
	
	public static void main(String[] args) throws IOException{
//		Word2VEC vec = new Word2VEC();
//		vec.loadJavaModel("data/vectorModel/AllComments");
//		System.out.println(vec.getSimilarScore("抢戏", "最终boss"));
		
		
		
//		List<String> words = new ArrayList<String>();
//		words.add("画面");
//		Set<WordEntry> wordSet = vec.distance(words);
//		System.out.println("关注点词："+words.get(0));
//        System.out.println("相似词：");
////		System.out.println(wordSet);
//		List<WordEntry> wordList = new ArrayList<WordEntry>();
//		int n = 1;
//		for(WordEntry wordEntry:wordSet){
//			wordList.add(wordEntry);
////			System.out.print(n+".");
//			System.out.println(n+wordEntry.name);
//			n++;
//			if(n == 31) break; 
//		}
		
//		List<String> similarName = getSimilarWord(entity,vec);
//		for(String name:similarName){
//			System.out.println(name);
//		}
		
//		Mongo connection = new Mongo("127.0.0.1",27017);
//		DB db = connection.getDB("local");
//		boolean ok = db.("root", "".toCharArray());
//		System.out.println(ok); 
		

//	      try{   
//	          // 连接到 mongodb 服务
//	            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
//	            mongoClient.
//	            // 连接到数据库
//	            MongoDatabase mongoDatabase = mongoClient.getDatabase("mycol");  
//	          System.out.println("Connect to database successfully");
////	          mongoDatabase.createCollection("test");
////	          System.out.println("集合创建成功");
//	          MongoCollection<Document> collection = mongoDatabase.getCollection("test");
//	          System.out.println("集合 test 选择成功");
//	          
//	          Document document = new Document().  
//	        	         append("description", "database").  
//	        	         append("likes", 100).  
//	        	         append("by", "Fly");  
//	        	         List<Document> documents = new ArrayList<Document>();  
//	        	         documents.add(document);  
//	        	         collection.insertMany(documents);  
//	        	         System.out.println("文档插入成功");  
//	          
////	          FindIterable<Document> findIterable = collection.find();  
////	          MongoCursor<Document> mongoCursor = findIterable.iterator();  
////	          while(mongoCursor.hasNext()){  
////	             Document document1 =mongoCursor.next();  
////	             System.out.println(document1.get("likes"));
////	          }  
//	        	      }
//	      	 catch(Exception e){
//	        	         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//	         }
	      
	         
	}
	
	/**
	 * 获取ｅｎｔｉｔｙ的同义词
	 * @param entity　实体名
	 * @param vec	word2vec模型
	 * @return　同义词列表
	 */
	public static List<String> getSimilarWord(String entity,Word2VEC vec){
		int wordLen=entity.length();
		List<String> similarName = new ArrayList<String>();
		Set<WordEntry> wordSet = vec.distance(entity);
//		System.out.println(wordSet);
		List<WordEntry> wordList = new ArrayList<WordEntry>();
		for(WordEntry wordEntry:wordSet){
			wordList.add(wordEntry);
		}
		for(WordEntry wordEntry:wordList){
			if((!similarName.contains(wordEntry.name))&&wordEntry.name!=entity){
					for(int i=0;i<wordLen;i++){
						if(wordEntry.name.contains(entity.substring(i))){
							similarName.add(wordEntry.name);
							break;
						}
					}
			}
			Set<WordEntry>  similarWordSet = vec.distance(wordEntry.name);
//			System.out.println(similarWordSet);
			List<String> similarWordList = new ArrayList<String>();
			for(WordEntry similarWord:similarWordSet){
				similarWordList.add(similarWord.name);
			}

			for(String similarWord:similarWordList){
//				System.out.println(similarWord);
				if(similarWord!=entity){
//					System.out.println(similarWordList.indexOf(entity));
					if(similarWordList.indexOf(entity)<5&&similarWordList.indexOf(entity)!=-1&&(!similarName.contains(wordEntry.name))){
						similarName.add(wordEntry.name);
				    }
				if((!similarName.contains(similarWord))&&wordEntry.name!=entity){
					if(similarName.contains(wordEntry.name)){
						for(int i=0;i<wordLen;i++){
							if(similarWord.contains(entity.substring(i))){
								similarName.add(similarWord);
								break;
							}
						}
					}
//						System.out.println(similarWordList.indexOf(entity));
				}
			}
		  }
	}
//	for(String name:similarName){
//		System.out.println(name);
//	}
		for(int i=0;i<similarName.size();i++){
			if(similarName.get(i).equals(entity)){
				similarName.remove(i);
			}
		}
		return similarName;
	}
	


	/**
	 * 加载模型
	 * 
	 * @param path
	 *            模型的路径
	 * @throws IOException
	 */
	public void loadGoogleModel(String path) throws IOException {
		DataInputStream dis = null;
		BufferedInputStream bis = null;
		double len = 0;
		float vector = 0;
		try {
			bis = new BufferedInputStream(new FileInputStream(path));
			dis = new DataInputStream(bis);
			// //读取词数
			words = Integer.parseInt(readString(dis));
			// //大小
			size = Integer.parseInt(readString(dis));
			String word;
			float[] vectors = null;
			for (int i = 0; i < words; i++) {
				word = readString(dis);
				vectors = new float[size];
				len = 0;
				for (int j = 0; j < size; j++) {
					vector = readFloat(dis);
					len += vector * vector;
					vectors[j] = (float) vector;
				}
				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					vectors[j] /= len;
				}

				wordMap.put(word, vectors);
				dis.read();
			}
		} finally {
			bis.close();
			dis.close();
		}
	}

	/**
	 * 加载模型
	 * 
	 * @param path
	 *            模型的路径
	 * @throws IOException
	 */
	public void loadJavaModel(String path) throws IOException {
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
			words = dis.readInt();
			size = dis.readInt();

			float vector = 0;

			String key = null;
			float[] value = null;
			for (int i = 0; i < words; i++) {
				double len = 0;
				key = dis.readUTF();
				value = new float[size];
				for (int j = 0; j < size; j++) {
					vector = dis.readFloat();
					len += vector * vector;
					value[j] = vector;
				}

				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					value[j] /= len;
				}
				wordMap.put(key, value);
			}

		}
	}

	private static final int MAX_SIZE = 50;

	/**
	 * 近义词
	 * 
	 * @return
	 */
	public TreeSet<WordEntry> analogy(String word0, String word1, String word2) {
		float[] wv0 = getWordVector(word0);
		float[] wv1 = getWordVector(word1);
		float[] wv2 = getWordVector(word2);

		if (wv1 == null || wv2 == null || wv0 == null) {
			return null;
		}
		float[] wordVector = new float[size];
		for (int i = 0; i < size; i++) {
			wordVector[i] = wv1[i] - wv0[i] + wv2[i];
		}
		float[] tempVector;
		String name;
		List<WordEntry> wordEntrys = new ArrayList<WordEntry>(topNSize);
		for (Entry<String, float[]> entry : wordMap.entrySet()) {
			name = entry.getKey();
			if (name.equals(word0) || name.equals(word1) || name.equals(word2)) {
				continue;
			}
			float dist = 0;
			tempVector = entry.getValue();
			for (int i = 0; i < wordVector.length; i++) {
				dist += wordVector[i] * tempVector[i];
			}
			insertTopN(name, dist, wordEntrys);
		}
		return new TreeSet<WordEntry>(wordEntrys);
	}

	private void insertTopN(String name, float score, List<WordEntry> wordsEntrys) {
		// TODO Auto-generated method stub
		if (wordsEntrys.size() < topNSize) {
			wordsEntrys.add(new WordEntry(name, score));
			return;
		}
		float min = Float.MAX_VALUE;
		int minOffe = 0;
		for (int i = 0; i < topNSize; i++) {
			WordEntry wordEntry = wordsEntrys.get(i);
			if (min > wordEntry.score) {
				min = wordEntry.score;
				minOffe = i;
			}
		}

		if (score > min) {
			wordsEntrys.set(minOffe, new WordEntry(name, score));
		}

	}
    public float getSimilarScore(String word1,String word2){
    	float[] vector1 = wordMap.get(word1);
    	float[] vector2 = wordMap.get(word2);
    	float dist = 0;
    	for (int i = 0; i < vector2.length; i++) {
    		dist += vector1[i] * vector2[i];
    	}
	   return dist;
    }
	public Set<WordEntry> distance(String queryWord) {

		float[] center = wordMap.get(queryWord);
		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}

	public Set<WordEntry> distance(List<String> words) {

		float[] center = null;
		for (String word : words) {
			center = sum(center, wordMap.get(word));
		}

		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();
		return result;
	}

	private float[] sum(float[] center, float[] fs) {
		// TODO Auto-generated method stub

		if (center == null && fs == null) {
			return null;
		}

		if (fs == null) {
			return center;
		}

		if (center == null) {
			return fs;
		}

		for (int i = 0; i < fs.length; i++) {
			center[i] += fs[i];
		}

		return center;
	}

	/**
	 * 得到词向量
	 * 
	 * @param word
	 * @return
	 */
	public float[] getWordVector(String word) {
		return wordMap.get(word);
	}

	public static float readFloat(InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		is.read(bytes);
		return getFloat(bytes);
	}

	/**
	 * 读取一个float
	 * 
	 * @param b
	 * @return
	 */
	public static float getFloat(byte[] b) {
		int accum = 0;
		accum = accum | (b[0] & 0xff) << 0;
		accum = accum | (b[1] & 0xff) << 8;
		accum = accum | (b[2] & 0xff) << 16;
		accum = accum | (b[3] & 0xff) << 24;
		return Float.intBitsToFloat(accum);
	}

	/**
	 * 读取一个字符串
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 */
	private static String readString(DataInputStream dis) throws IOException {
		// TODO Auto-generated method stub
		byte[] bytes = new byte[MAX_SIZE];
		byte b = dis.readByte();
		int i = -1;
		StringBuilder sb = new StringBuilder();
		while (b != 32 && b != 10) {
			i++;
			bytes[i] = b;
			b = dis.readByte();
			if (i == 49) {
				sb.append(new String(bytes));
				i = -1;
				bytes = new byte[MAX_SIZE];
			}
		}
		sb.append(new String(bytes, 0, i + 1));
		return sb.toString();
	}

	public int getTopNSize() {
		return topNSize;
	}

	public void setTopNSize(int topNSize) {
		this.topNSize = topNSize;
	}

	public HashMap<String, float[]> getWordMap() {
		return wordMap;
	}

	public int getWords() {
		return words;
	}

	public int getSize() {
		return size;
	}

}
