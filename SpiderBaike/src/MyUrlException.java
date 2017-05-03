
public class MyUrlException extends Exception{
	
	private String url;

	public MyUrlException(String url){
		super("this url is empty");
		this.url = url;
	}
	
	/**
	 * @param args
	 */
}
