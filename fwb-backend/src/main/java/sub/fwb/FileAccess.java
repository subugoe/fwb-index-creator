package sub.fwb;

import java.io.File;
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
}
