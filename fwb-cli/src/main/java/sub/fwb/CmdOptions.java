package sub.fwb;

import java.io.File;
import java.io.OutputStreamWriter;
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
	public boolean convertToIndexFiles = false;
	public boolean compareTeiAndIndexFiles = false;
	public boolean uploadIndexFiles = false;
	public boolean executeTestSearches = false;

	public String teiInputDir;
	public String inputExcel;
	public String solrXmlDir;
	public String solrUrl;

	public void initOptions(String[] args) throws UnsupportedEncodingException {
		options.addOption("help", false, "Print help");
		options.addOption("convert", false, "Convert TEIs to Solr XMLs");
		options.addOption("compare", false, "Compare text from TEIs to Solr XMLs");
		options.addOption("upload", false, "Upload Solr XMLs to Solr");
		options.addOption("test", false, "Execute some test searches on the Solr index");
		options.addOption("teidir", true, "Input directory with TEI files - use with: -convert, -compare");
		options.addOption("excel", true, "File containing sources - use with: -convert");
		options.addOption("solrxmldir", true,
				"Output directory for Solr index files - use with: -convert, -compare, -upload");
		options.addOption("solr", true, "URL of the Solr core - use with: -upload, -test");
		CommandLineParser parser = new DefaultParser();
		try {
			parsedOptions = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Illegal arguments.");
			System.out.println();
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
				System.out.println("Missing required arguments for converting.");
				incorrectOptions = true;
				return;
			}
			teiInputDir = parsedOptions.getOptionValue("teidir");
			inputExcel = parsedOptions.getOptionValue("excel");
			solrXmlDir = parsedOptions.getOptionValue("solrxmldir");
			makeSureThatExists(new File(solrXmlDir));
		}
		compareTeiAndIndexFiles = parsedOptions.hasOption("compare");
		if (compareTeiAndIndexFiles) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("teidir");
			allRequiredPresent &= parsedOptions.hasOption("solrxmldir");

			if (!allRequiredPresent) {
				System.out.println("Missing required arguments for comparing.");
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

			if (!allRequiredPresent) {
				System.out.println("Missing required arguments for uploading.");
				incorrectOptions = true;
				return;
			}
			solrXmlDir = parsedOptions.getOptionValue("solrxmldir");
			solrUrl = parsedOptions.getOptionValue("solr");
		}
		executeTestSearches = parsedOptions.hasOption("test");
		if (executeTestSearches) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("solr");

			if (!allRequiredPresent) {
				System.out.println("Missing required arguments for testing.");
				incorrectOptions = true;
				return;
			}
			solrUrl = parsedOptions.getOptionValue("solr");
		}
		if (!convertToIndexFiles && !compareTeiAndIndexFiles && !uploadIndexFiles && !executeTestSearches) {
			printHelp();
			incorrectOptions = true;
			return;
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

	private void printHelp() throws UnsupportedEncodingException {
		OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF8");
		PrintWriter pw = new PrintWriter(osw);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH,
				"java -jar fwb-indexer.jar -convert -compare -upload -test <options> (any combination of -convert and/or -compare and/or -upload and/or -test is possible)",
				"", options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		pw.close();
	}

}
