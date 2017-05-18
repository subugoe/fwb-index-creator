package sub.fwb.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

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

	public void clear() {
		try {
			FileUtils.forceDelete(logFile);
			FileUtils.touch(logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void append(String message) {
		PrintStream logOut = null;
		try {
			logOut = new PrintStream(logFile);
			logOut.println(message);
			System.out.println(message);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			close(logOut);
		}
	}

	private void close(PrintStream logOut) {
		if (logOut != null) {
			logOut.close();
		}
	}

}
