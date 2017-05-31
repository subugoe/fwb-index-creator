package sub.fwb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

}
