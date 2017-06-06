package sub.fwb.web;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

import sub.fwb.FileAccess;
import sub.fwb.Importer;
import sub.fwb.Timer;

public class ImporterRunner implements Runnable {

	private Importer importer = new Importer();
	private LogAccess logAccess = new LogAccess();
	private Environment env = new Environment();
	private LockFile lock = new LockFile();
	private FileAccess fileAccess = new FileAccess();
	private Timer timer = new Timer();

	@Override
	public void run() {
		timer.setStart(new Date().getTime());
		logAccess.clear();
		PrintStream log = logAccess.getOutput();

		log.println("Starting import (" + new Date() + ")");
		log.println();
		try {
			importer.setLogOutput(log);
			importer.convertAll(inputExcel(), teiInputDir(), solrXmlDir());
			importer.compareAll(teiInputDir(), solrXmlDir());
			importer.uploadAll(solrXmlDir(), solrUrl());
			importer.runTests(solrUrl());
		} catch (Exception e) {
			log.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		} finally {
			lock.delete();
			timer.setStop(new Date().getTime());
			log.println();
			log.println(timer.getDurationMessage());
			log.close();
		}
	}

	private String inputExcel() {
		File gitDir = new File(env.getVariable("GIT_DIR"));
		return new File(gitDir, "FWB-Quellenliste.xlsx").getAbsolutePath();
	}

	private String teiInputDir() {
		File gitDir = new File(env.getVariable("GIT_DIR"));
		return gitDir.getAbsolutePath();
	}

	private String solrXmlDir() {
		File outputDir = new File(env.getVariable("OUTPUT_DIR"));
		String solrXmlDir = new File(outputDir, "solrxml").getAbsolutePath();
		fileAccess.makeSureThatExists(new File(solrXmlDir));
		return solrXmlDir;
	}

	private String solrUrl() {
		return env.getVariable("SOLR_STAGING_URL");
	}


	// for unit tests
	void setImporter(Importer newImporter) {
		importer = newImporter;
	}
	void setLogAccess(LogAccess newLog) {
		logAccess = newLog;
	}
	void setEnv(Environment newEnv) {
		env = newEnv;
	}
	void setLockFile(LockFile newLock) {
		lock = newLock;
	}
	void setFileAccess(FileAccess newAccess) {
		fileAccess = newAccess;
	}
	void setTimer(Timer newTimer) {
		timer = newTimer;
	}

}
