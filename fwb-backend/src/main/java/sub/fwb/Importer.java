package sub.fwb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import sub.fwb.testing.SolrTester;
import sub.fwb.testing.TeiHtmlComparator;

public class Importer {

	private PrintStream out = System.out;
	private SourcesParser sourcesParser = new SourcesParser();
	private WordTypesGenerator wordTyper = new WordTypesGenerator();
	private Xslt xslt = new Xslt();
	private FileAccess fileAccess = new FileAccess();
	private Uploader uploader = new Uploader();
	private CoreSwapper swapper = new CoreSwapper();


	public void setLogOutput(PrintStream newOut) {
		out = newOut;
	}

	public void convertAll(String inputExcel, String teiInputDir, String solrXmlDir) throws Exception {
		fileAccess.cleanDir(new File(solrXmlDir));
		out.println("    Converting Excel to index file.");

		File sourcesXml = new File(new File(solrXmlDir), "0-sources.xml");
		sourcesParser.convertExcelToXml(new File(inputExcel), sourcesXml);

		InputStream xsltStream = Importer.class.getResourceAsStream("/fwb-indexer.xslt");
		xslt.setXsltScript(xsltStream);
		xslt.setErrorOut(out);

		InputStream wordTypes = Importer.class.getResourceAsStream("/wordtypes.txt");
		String wordTypesList = wordTyper.prepareForXslt(wordTypes);
		xslt.setParameter("wordTypes", wordTypesList);
		InputStream generalWordTypes = Importer.class.getResourceAsStream("/wordtypes_general.txt");
		String generalWordTypesList = wordTyper.prepareForXslt(generalWordTypes);
		xslt.setParameter("generalWordTypes", generalWordTypesList);

		InputStream subfacetWordTypes = Importer.class.getResourceAsStream("/wordtypes_subfacet.txt");
		String subfacetWordTypesList = wordTyper.prepareForXslt(subfacetWordTypes);
		xslt.setParameter("subfacetWordTypes", subfacetWordTypesList);

		List<File> allFiles = fileAccess.getAllXmlFilesFromDir(new File(teiInputDir));
		Collections.sort(allFiles);

		out.println("    Converting TEIs to index files:");
		int currentId = 1;
		for (File currentFile : allFiles) {
			printCurrentStatus(currentId, allFiles.size());
			xslt.setParameter("currentArticleId", currentId + "");
			OutputStream fileOs = fileAccess.createOutputStream(new File(solrXmlDir), currentFile.getName());
			xslt.transform(currentFile.getAbsolutePath(), fileOs);
			currentId++;
		}
	}

	public void compareAll(String teiInputDir, String solrXmlDir) throws IOException {
		out.println();
		out.println("    Comparing text from TEIs to HTML text in index files:");
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		List<File> allFiles = fileAccess.getAllXmlFilesFromDir(new File(teiInputDir));
		Collections.sort(allFiles);
		int i = 1;
		for (File tei : allFiles) {
			printCurrentStatus(i, allFiles.size());
			File solrXml = new File(new File(solrXmlDir), tei.getName());
			try {
				comparator.compareTexts(tei, solrXml);
			} catch(AssertionError e) {
				out.println("WARNING " + e.getMessage());
			}
			i++;
		}
	}

	public void uploadAll(String solrXmlDir, String solrUrl, String core) throws IOException {
		uploader.setSolrEndpoint(solrUrl, core);
		try {
			List<File> xmls = fileAccess.getAllXmlFilesFromDir(new File(solrXmlDir));
			out.println();
			out.println("    Cleaning the import core.");
			uploader.cleanSolr();
			out.println("    Reloading the import core.");
			uploader.reloadCore();
			out.println("    Uploading index files:");
			int i = 1;
			for (File x : xmls) {
				printCurrentStatus(i, xmls.size());
				uploader.add(x);
				i++;
			}
			uploader.commitToSolr();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			out.println();
			out.println("Performing a rollback due to errors.");
			uploader.rollbackChanges();
			throw new IOException(e);
		}
	}

	public void runTests(String solrUrl, String core) {
		out.println();
		out.println("    Running test queries.");
		System.setProperty("SOLR_URL_FOR_TESTS", solrUrl);
		System.setProperty("SOLR_CORE_FOR_TESTS", core);
		JUnitCore junit = new JUnitCore();
		Result testResult = junit.run(SolrTester.class);
		for (Failure fail : testResult.getFailures()) {
			out.println();
			out.println("FAILURE in " + fail.getTestHeader() + ": " + fail.getMessage());
		}
	}

	public void swapCores(String solrUrl, String core, String swapCore) throws IOException {
		out.println();
		out.println("    Switching to the online core: " + core + " -> " + swapCore);
		try {
			swapper.setSolrEndpoint(solrUrl, core);
			swapper.switchTo(swapCore);
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	private void printCurrentStatus(int currentNumber, int lastNumber) {
		if (currentNumber % 10000 == 0 || currentNumber == lastNumber) {
			out.println("    ... " + currentNumber);
		}
	}


	// for unit tests
	void setSourcesParser(SourcesParser newParser) {
		sourcesParser = newParser;
	}
	void setWordTyper(WordTypesGenerator newTyper) {
		wordTyper = newTyper;
	}
	void setXslt(Xslt newXslt) {
		xslt = newXslt;
	}
	void setFileAccess(FileAccess newAccess) {
		fileAccess = newAccess;
	}
	void setUploader(Uploader newUploader) {
		uploader = newUploader;
	}
	void setCoreSwapper(CoreSwapper newSwapper) {
		swapper = newSwapper;
	}

}
