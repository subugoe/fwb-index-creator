package sub.fwb.web;

import java.io.File;
import java.io.PrintStream;

import sub.fwb.Importer;

public class ImporterRunner implements Runnable {

	private Importer importer = new Importer();
	private LogAccess logAccess = new LogAccess();
	private Environment env = new Environment();
	private LockFile lock = new LockFile();

	@Override
	public void run() {
		File outputDir = new File(env.getVariable("OUTPUT_DIR"));
		String solrXmlDir = new File(outputDir, "solrxml").getAbsolutePath();
		makeSureThatExists(new File(solrXmlDir));
		
		File gitDir = new File(env.getVariable("GIT_DIR"));
		String inputExcel = new File(gitDir, "FWB-Quellenliste.xlsx").getAbsolutePath();
		String teiInputDir = gitDir.getAbsolutePath();
		
		String solrUrl = env.getVariable("SOLR_STAGING_URL");
		PrintStream logStream = null;
		try {
			logAccess.clear();
			File logFile = new File(outputDir, "log.txt");
			logStream = new PrintStream(logFile);
			importer.setLogOutput(logStream);
			importer.convertAll(inputExcel, teiInputDir, solrXmlDir);
			importer.compareAll(teiInputDir, solrXmlDir);
			importer.uploadAll(solrXmlDir, solrUrl);
			importer.runTests(solrUrl);
			System.out.println("Finished.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (logStream != null) {
				logStream.close();
			}
			lock.delete();
		}
		
	}

	private void makeSureThatExists(File dir) {
		if (!dir.exists()) {
			System.out.println("Creating directory: " + dir);
			boolean created = dir.mkdir();
			if (created) {
				System.out.println(dir + " created");
			}
		}
	}

}
