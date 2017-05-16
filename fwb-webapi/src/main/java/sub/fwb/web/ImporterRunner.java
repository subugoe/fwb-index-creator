package sub.fwb.web;

import java.io.File;

import sub.fwb.Importer;

public class ImporterRunner implements Runnable {

	private Importer importer = new Importer();
	private LogAccess logAccess = new LogAccess();
	private Environment env = new Environment();

	@Override
	public void run() {
		File outputDir = new File(env.getVariable("OUTPUT_DIR"));
		File solrXmlDir = new File(outputDir, "solrxml");
		makeSureThatExists(solrXmlDir);
		
		File gitDir = new File(env.getVariable("GIT_DIR"));
		File inputExcel = new File(gitDir, "FWB-Quellenliste.xlsx");
		File teiInputDir = gitDir;
		
		String solrUrl = env.getVariable("SOLR_STAGING_URL");
		boolean compareTeiAndIndexFiles = true;
		boolean convertToIndexFiles = true;
		boolean uploadIndexFiles = true;
		try {
			importer.convertAll(solrXmlDir, inputExcel, teiInputDir);
			importer.compareAll(convertToIndexFiles, teiInputDir, solrXmlDir);
			importer.uploadAll(solrUrl, solrXmlDir, compareTeiAndIndexFiles, convertToIndexFiles);
			importer.runTests(compareTeiAndIndexFiles, convertToIndexFiles, uploadIndexFiles, solrUrl);
			System.out.println("Finished.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void makeSureThatExists(File outputDir) {
		if (!outputDir.exists()) {
			System.out.println("Creating directory: " + outputDir);
			boolean created = outputDir.mkdir();
			if (created) {
				System.out.println(outputDir + " created");
			}
		}
	}

}
