package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SourcesParser {

	public void bla() throws IOException {
		FileInputStream file = new FileInputStream(new File("/home/dennis/temp/FWB-Quellenliste2.xlsx"));

		XSSFWorkbook workbook = new XSSFWorkbook(file);

		XSSFSheet sheet = workbook.getSheetAt(0);

		for (int i = 1; i < 6; i++) {

			Row r = sheet.getRow(i);

			Cell c = r.getCell(2);
			String wholeCell = c.getStringCellValue();

			Pattern pattern = Pattern.compile("\\$c(.*?)#");
			Matcher matcher = pattern.matcher(wholeCell);
			String foundName = "";
			if (matcher.find()) {
				foundName = matcher.group(1);
			}
			// System.out.println(foundName);

		}
	}

}
