package sub.fwb;

import java.util.Date;

public class Main {

	private CmdOptions o = new CmdOptions();
	private Importer importer = new Importer();

	public static void main(String[] args) throws Exception {
		new Main().execute(args);
	}

	public void execute(String[] args) throws Exception {
		long before = new Date().getTime();

		importer.setLogOutput(System.out);

		o.initOptions(args);
		if (o.incorrectOptions) {
			return;
		}

		if (o.convertToIndexFiles) {
			importer.convertAll(o.inputExcel, o.teiInputDir, o.solrXmlDir);
		}
		if (o.compareTeiAndIndexFiles) {
			importer.compareAll(o.teiInputDir, o.solrXmlDir, o.convertToIndexFiles);
		}
		if (o.uploadIndexFiles) {
			importer.uploadAll(o.solrXmlDir, o.solrUrl, o.convertToIndexFiles, o.compareTeiAndIndexFiles);
		}
		if (o.executeTestSearches) {
			importer.runTests(o.solrUrl, o.convertToIndexFiles, o.compareTeiAndIndexFiles, o.uploadIndexFiles);
		}

		long after = new Date().getTime();
		long millis = after - before;
		long minutes = millis / 1000 / 60;
		long seconds = (millis - minutes * 60 * 1000) / 1000;
		String potentialZero = seconds < 10 ? "0" : "";

		System.out.println();
		System.out.println();
		System.out
				.println("Took " + minutes + ":" + potentialZero + seconds + " minutes (" + millis + " milliseconds)");
	}

}
