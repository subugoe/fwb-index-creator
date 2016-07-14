package sub.fwb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.solr.client.solrj.SolrServerException;

public class Main {

	private Options options = new Options();
	private boolean terminated = false;
	private CommandLine parsedOptions;
	private boolean convertToIndexFiles = false;
	private boolean compareTeiAndIndexFiles = false;
	private boolean uploadIndexFiles = false;

	private File teiInputDir;
	private File inputExcel;
	private File inputWordTypes;
	private File solrXmlDir;
	private String solrUrl;

	public static void main(String[] args) throws Exception {
		new Main().execute(args);
	}

	public void execute(String[] args) throws Exception {
		long before = new Date().getTime();

		initOptions(args);
		if (terminated) {
			return;
		}

		if (convertToIndexFiles) {
			teiInputDir = new File(parsedOptions.getOptionValue("teidir"));
			inputExcel = new File(parsedOptions.getOptionValue("excel"));
			inputWordTypes = new File(parsedOptions.getOptionValue("wordtypes"));
			solrXmlDir = new File(parsedOptions.getOptionValue("solrxmldir"));
			makeSureThatExists(solrXmlDir);
			convertAll();
		}
		if (compareTeiAndIndexFiles) {
			teiInputDir = new File(parsedOptions.getOptionValue("teidir"));
			solrXmlDir = new File(parsedOptions.getOptionValue("solrxmldir"));
			compareAll();
		}
		if (uploadIndexFiles) {
			solrXmlDir = new File(parsedOptions.getOptionValue("solrxmldir"));
			solrUrl = parsedOptions.getOptionValue("solr");
			uploadAll();
		}

		long after = new Date().getTime();
		long millis = after - before;
		long minutes = millis / 1000 / 60;
		long seconds = (millis - minutes * 60 * 1000) / 1000;
		System.out.println();
		System.out.println();
		System.out.println("Took " + minutes + ":" + seconds + " minutes (" + millis + " milliseconds)");
	}

	private void initOptions(String[] args) throws UnsupportedEncodingException {
		options.addOption("help", false, "Print help");
		options.addOption("convert", false, "Convert TEIs to Solr XMLs");
		options.addOption("compare", false, "Compare text from TEIs to Solr XMLs");
		options.addOption("upload", false, "Upload Solr XMLs to Solr");
		options.addOption("teidir", true, "Input directory with TEI files - use with: -convert, -compare");
		options.addOption("excel", true, "File containing sources - use with: -convert");
		options.addOption("wordtypes", true, "Text file containing word type mappings - use with: -convert");
		options.addOption("solrxmldir", true,
				"Output directory for Solr index files - use with: -convert, -compare, -upload");
		options.addOption("solr", true, "URL of the Solr core - use with: -upload");
		CommandLineParser parser = new DefaultParser();
		try {
			parsedOptions = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Illegal arguments.");
			System.out.println();
			printHelp();
			terminated = true;
			return;
		}

		if (parsedOptions.hasOption("help")) {
			printHelp();
			terminated = true;
			return;
		}

		convertToIndexFiles = parsedOptions.hasOption("convert");
		if (convertToIndexFiles) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("teidir");
			allRequiredPresent &= parsedOptions.hasOption("excel");
			allRequiredPresent &= parsedOptions.hasOption("wordtypes");
			allRequiredPresent &= parsedOptions.hasOption("solrxmldir");

			if (!allRequiredPresent) {
				System.out.println("Missing required arguments for converting.");
				terminated = true;
			}
		}
		compareTeiAndIndexFiles = parsedOptions.hasOption("compare");
		if (compareTeiAndIndexFiles) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("teidir");
			allRequiredPresent &= parsedOptions.hasOption("solrxmldir");

			if (!allRequiredPresent) {
				System.out.println("Missing required arguments for comparing.");
				terminated = true;
			}
		}
		uploadIndexFiles = parsedOptions.hasOption("upload");
		if (uploadIndexFiles) {
			boolean allRequiredPresent = true;
			allRequiredPresent &= parsedOptions.hasOption("solrxmldir");
			allRequiredPresent &= parsedOptions.hasOption("solr");

			if (!allRequiredPresent) {
				System.out.println("Missing required arguments for uploading.");
				terminated = true;
			}
		}
		if (!convertToIndexFiles && !compareTeiAndIndexFiles && !uploadIndexFiles) {
			printHelp();
			terminated = true;
		}
	}

	private void makeSureThatExists(File outputDir) {
		if (!outputDir.exists()) {
			System.out.println("Creating directory: " + outputDir);
			boolean result = false;

			try {
				outputDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				// handle it
			}
			if (result) {
				System.out.println(outputDir + " created");
			}
		}
	}

	private void convertAll() throws Exception {
		SourcesParser sourcesParser = new SourcesParser();
		File sourcesXml = new File(solrXmlDir, "0-sources.xml");
		sourcesParser.convertExcelToXml(inputExcel, sourcesXml);

		InputStream xsltStream = Main.class.getResourceAsStream("/fwb-indexer.xslt");
		Xslt xslt = new Xslt(xsltStream);

		WordTypesGenerator wordTyper = new WordTypesGenerator();
		String wordTypesList = wordTyper.prepareForXslt(inputWordTypes);
		xslt.setParameter("wordTypes", wordTypesList);

		ArrayList<File> allFiles = new ArrayList<File>();
		fillListWithFiles(allFiles, teiInputDir);
		Collections.sort(allFiles);

		System.out.println("Generating index files:");
		int i = 0;
		for (File currentFile : allFiles) {
			int currentId = i + 1;
			if (currentId % 2000 == 0) {
				System.out.print(" ..." + currentId);
			}
			if (i > 0) {
				addFileParametersToXslt("previous", (currentId - 1) + "", allFiles.get(i - 1), xslt);
			}
			if (i < allFiles.size() - 1) {
				addFileParametersToXslt("next", (currentId + 1) + "", allFiles.get(i + 1), xslt);
			} else {
				xslt.setParameter("nextArticleId", "");
				xslt.setParameter("nextLemma", "");
			}
			xslt.setParameter("currentArticleId", currentId + "");
			xslt.transform(currentFile.getAbsolutePath(),
					new FileOutputStream(new File(solrXmlDir, currentFile.getName())));
			i++;
		}
	}

	private void compareAll() throws IOException {
		if (convertToIndexFiles) {
			System.out.println();
			System.out.println();
		}
		System.out.println("Comparing text from TEIs to index files:");
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		ArrayList<File> allFiles = new ArrayList<File>();
		fillListWithFiles(allFiles, teiInputDir);
		int i = 0;
		for (File tei : allFiles) {
			i++;
			System.out.print(i + " / " + allFiles.size());
			File solrXml = new File(solrXmlDir, tei.getName());
			comparator.compareTexts(tei, solrXml);
			System.out.print("\r");
		}
	}

	private void uploadAll() {
		Uploader uploader = new Uploader(solrUrl);
		try {
			File[] xmls = solrXmlDir.listFiles();
			uploader.cleanSolr();
			if (compareTeiAndIndexFiles || convertToIndexFiles) {
				System.out.println();
				System.out.println();
			}
			System.out.println("Uploading documents:");
			for (File x : xmls) {
				uploader.add(x);
			}
			uploader.commitToSolr();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			uploader.rollbackChanges();
		}
	}

	private void fillListWithFiles(ArrayList<File> allFiles, File currentDir) {
		File[] currentDirChildren = currentDir.listFiles();
		for (File child : currentDirChildren) {
			if (child.isFile() && child.getName().endsWith("xml")) {
				allFiles.add(child);
			} else if (child.isDirectory()) {
				fillListWithFiles(allFiles, child);
			}
		}
	}

	private void addFileParametersToXslt(String previousOrNext, String id, File file, Xslt xslt) {
		xslt.setParameter(previousOrNext + "ArticleId", id);

		String fileName = file.getName();
		int indexOfFirstDot = fileName.indexOf('.');
		String lemma = fileName.substring(0, indexOfFirstDot);
		xslt.setParameter(previousOrNext + "Lemma", lemma);
	}

	private void printHelp() throws UnsupportedEncodingException {
		OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF8");
		PrintWriter pw = new PrintWriter(osw);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH,
				"java -jar fwb-indexer.jar -convert -compare -upload <options> (any combination of -convert and/or -compare and/or -upload is possible)",
				"", options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		pw.close();
	}

}
