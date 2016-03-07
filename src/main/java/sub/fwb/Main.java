package sub.fwb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Main {

	public static void main(String[] args) throws Exception {
		
		
		if (args.length != 2) {
			System.out.println("Syntax: java -jar indexer.jar <input-dir> <output-dir>");
		} else {
			File inputDir = new File(args[0]);
			File outputDir = new File(args[1]);
			
			InputStream xsltStream = Main.class.getResourceAsStream("/fwb-indexer.xslt");
			Xslt xslt = new Xslt(xsltStream);
			
			
			long before = new Date().getTime();

			ArrayList<File> allFiles = new ArrayList<File>();
			fillWithFiles(allFiles, inputDir);
			Collections.sort(allFiles);
			
			int i = 0;
			for (File currentFile : allFiles) {
				File previousFile = i>0 ? allFiles.get(i-1) : null;
				File nextFile = i<allFiles.size()-1 ? allFiles.get(i+1) : null;
				xslt.setParameter("currentArticleId", "" + (i+1));
				xslt.transform(currentFile.getAbsolutePath(), new FileOutputStream(new File(outputDir, ""+(i+1)+".xml")));
				i++;
			}
			
			long after = new Date().getTime();
			System.out.println("Took " + (after - before) + " milliseconds");
			
		}

	}
	
	private static void fillWithFiles(ArrayList<File> allFiles, File currentDir) {
		File[] currentDirChildren = currentDir.listFiles();
		for (File child : currentDirChildren) {
			if (child.isFile() && child.getName().endsWith("xml")) {
				allFiles.add(child);
			} else if (child.isDirectory()) {
				fillWithFiles(allFiles, child);
			}
		}

	}

}
