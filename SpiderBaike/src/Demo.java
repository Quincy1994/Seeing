import com.hankcs.hanlp.HanLP;


public class Demo {
	
	public static void main(String[] args){
		String sentence = "我喜欢这部电影是因为它的故事情节十分精彩";
		System.out.println(HanLP.parseDependency(sentence));
	}
 
}
