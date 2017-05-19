package sub.fwb.web;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

public class LockFile {

	private Environment env = new Environment();
	private File lockFilePath;

	public LockFile() {
		File outputDir = new File(env.getVariable("OUTPUT_DIR"));
		lockFilePath = new File(outputDir, "lock");
	}

	public void create() throws IOException {
		FileUtils.writeStringToFile(lockFilePath, new Date().toString());
	}

	public boolean exists() {
		return lockFilePath.exists();
	}

	public void delete() {
		try {
			FileUtils.forceDelete(lockFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
