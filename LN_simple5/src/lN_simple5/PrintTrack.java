package lN_simple5;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PrintTrack {
 
 public PrintTrack(){}
 
 public static void printCoordinates(String Filename ,int i, ArrayList celltracks) throws IOException {
 
	 
	 
 ArrayList<Double>ListOfTimes = celltracks;
 String istring = Integer.toString(i);
String fileName =  Constants.FILE_NAME0 + "_Tcell_"+ istring+".csv";

 
 FileWriter pw = new FileWriter(fileName,true);
StringBuilder sb = new StringBuilder();
for (Double element : ListOfTimes) {
sb.append(element.toString());
sb.append(","); 
 
}

pw.write(sb.toString());
pw.append('\n');
pw.flush();

pw.close();

 }
 
}
