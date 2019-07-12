package lN_simple3;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PrintDCsContacted {
 

 
 public static void printDcsCont(String Filename , ArrayList details) throws IOException {
 
//the details coming in are cognate or not, timesinceentered, number of DCs contacted	 
	 
ArrayList<Integer>DetailsPerCell = details;

String fileName =  Filename;

FileWriter pw = new FileWriter(fileName,true); // if this is set at true just write to the end of it. 
StringBuilder sb = new StringBuilder();
for (Integer element : DetailsPerCell) {
sb.append(element.toString()); 
sb.append(","); 
 
}

pw.write(sb.toString());
pw.append('\n');
pw.flush();

pw.close();

 }
 
}
