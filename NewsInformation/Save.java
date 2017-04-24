package movieCrawler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Save {
    /**
     * 保存文件
     * @param filePath：文件路径
     * @param message：文件内容
     */
    public static void saveMovie(String filePath,String message){
        File file = new File(filePath);
        File parent = file.getParentFile();
        if((parent != null) && (!parent.exists())){
            parent.mkdirs();
        }
        if(!file.exists()) try {
            file.createNewFile();
            DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
            output.write(message .getBytes("utf-8"));
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

