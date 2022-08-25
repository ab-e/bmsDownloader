package de.pangaea.bmsdownloader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.pangaea.bmsharvester.BMSDatasets;
import de.pangaea.bmsharvester.XmlArchive;



public class BMSDownloader {
	private static Logger logger; 
	private static String path;
	private static HashMap<String, String> datasetIDmap = new HashMap<String, String>();
	private static HashMap<String, String> providerIDNAMEmap = new HashMap<String, String>();
	private static HashMap<String, String> providerNameShortnameMap = new HashMap<String, String>();
	private static Properties props;
	private static HashMap<String,String> inMap = null;
	private static boolean bNewArchives = false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("./conf/log4j.properties");
		logger = Logger.getLogger(BMSDownloader.class.getName());
		logger.info("Streaming and unpacking ABCD archives...");
		props = ConfReader.readProps("./conf/conf.properties");
		path = props.getProperty("abcd_path");
		getArchiveList();
		downloadABCD();
		if(!bNewArchives) {
			logger.info("No new archives - quitting.");
		}
		logger.info("...done.");
	}
	private static void getArchiveList() {
		
			try {
				URL url = new URL(props.getProperty("bms_url"));
				
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				StringBuilder strBuilder = new StringBuilder();
				String line;
				while ((line = in.readLine()) != null) {
					strBuilder.append(line).append(System.lineSeparator());
				}
				in.close();
				//write json string to file
				FileIO.writeFile("bmsArchives", strBuilder.toString());
			} catch (IOException e) {
				logger.error("Error getting JSON from BMS server: " + e.getMessage());
			}
		   
		
	}
	
	private static void downloadABCD() {
		
		
		//read from local file: last harvested latest archive id
		File file = new File("datasets.txt");
		if(file.exists()) {
			inMap = FileIO.readFile("datasets.txt");
		}else {
			inMap = new HashMap<String, String>();
		}
		
		File parentDir;
		HashMap<String,BMSDatasets> bmsMap = JSONDeserialiser.deserialiseBMS();
		Set<String> bmsMapKeys = bmsMap.keySet();
		Iterator<String> iter = bmsMapKeys.iterator();
		while(iter.hasNext()) {
			BMSDatasets bmsDataset = bmsMap.get(iter.next());
			List<XmlArchive>archivesList = bmsDataset.getXmlArchives();
			
			ListIterator<XmlArchive> listIter = archivesList.listIterator();
			while(listIter.hasNext()) {
				XmlArchive archive = listIter.next();
				if(archive.getLatest()) {
					//only process if latest archive differs from previuos harvest
					if(!inMap.containsKey(bmsDataset.getProviderId().concat("_").concat(bmsDataset.getDatasetId())) 
							|| !archive.getArchiveId().equals(inMap.get(bmsDataset.getProviderId().concat("_").concat(bmsDataset.getDatasetId())))){
								
								//datasetIDmap.put(bmsDataset.getProviderId()+"_"+bmsDataset.getDatasetId(), archive.getArchiveId());
								inMap.put(bmsDataset.getProviderId()+"_"+bmsDataset.getDatasetId(), archive.getArchiveId());
								parentDir = new File(path);
								streamZip(archive.getXmlArchive(), parentDir.getAbsolutePath(), bmsDataset.getProviderId(), bmsDataset.getDatasetId(), archive.getArchiveId() );
								bNewArchives = true;
					}
				}
			}
			providerIDNAMEmap.put(bmsDataset.getProviderId(), bmsDataset.getProviderShortname() +"#"+ bmsDataset.getProviderName());
            providerNameShortnameMap.put(bmsDataset.getProviderShortname(), bmsDataset.getProviderName());
		}
		
		setProviderNameShortnameMap(providerNameShortnameMap);
	    // write id's to file 
	    //FileIO.writeFile("datasets.txt",datasetIDmap);
		FileIO.writeFile("datasets.txt",inMap);
	    FileIO.writeFile("providerNames.txt",providerIDNAMEmap);
		
	}
		

	private static void setProviderNameShortnameMap(HashMap<String, String> providerNameShortnameMap) {
		BMSDownloader.providerNameShortnameMap = providerNameShortnameMap;
	}
	
	private synchronized static boolean streamZip(String archive, String path, String providerID, String datasetID, String archiveID){
		int iCounter = 0;
		boolean bSuccess = true;
		byte[] fileBytes = null;
		try {
			URL url = new URL(archive);
			boolean bReconnect = true;
			ReadableByteChannel rbc = null;
			while(bReconnect) {
				
					try {
						//rbc = Channels.newChannel(url.openStream());
						InputStream ist = url.openStream();
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();

						int nRead;
						byte[] data = new byte[16384];

						while ((nRead = ist.read(data, 0, data.length)) != -1) {
						  buffer.write(data, 0, nRead);
						}
						fileBytes = buffer.toByteArray();
						bReconnect = false;
						ist.close();
						buffer.close();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						iCounter ++;
						if (iCounter < 6) {
							logger.info("(Reconnecting to: "+ archive);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException et) {
								logger.error("Error streaming zip: "+ et.getMessage());
							}
						}else {
							bReconnect = false;
							//iCounter = 0;
							bSuccess = false;
							logger.info("Streaming "+providerID+"_"+datasetID+"_"+archiveID+" not possible, skipping.");
							//datasetIDmap.remove(providerID+"_"+datasetID);
							inMap.remove(providerID+"_"+datasetID);
						}
						
					}
				
			}
			
			if(bSuccess) {
				
				
				//check dir
				File dir = new File(path +"/"+  providerID+"_"+datasetID);
				if(!dir.exists()) {
					dir.mkdir();
				}
				
				//unzip on the fly w/o wtriting archive to disk 
				Unzipper.unzip(dir.getAbsolutePath(), archiveID, fileBytes);
	
			}
		
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bSuccess;
	}
}
