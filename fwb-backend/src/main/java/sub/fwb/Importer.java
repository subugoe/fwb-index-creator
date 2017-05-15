package sub.fwb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import sub.fwb.testing.SolrTester;
import sub.fwb.testing.TeiHtmlComparator;

public class Importer {

	public void convertAll(File solrXmlDir, File inputExcel, File teiInputDir) throws Exception {
		System.out.println("Converting Excel to index file.");
		SourcesParser sourcesParser = new SourcesParser();
		File sourcesXml = new File(solrXmlDir, "0-sources.xml");
		sourcesParser.convertExcelToXml(inputExcel, sourcesXml);

		InputStream xsltStream = Importer.class.getResourceAsStream("/fwb-indexer.xslt");
		Xslt xslt = new Xslt(xsltStream);

		WordTypesGenerator wordTyper = new WordTypesGenerator();
		InputStream wordTypes = Importer.class.getResourceAsStream("/wordtypes.txt");
		String wordTypesList = wordTyper.prepareForXslt(wordTypes);
		xslt.setParameter("wordTypes", wordTypesList);
		InputStream generalWordTypes = Importer.class.getResourceAsStream("/wordtypes_general.txt");
		String generalWordTypesList = wordTyper.prepareForXslt(generalWordTypes);
		xslt.setParameter("generalWordTypes", generalWordTypesList);

		InputStream subfacetWordTypes = Importer.class.getResourceAsStream("/wordtypes_subfacet.txt");
		String subfacetWordTypesList = wordTyper.prepareForXslt(subfacetWordTypes);
		xslt.setParameter("subfacetWordTypes", subfacetWordTypesList);

		ArrayList<File> allFiles = new ArrayList<File>();
		fillListWithFiles(allFiles, teiInputDir);
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
					new FileOutputStream(new File(solrXmlDir, currentFile.getName())));
			i++;
		}
	}

	public void compareAll(boolean convertToIndexFiles, File teiInputDir, File solrXmlDir) throws IOException {
		if (convertToIndexFiles) {
			System.out.println();
			System.out.println();
		}
		System.out.println("Comparing text from TEIs to HTML text in index files:");
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

	public void uploadAll(String solrUrl, File solrXmlDir, boolean compareTeiAndIndexFiles, boolean convertToIndexFiles) {
		Uploader uploader = new Uploader(solrUrl);
		try {
			File[] xmls = solrXmlDir.listFiles();
			uploader.cleanSolr();
			if (compareTeiAndIndexFiles || convertToIndexFiles) {
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

	public void runTests(boolean compareTeiAndIndexFiles, boolean convertToIndexFiles, boolean uploadIndexFiles, String solrUrl) {
		if (compareTeiAndIndexFiles || convertToIndexFiles || uploadIndexFiles) {
			System.out.println();
			System.out.println();
		}
		System.out.println("Running test queries:");
		System.setProperty("SOLR_URL_FOR_TESTS", solrUrl);
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
