package sub.fwb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import sub.fwb.testing.SolrTester;
import sub.fwb.testing.TeiHtmlComparator;

public class Main {

	private CmdOptions o = new CmdOptions();

	public static void main(String[] args) throws Exception {
		new Main().execute(args);
	}

	public void execute(String[] args) throws Exception {
		long before = new Date().getTime();

		o.initOptions(args);
		if (o.incorrectOptions) {
			return;
		}

		if (o.convertToIndexFiles) {
			convertAll();
		}
		if (o.compareTeiAndIndexFiles) {
			compareAll();
		}
		if (o.uploadIndexFiles) {
			uploadAll();
		}
		if (o.executeTestSearches) {
			runTests();
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

	private void convertAll() throws Exception {
		System.out.println("Converting Excel to index file.");
		SourcesParser sourcesParser = new SourcesParser();
		File sourcesXml = new File(o.solrXmlDir, "0-sources.xml");
		sourcesParser.convertExcelToXml(o.inputExcel, sourcesXml);

		InputStream xsltStream = Main.class.getResourceAsStream("/fwb-indexer.xslt");
		Xslt xslt = new Xslt(xsltStream);

		WordTypesGenerator wordTyper = new WordTypesGenerator();
		InputStream wordTypes = Main.class.getResourceAsStream("/wordtypes.txt");
		String wordTypesList = wordTyper.prepareForXslt(wordTypes);
		xslt.setParameter("wordTypes", wordTypesList);
		InputStream generalWordTypes = Main.class.getResourceAsStream("/wordtypes_general.txt");
		String generalWordTypesList = wordTyper.prepareForXslt(generalWordTypes);
		xslt.setParameter("generalWordTypes", generalWordTypesList);

		ArrayList<File> allFiles = new ArrayList<File>();
		fillListWithFiles(allFiles, o.teiInputDir);
		Collections.sort(allFiles);

		System.out.println("Converting TEIs to index files:");
		int i = 0;
		for (File currentFile : allFiles) {
			int currentId = i + 1;
			if (currentId % 2000 == 0 || currentId == allFiles.size()) {
				System.out.print(" ..." + currentId);
			}
			xslt.setParameter("currentArticleId", currentId + "");
			xslt.transform(currentFile.getAbsolutePath(),
					new FileOutputStream(new File(o.solrXmlDir, currentFile.getName())));
			i++;
		}
	}

	private void compareAll() throws IOException {
		if (o.convertToIndexFiles) {
			System.out.println();
			System.out.println();
		}
		System.out.println("Comparing text from TEIs to HTML text in index files:");
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		ArrayList<File> allFiles = new ArrayList<File>();
		fillListWithFiles(allFiles, o.teiInputDir);
		int i = 0;
		for (File tei : allFiles) {
			i++;
			System.out.print(i + " / " + allFiles.size());
			File solrXml = new File(o.solrXmlDir, tei.getName());
			comparator.compareTexts(tei, solrXml);
			System.out.print("\r");
		}
	}

	private void uploadAll() {
		Uploader uploader = new Uploader(o.solrUrl);
		try {
			File[] xmls = o.solrXmlDir.listFiles();
			uploader.cleanSolr();
			if (o.compareTeiAndIndexFiles || o.convertToIndexFiles) {
				System.out.println();
				System.out.println();
			}
			System.out.println("Reloading the core.");
			uploader.reloadCore();
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

	private void runTests() {
		if (o.compareTeiAndIndexFiles || o.convertToIndexFiles || o.uploadIndexFiles) {
			System.out.println();
			System.out.println();
		}
		System.out.println("Running test queries:");
		System.setProperty("SOLR_URL_FOR_TESTS", o.solrUrl);
		JUnitCore junit = new JUnitCore();
		Result testResult = junit.run(SolrTester.class);
		for (Failure fail : testResult.getFailures()) {
			System.out.println();
			System.out.println("FAILURE in " + fail.getTestHeader() + ": " + fail.getMessage());
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

}
