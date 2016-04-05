package sub.fwb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Main {

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.out.println("Syntax: java -jar indexer.jar <input-dir> <excel-file> <output-dir>");
		} else {
			File inputDir = new File(args[0]);
			File inputExcel = new File(args[1]);
			File outputDir = new File(args[2]);

			checkIfExists(outputDir);
			
			long before = new Date().getTime();

			SourcesParser sourcesParser = new SourcesParser();
			File sourcesListXml = new File(System.getProperty("java.io.tmpdir"), "sourcesList.xml");
			sourcesParser.convertExcelToXml(inputExcel, sourcesListXml);

			InputStream xsltStream = Main.class.getResourceAsStream("/fwb-indexer.xslt");
			Xslt xslt = new Xslt(xsltStream);
			xslt.setParameter("sourcesListFile", sourcesListXml.getAbsolutePath());


			ArrayList<File> allFiles = new ArrayList<File>();
			fillWithFiles(allFiles, inputDir);
			Collections.sort(allFiles);

			int i = 0;
			for (File currentFile : allFiles) {
				int currentId = i + 1;
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
						new FileOutputStream(new File(outputDir, currentFile.getName())));
				i++;
			}

			long after = new Date().getTime();
			System.out.println("Took " + (after - before) + " milliseconds");

		}

	}

	private static void checkIfExists(File outputDir) {
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

	private static void fillWithFiles(ArrayList<File> allFiles, File currentDir) {
		File[] currentDirChildren = currentDir.listFiles();
		for (File child : currentDirChildren) {
			if (child.isFile() && child.getName().endsWith("xml")) {
				allFiles.add(child);
			} else if (child.isDirectory()) {
				fillWithFiles(allFiles, child);
			}
		}

	}

	private static void addFileParametersToXslt(String previousOrNext, String id, File file, Xslt xslt) {
		xslt.setParameter(previousOrNext + "ArticleId", id);

		String fileName = file.getName();
		int indexOfFirstDot = fileName.indexOf('.');
		String lemma = fileName.substring(0, indexOfFirstDot);
		xslt.setParameter(previousOrNext + "Lemma", lemma);
	}

}
