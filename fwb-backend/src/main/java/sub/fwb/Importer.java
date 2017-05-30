package sub.fwb;

import java.io.File;
import java.io.FileOutputStream;
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


	public void setLogOutput(PrintStream newOut) {
		out = newOut;
	}

	public void convertAll(String inputExcel, String teiInputDir, String solrXmlDir) throws Exception {
		out.println("Converting Excel to index file.");

		File sourcesXml = new File(new File(solrXmlDir), "0-sources.xml");
		sourcesParser.convertExcelToXml(new File(inputExcel), sourcesXml);

		InputStream xsltStream = Importer.class.getResourceAsStream("/fwb-indexer.xslt");
		xslt.setXsltScript(xsltStream);

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

		out.println("Converting TEIs to index files:");
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
		out.println("Comparing text from TEIs to HTML text in index files:");
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		List<File> allFiles = fileAccess.getAllXmlFilesFromDir(new File(teiInputDir));
		Collections.sort(allFiles);
		int i = 1;
		for (File tei : allFiles) {
			printCurrentStatus(i, allFiles.size());
			File solrXml = new File(new File(solrXmlDir), tei.getName());
			comparator.compareTexts(tei, solrXml);
			i++;
		}
	}

	public void uploadAll(String solrXmlDir, String solrUrl) {
		uploader.setSolrUrl(solrUrl);
		try {
			File[] xmls = new File(solrXmlDir).listFiles();
			uploader.cleanSolr();
			out.println();
			out.println("Reloading the core.");
			uploader.reloadCore();
			out.println("Uploading documents:");
			int i = 1;
			for (File x : xmls) {
				printCurrentStatus(i, xmls.length);
				uploader.add(x);
				i++;
			}
			uploader.commitToSolr();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			out.println();
			out.println(e.getMessage());
			out.println("Performing a rollback due to errors.");
			uploader.rollbackChanges();
		}
	}

	public void runTests(String solrUrl) {
		out.println();
		out.println("Running test queries.");
		System.setProperty("SOLR_URL_FOR_TESTS", solrUrl);
		JUnitCore junit = new JUnitCore();
		Result testResult = junit.run(SolrTester.class);
		for (Failure fail : testResult.getFailures()) {
			out.println();
			out.println("FAILURE in " + fail.getTestHeader() + ": " + fail.getMessage());
		}
	}

	private void printCurrentStatus(int currentNumber, int lastNumber) {
		if (currentNumber % 10000 == 0 || currentNumber == lastNumber) {
			out.println(" ..." + currentNumber);
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

}
