package lN_simple5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class copyResults {
	public static void copy(String name){
		String sourcestring= System.getProperty("user.dir");
		File source = new File(sourcestring);		
		String target = Constants.DESTINATION+name+"/temp_"+Constants.randomnumber;
		new File(target).mkdir();
		File dest = new File(target);			
		File[] filelist = source.listFiles(); 
		for (int i=0; i < filelist.length; i++){
			System.out.println(source+"/"+filelist[i].getName());
			String x=(source+"/"+filelist[i].getName());
			String y=(dest + "/"+ filelist[i].getName());				
			 Path FROM = Paths.get(x);
		     Path TO = Paths.get(y);
		        try {
					Files.copy(FROM,TO);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}								
		}		
	  }
} 




