package sub.fwb;

import java.io.PrintStream;
import java.util.Date;

public class Main {

	private CmdOptions o = new CmdOptions();
	private Importer importer = new Importer();
	private PrintStream out = System.out;
	private Timer timer = new Timer();

	public static void main(String[] args) throws Exception {
		new Main().execute(args);
	}

	public void execute(String[] args) throws Exception {
		timer.setStart(new Date().getTime());

		importer.setLogOutput(out);
		o.setErrorOutput(out);

		o.initOptions(args);
		if (o.incorrectOptions) {
			return;
		}

		if (o.convertToIndexFiles) {
			importer.convertAll(o.inputExcel, o.teiInputDir, o.solrXmlDir);
		}
		if (o.compareTeiAndIndexFiles) {
			importer.compareAll(o.teiInputDir, o.solrXmlDir);
		}
		if (o.uploadIndexFiles) {
			importer.uploadAll(o.solrXmlDir, o.solrUrl, o.core);
		}
		if (o.executeTestSearches) {
			importer.runTests(o.solrUrl, o.core);
		}

		timer.setStop(new Date().getTime());

		out.println();
		out.println();
		out.println("    " + timer.getDurationMessage());
	}

	// for unit testing
	void setLogOutput(PrintStream newOut) {
		out = newOut;
	}
	void setImporter(Importer newImporter) {
		importer = newImporter;
	}

}
