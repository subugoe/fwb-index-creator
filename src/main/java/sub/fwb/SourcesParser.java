package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SourcesParser {
	
	private String[] headers = {"sort", "sigle", "kraftliste", "", "", "", "", "", "", "", "pdf", "epdf", "online", "eonline", "permalink", "biblio", "citing", "syptom"};

	public void bla() throws IOException {
		FileInputStream file = new FileInputStream(new File("/home/dennis/temp/FWB-Quellenliste2.xlsx"));
		StringBuffer buffer = new StringBuffer();
		buffer.append("<sheet>\n");

		XSSFWorkbook workbook = new XSSFWorkbook(file);

		XSSFSheet sheet = workbook.getSheetAt(0);

		for (int i = 1; i <= 49; i++) {
			
			buffer.append("<entry>\n");
			Row row = sheet.getRow(i);
			for (int j = 0; j < headers.length; j++) {
				Cell cell = row.getCell(j);
				if (cell != null) {
					buffer.append("<" + headers[j] + ">");
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						buffer.append(cell.getStringCellValue());
					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						buffer.append(cell.getNumericCellValue());
					}
					buffer.append("</" + headers[j] + ">\n");
				}
				if (j == 2) {
					j += 7;
				}
			}
			
			buffer.append("</entry>\n");
			
			
			
//			Row r = sheet.getRow(i);
//
//			Cell c = r.getCell(2);
//			String wholeCell = "";
//			if (c != null)
//				wholeCell = c.getStringCellValue();
//
//			String foundName = extractUsingRegex("\\$c(.*?)#", wholeCell).get(0);
//			//System.out.println();
//			// System.out.println(foundName);
//
//			Cell biblioCell = r.getCell(15);
//			String biblio = "";
//			if (biblioCell != null)
//				biblio = biblioCell.getStringCellValue();
//			
//			String hrsg = extractUsingRegex("[hH]rsg\\. v\\.\\s*(.*?\\w\\w)\\.", biblio).get(0);
//			//System.out.println(i + 1 + ": " + hrsg);
//
//			List<String> years = extractUsingRegex("(\\w*\\s1\\d\\d\\d)", biblio);
//			for (String year : years) {
//				//System.out.println(i + 1 + ": " + year);
//			}
		}
		workbook.close();
		
		buffer.append("</sheet>");
		System.out.println(buffer);
	}

	private List<String> extractUsingRegex(String regex, String s) {
		List<String> results = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			results.add(matcher.group(1));
		}

		if (results.isEmpty()) {
			results.add("");
		}
		return results;
	}

}
