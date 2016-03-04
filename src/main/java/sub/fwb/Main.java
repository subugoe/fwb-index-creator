package sub.fwb;

import java.io.File;
import java.io.InputStream;
import java.util.TreeSet;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Syntax: java -jar indexer.jar <input-dir> <output-dir>");
		} else {
			File inputDir = new File(args[0]);
			File outputDir = new File(args[1]);
			
			TreeSet<File> allFiles = new TreeSet<File>();
			fillWithFiles(allFiles, inputDir);
			
			InputStream xsltStream = Main.class.getResourceAsStream("/fwb-indexer.xslt");
			Xslt xslt = new Xslt(xsltStream);
			
			//for (File f : )
		}

	}
	
	private static void fillWithFiles(TreeSet<File> allFiles, File currentDir) {
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
