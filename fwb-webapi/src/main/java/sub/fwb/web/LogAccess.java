package sub.fwb.web;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class LogAccess {

	private File logFile;

	public LogAccess() {
		File outputDir = new File(new Environment().getVariable("OUTPUT_DIR"));
		logFile = new File(outputDir, "log.txt");
		try {
			if (!logFile.exists()) {
				FileUtils.writeStringToFile(logFile, "No logs yet");
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not write log file: ", e);
		}
	}

	public String getLogContents() {
		try {
			return FileUtils.readFileToString(logFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "Could not read log file: " + e.getMessage();
		}
	}
}
