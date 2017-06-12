package sub.fwb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileAccess {

	public List<File> getAllXmlFilesFromDir(File dir) {
		List<File> xmls = new ArrayList<>();
		File[] children = dir.listFiles();
		for (File child : children) {
			if (child.isFile() && child.getName().endsWith("xml")) {
				xmls.add(child);
			} else if (child.isDirectory()) {
				xmls.addAll(getAllXmlFilesFromDir(child));
			}
		}
		return xmls;
	}

	public OutputStream createOutputStream(File dir, String fileName) throws FileNotFoundException {
		return new FileOutputStream(new File(dir, fileName));
	}

	public void makeSureThatExists(File outputDir) {
		if (!outputDir.exists()) {
			System.out.println("Creating directory: " + outputDir);
			boolean created = outputDir.mkdir();
			if (created) {
				System.out.println(outputDir + " created");
			}
		}
	}

	public void cleanDir(File dir) throws IOException {
		if (dir.exists()) {
			FileUtils.cleanDirectory(dir);
		}
	}

}
