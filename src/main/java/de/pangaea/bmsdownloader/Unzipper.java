package de.pangaea.bmsdownloader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

public class Unzipper {
	private static Logger logger; 
	
    public synchronized static void unzip(String destDirectory, String archiveID, byte[] zipBytes) throws IOException {
    	logger = Logger.getLogger(Unzipper.class.getName());
    	logger.info("Unzipping: "+destDirectory+"/"+archiveID+".zip");
    	
    	ByteArrayInputStream byteStream = new ByteArrayInputStream(zipBytes);
    	ZipInputStream zipStream = new ZipInputStream(byteStream);
    	
    	
    	final File destDir = new File(destDirectory);
        final byte[] buffer = new byte[4096];
        final ZipInputStream zis = new ZipInputStream(byteStream);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            final File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                final FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        
        zis.closeEntry();
        zis.close();
        byteStream.close();
        zipStream.close();
    }

   
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
