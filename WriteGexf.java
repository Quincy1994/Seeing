package pro01;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WriteGexf {

	/**
	 * 根据给出的,txt文件，写成gexf文件
	 */
		List<String> labelList = new ArrayList<String>();//存放结点的字符串序列
		List<String> valueList = new ArrayList<String>();//存放类别的字符串序列
		List<String> edgeList = new ArrayList<String>();	//存放边指向的字符串序列
		String name ;
		
	public WriteGexf(String txtPath){
		createStrList(txtPath);//构造三条字符串序列
		this.name = txtPath.split("/")[txtPath.split("/").length - 1].replace(".txt", ".gexf");
	
//		for(String s: this.labelList
//			System.out.println(s);
//		for(String s: this.valueList)
//			System.out.println(s);
//		for(String s: this.edgeList)
//			System.out.println(s);
		writeIntoFile();//写入文件
	}
	
	public void createStrList(String path){
		//构造三条字符串序列
		System.out.println("createStrlist' path: " + path);
		File file = new File(path);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int increId = 0;
			int edgeId = 0;
			int length = 0;
			while((tempString = reader.readLine()) != null ){
				List map = new ArrayList<String>();
				//System.out.println(tempString);
				if(tempString.length() != 0){
				int value =Integer.parseInt(tempString.split(":")[0]) ;
				String[] words = tempString.split(":")[1].split(" ");
				
				//结点
				for(int i = 0; i< words.length; i ++){
					String nodeLabel = "<node id=\"" + increId + "\" label=\"" + words[i] + "\">" ;
					String valeStr = "<attvalue for=\"modularity_class\" value=\"" + value + "\"/>";
					this.labelList.add(nodeLabel);
					this.valueList.add(valeStr);
					increId ++;
				}
				//类别
				for(int index = 0; index < words.length; index ++){
					for(int latterIndex = index + 1; latterIndex < words.length; latterIndex ++){
						String edgStr = "<edge id=\"" + edgeId + "\" source=\"" + (length + index) + "\" target=\"" 
													+ (length + latterIndex) + "\" weight=\"1\"></edge>";
						this.edgeList.add(edgStr);
						edgeId ++;
					}
				}					
				length += words.length;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}		
	}
	
	public void writeIntoFile(){
		//写入文件
		try{
			String path = "/home/yingying/桌面/gexfCollection/" + this.name;
			System.out.println("writeIntoFile' path: " + path);
			File file = new File(path);
			if(!file.exists())
				file.createNewFile();
			else {//如果文件已经存在，先删除它再新建，以防重复写，出现错误
				file.delete();
				file.createNewFile();
				System.out.println("file exist");
			}
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			ArrayList<String> strs = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			strs.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			strs.add("<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\" xmlns:viz=\"http://www.gexf.net/1.2draft/viz\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\">");
			strs.add("<meta lastmodifieddate=\"2014-01-30\">");
			strs.add("<creator>Gephi 0.8.1</creator>");
			strs.add("<description/>");
			strs.add("</meta>");
			strs.add("<graph defaultedgetype=\"undirected\" mode=\"static\">");
			strs.add("<attributes class=\"node\" mode=\"static\">");
			strs.add("<attribute id=\"modularity_class\" title=\"Modularity Class\" type=\"integer\"/>");
			strs.add("</attributes>");
			strs.add("<nodes>");
		
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			sb.append("<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\" xmlns:viz=\"http://www.gexf.net/1.2draft/viz\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\">\n");
			sb.append("<meta lastmodifieddate=\"2014-01-30\">\n");
			sb.append("<creator>Gephi 0.8.1</creator>\n");
			sb.append("<description><description/>\n");
			sb.append("</meta>\n");
			sb.append("<graph defaultedgetype=\"undirected\" mode=\"static\">\n");
			sb.append("<attributes class=\"node\" mode=\"static\">\n");
			sb.append("<attribute id=\"modularity_class\" title=\"Modularity Class\" type=\"integer\"/>\n");
			sb.append("</attributes>\n");
			sb.append("<nodes>\n");
			
			for(String str: strs){
				bw.write(str);
				System.out.println(str);
				bw.write("\n");
//				bw.flush();
//				break;
			}

//			bw.write(sb.toString());
			if(this.labelList.size() != this.valueList.size())
				System.out.println("wrong ! no same length");
			else{
				System.out.println("label list share the same length with edge list");
				//结点，类别
				for(int index = 0; index < this.labelList.size(); index ++){
					bw.write(labelList.get(index));
					bw.write("\n");
					bw.write("<attvalues>");
					bw.write("\n");
					bw.write(valueList.get(index));
					bw.write("\n");				
					bw.write("</attvalues>");
					bw.write("\n");	
					bw.write("<viz:size value=\"11\"/>");
					bw.write("\n");	
					bw.write("<viz:color r=\"235\" g=\"81\" b=\"72\"/>");
					bw.write("\n");	
					bw.write("</node>");
					bw.write("\n");	
				}
			bw.write("</nodes>");
			bw.write("\n");			
			bw.write("<edges>");
			bw.write("\n");
			//边
			for(int k = 0; k < this.edgeList.size(); k ++){
				bw.write(edgeList.get(k));
				bw.write("\n");
			}
			bw.write("</edges>");
			bw.write("\n");
			bw.write("</graph>");
			bw.write("\n");
			bw.write("</gexf>");
			bw.close();				
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		WriteGexf test = new WriteGexf("/home/yingying/下载/话题识别效果/星球大战.txt");
	}

}
