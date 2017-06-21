package sub.fwb;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CmdOptions {

	private Options options = new Options();
	public boolean incorrectOptions = false;
	private CommandLine parsedOptions;
	private PrintStream out = System.out;
	private FileAccess fileAccess = new FileAccess();
	public boolean convertToIndexFiles = false;
	public boolean compareTeiAndIndexFiles = false;
	public boolean uploadIndexFiles = false;
	public boolean executeTestSearches = false;
	public boolean swapCores = false;

	public String teiInputDir;
	public String inputExcel;
	public String solrXmlDir;
	public String solrUrl;
	public String core;
	public String swapCore;

	public void initOptions(String[] args) throws UnsupportedEncodingException {
		options.addOption("help", false, "Print help");
		options.addOption("convert", false, "Convert TEIs to Solr XMLs");
		options.addOption("compare", false, "Compare text from TEIs to Solr XMLs");
		options.addOption("upload", false, "Upload Solr XMLs to Solr");
		options.addOption("test", false, "Execute some test searches on the Solr index");
		options.addOption("swap", false, "Swap the given Solr cores");
		options.addOption("teidir", true, "Input directory with TEI files - use with: -convert, -compare");
		options.addOption("excel", true, "File containing sources - use with: -convert");
		options.addOption("solrxmldir", true,
				"Output directory for Solr index files - use with: -convert, -compare, -upload");
		options.addOption("solr", true, "URL of the Solr instance - use with: -upload, -test, -swap");
		options.addOption("core", true, "The target core - use with: -upload, -test, -swap");
		options.addOption("swapcore", true,
				"The core that will be swapped, usually to become the online core - use with: -swap");
		CommandLineParser parser = new DefaultParser();
		try {
			parsedOptions = parser.parse(options, args);
		} catch (ParseException e) {
			out.println("Illegal arguments.");
			out.println();
			printHelp();
			incorrectOptions = true;
			return;
		}

		if (parsedOptions.hasOption("help")) {
			printHelp();
			incorrectOptions = true;
			return;
		}

		convertToIndexFiles = parsedOptions.hasOption("convert");
		if (convertToIndexFiles) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("teidir");
			allRequiredPresent &= parsedOptions.hasOption("excel");
			allRequiredPresent &= parsedOptions.hasOption("solrxmldir");

			if (!allRequiredPresent) {
				out.println("Missing required arguments for converting.");
				incorrectOptions = true;
				return;
			}
			teiInputDir = parsedOptions.getOptionValue("teidir");
			inputExcel = parsedOptions.getOptionValue("excel");
			solrXmlDir = parsedOptions.getOptionValue("solrxmldir");
			fileAccess.makeSureThatExists(new File(solrXmlDir));
		}
		compareTeiAndIndexFiles = parsedOptions.hasOption("compare");
		if (compareTeiAndIndexFiles) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("teidir");
			allRequiredPresent &= parsedOptions.hasOption("solrxmldir");

			if (!allRequiredPresent) {
				out.println("Missing required arguments for comparing.");
				incorrectOptions = true;
				return;
			}
			teiInputDir = parsedOptions.getOptionValue("teidir");
			solrXmlDir = parsedOptions.getOptionValue("solrxmldir");
		}
		uploadIndexFiles = parsedOptions.hasOption("upload");
		if (uploadIndexFiles) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("solrxmldir");
			allRequiredPresent &= parsedOptions.hasOption("solr");
			allRequiredPresent &= parsedOptions.hasOption("core");

			if (!allRequiredPresent) {
				out.println("Missing required arguments for uploading.");
				incorrectOptions = true;
				return;
			}
			solrXmlDir = parsedOptions.getOptionValue("solrxmldir");
			solrUrl = parsedOptions.getOptionValue("solr");
			core = parsedOptions.getOptionValue("core");
		}
		executeTestSearches = parsedOptions.hasOption("test");
		if (executeTestSearches) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("solr");
			allRequiredPresent &= parsedOptions.hasOption("core");

			if (!allRequiredPresent) {
				out.println("Missing required arguments for testing.");
				incorrectOptions = true;
				return;
			}
			solrUrl = parsedOptions.getOptionValue("solr");
			core = parsedOptions.getOptionValue("core");
		}
		swapCores = parsedOptions.hasOption("swap");
		if (swapCores) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("solr");
			allRequiredPresent &= parsedOptions.hasOption("core");
			allRequiredPresent &= parsedOptions.hasOption("swapcore");

			if (!allRequiredPresent) {
				out.println("Missing required arguments for core swapping.");
				incorrectOptions = true;
				return;
			}
			solrUrl = parsedOptions.getOptionValue("solr");
			core = parsedOptions.getOptionValue("core");
			swapCore = parsedOptions.getOptionValue("swapcore");
		}
		if (!convertToIndexFiles && !compareTeiAndIndexFiles && !uploadIndexFiles && !executeTestSearches
				&& !swapCores) {
			printHelp();
			incorrectOptions = true;
			return;
		}
	}

	private void printHelp() throws UnsupportedEncodingException {
		OutputStreamWriter osw = new OutputStreamWriter(out, "UTF8");
		PrintWriter pw = new PrintWriter(osw);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH,
				"java -jar fwb-indexer.jar -convert -compare -upload -test -swap <options> (any combination of -convert and/or -compare and/or -upload and/or -test and/or -swap is possible)",
				"", options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		pw.close();
	}

	// for unit testing
	void setErrorOutput(PrintStream newOut) {
		out = newOut;
	}

}
