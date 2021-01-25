package lN_simple5;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WriteArrayToFile {
	  
	  public WriteArrayToFile(){}
	  
	  public static void convertArrayList(String Filename ,ArrayList list) throws IOException {
		  String filename = Filename;
		  ArrayList<Integer>ListOfTimes = list;
		  //can do the same append here usng name in builder filw
		  BufferedWriter br = new BufferedWriter(new FileWriter(filename,true));
			StringBuilder sb = new StringBuilder();
			for (Integer element : ListOfTimes) {
				 sb.append(element.toString());
				 sb.append(",");
				}

				br.write(sb.toString());
				br.close();
				System.out.println(" WriteArrayToFile was called");
	  }
	  
}





