package sub.fwb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertEquals;

public class TeiHtmlComparator {

	public static void main(String[] args) throws IOException {
		File teiDir = new File(args[0]);
		File solrXmlDir = new File(args[1]);
		new TeiHtmlComparator().compareAll(teiDir, solrXmlDir);
	}

	private void compareAll(File teiDir, File solrXmlDir) throws IOException {
		ArrayList<File> allFiles = new ArrayList<File>();
		fillListWithFiles(allFiles, teiDir);
		int i = 0;
		for (File tei : allFiles) {
			i++;
			System.out.println(i);
			File solrXml = new File(solrXmlDir, tei.getName());
			compareTexts(tei, solrXml);
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

	public void compareTexts(File tei, File solrXml) throws IOException {

		String teiString = FileUtils.readFileToString(tei);
		teiString = teiString.replaceAll("\n\\s*", "");
		teiString = extract("<body>(.*?)</body>", teiString);
		teiString = teiString.replace("<oRef/>", "-");
		teiString = teiString.replace("<quote>", ": ");
		teiString = teiString.replace("<lb/>", " / ");
		teiString = teiString.replace("> <", "");
		// need to replace the accidental occurrences
		teiString = teiString.replaceAll("(Zur Sache: |Syntagmen: |Redensart: |Phraseme: )", "");
		teiString = removeTags(teiString);
		teiString = teiString.replaceAll("\\s+", " ").trim();

		String solrString = FileUtils.readFileToString(solrXml);
		solrString = solrString.replaceAll("\n\\s*", "");

		solrString = extract("<field name=\"article_html\"><!\\[CDATA\\[(.*?)\\]\\]>", solrString);
		solrString = solrString.replaceAll("<span class=\"sense-number\">.*?</span>", "");
		solrString = solrString.replaceAll("<span class=\"subvoce-begin\">.*?</span>", "");
		solrString = solrString.replace("> <", "");
		solrString = solrString.replaceAll(
				"(Bedeutungsverwandt: |Syntagmen: |Quellenzitate: |Belegstellenangaben: |Gegensätze: |Phraseme: |Wortbildungen: |Zur Sache: |Redensart: )",
				"");
		solrString = removeTags(solrString);

//		System.out.println(teiString);
//		System.out.println(solrString);

		assertEquals("File: " + tei.getName(), teiString, solrString);
	}

	private String extract(String regex, String s) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	private String removeTags(String html) {
		String textOnly = html.replaceAll("<.*?>", "");
		return textOnly.replaceAll("[\\p{Zs}\\s]+", " ").trim();
	}

}
