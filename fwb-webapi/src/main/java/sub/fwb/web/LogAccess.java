package sub.fwb.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;

public class LogAccess {

	private File logFile;

	private void init() {
		File outputDir = new File(new Environment().getVariable("OUTPUT_DIR"));
		logFile = new File(outputDir, "log.txt");
	}

	public String getLogContents() {
		init();
		try {
			if (!logFile.exists()) {
				FileUtils.writeStringToFile(logFile, "No logs yet");
			}
			return FileUtils.readFileToString(logFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "Could not access log file: " + e.getMessage();
		}
	}

	public String getStatusOfLastLog() {
		String log = getLogContents();
		if (log.contains("ERROR")) {
			return "ERROR";
		} else if (log.contains("WARNING")) {
			return "WARNING";
		} else if (log.contains("Took") && log.contains("minutes")){
			return "SUCCESS";
		} else {
			return "UNKNOWN";
		}
	}

	public void clear() {
		init();
		try {
			FileUtils.forceDelete(logFile);
			FileUtils.touch(logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PrintStream getOutput() {
		init();
		PrintStream logOut = null;
		try {
			logOut = new PrintStream(logFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return logOut;
	}

	public void append(String message) {
		init();
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
