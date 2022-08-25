package de.pangaea.bmsdownloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;



public class FileIO {

	public static String readFileForString(String fileName) {
		String xmlString = "";
		
		ArrayList<String> list = new ArrayList<String>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while (in.ready()) {
			  String s = in.readLine();
			  if(s.length() == 0 || s.contains("dislink") || s.contains("?xml")) {
				  
			  }
			  else{
				  
				  xmlString = xmlString + s + System.lineSeparator();
			  }
			  
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return xmlString;

	}
	
	
	public static HashMap<String, String> readFile(String fileName) {
		//ArrayList<String> list = new ArrayList<String>();
		HashMap<String, String> map = new HashMap<String, String>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while (in.ready()) {
			  String s = in.readLine();
			  map.put(s.split("=")[0], s.split("=")[1]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return map;

	}
	
	public static StringBuffer readFileForBuffer(String fileName) {
		StringBuffer buffer = new StringBuffer();

		File file = new File(fileName);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		char[] buf = new char[1024];
		int numRead = 0;
		try {
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				buffer.append(readData);

				buf = new char[1024];
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return buffer;

	}
	
	/**
	 * method is used for debugging request/resonse pairs
	 * @param data
	 */
	public static synchronized void writeFile(String doi, String data) {

	
		File f=new File(doi+".json");
		//File f=new File(Starter.responseFileLoc+"antares_"+formatter.format( new Date() )+".xml");
		
		Writer fstream = null;
		BufferedWriter out = null;
			
		try {
			fstream = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
			out = new BufferedWriter(fstream);
			out.write(data);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void writeFile(String filename, HashMap<String, String> datasetIDmap) {

		
		File f=new File(filename);
		//File f=new File(Starter.responseFileLoc+"antares_"+formatter.format( new Date() )+".xml");
		
		
		try {
			FileWriter fWriter = new FileWriter(f);
			Set<String> kset = datasetIDmap.keySet();
			Iterator<String> iter = kset.iterator();
			String key = "";
 			while(iter.hasNext()) {
 				key = iter.next();
				fWriter.write(key.concat("=").concat(datasetIDmap.get(key))+System.lineSeparator());
			}
			
			fWriter.flush();
			fWriter.close();
		} catch (IOException e) {
			System.out.println("Error writing xml response: "+e.getMessage());
		}
	}
	
	public static void append2File(String in){
		File file =new File("basis_for_event.txt");

    	   try {
			file.createNewFile();
	    	FileWriter fw = new FileWriter(file,true);
	    	BufferedWriter bw = new BufferedWriter(fw);
	    	bw.write(in);
	    	bw.write("\n");
	    	bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	   
    	}
	public static void append2ErrorFile(String in){
		File file =new File("error.txt");

    	   try {
			file.createNewFile();
	    	FileWriter fw = new FileWriter(file,true);
	    	BufferedWriter bw = new BufferedWriter(fw);
	    	bw.write(in);
	    	bw.write("\n");
	    	bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	   
    	}

    	
	
	
	
}
