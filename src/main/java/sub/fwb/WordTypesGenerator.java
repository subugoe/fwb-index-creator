package sub.fwb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class WordTypesGenerator {

	public String prepareForXslt(File wordTypes) throws IOException {
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(wordTypes)));
		String wordTypeLine = "";
		StringBuilder result = new StringBuilder();
		while ((wordTypeLine = lineReader.readLine()) != null) {
			result.append(wordTypeLine);
			result.append("###");
		}
		lineReader.close();
		return result.toString();
	}
}
