package lN_simple5;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
public class printdata {//public class AppendToFileExample

//note to call this you need to do 
	//printdata.printer(transitTime)
	
	public printdata(){}

    public static void printer1(int celltransittime)throws IOException 
	    {
    	
    
		int data = celltransittime;
	
		String filename = Constants.FILE_NAME1;
		//System.out.println(filename);
		//File file =new File(filename);

    	 FileWriter pw = new FileWriter(filename,true);
    //System.out.println(cell.toString()+"\n");
		            
	       pw.append(String.valueOf(data));
		 //  pw.append(",");
	  //  pw.append(String.valueOf(cell.getAge()));
		            pw.append("\n");
		        
		            pw.flush();
		            pw.close();
		  
		
	  }
    
    public static void printer2(int celltransittime)throws IOException 
    {
	int data = celltransittime;
	String filename = Constants.FILE_NAME2;
	 FileWriter pw = new FileWriter(filename,true);           
       pw.append(String.valueOf(data));
	            pw.append("\n");        
	            pw.flush();
	            pw.close();	 
  }
    public static void printer3(int celltransittime)throws IOException 
    {
	int data = celltransittime;
	String filename = Constants.FILE_NAME3;
	 FileWriter pw = new FileWriter(filename,true);           
       pw.append(String.valueOf(data));
	            pw.append("\n");        
	            pw.flush();
	            pw.close();	 
  }  
    public static void printer4(int celltransittime)throws IOException 
    {
	int data = celltransittime;
	String filename = Constants.FILE_NAME4;
	 FileWriter pw = new FileWriter(filename,true);           
       pw.append(String.valueOf(data));
	            pw.append("\n");        
	            pw.flush();
	            pw.close();	 
  }
    public static void printer5(int celltransittime)throws IOException 
    {
	int data = celltransittime;
	String filename = Constants.FILE_NAME5;
	 FileWriter pw = new FileWriter(filename,true);           
       pw.append(String.valueOf(data));
	            pw.append("\n");        
	            pw.flush();
	            pw.close();	 
  }
    public static void printer6(int celltransittime)throws IOException 
    {
	int data = celltransittime;
	String filename = Constants.FILE_NAME6;
	 FileWriter pw = new FileWriter(filename,true);           
       pw.append(String.valueOf(data));
	            pw.append("\n");        
	            pw.flush();
	            pw.close();	 
  }
    
    		
	}
	
	
	
