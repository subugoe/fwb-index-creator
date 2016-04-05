package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SourcesParser {

	private String[] headers = { "sort", "sigle", "kraftliste", "", "", "", "", "", "", "", "pdf", "epdf", "online",
			"eonline", "permalink", "biblio", "citing", "syptom" };

	public void convertExcelToXml(File excelFile, File xmlResult) throws IOException {
		FileInputStream file = new FileInputStream(excelFile);
		StringBuffer buffer = new StringBuffer();
		buffer.append("<sheet>\n");

		XSSFWorkbook workbook = new XSSFWorkbook(file);

		XSSFSheet sheet = workbook.getSheetAt(0);

		// for (int i = 1; i <= 49; i++) {
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {

			buffer.append("<entry>\n");
			Row row = sheet.getRow(i);
			for (int j = 0; j < headers.length; j++) {
				Cell cell = row.getCell(j);
				if (cell != null && !isEmpty(cell)) {
					buffer.append("<" + headers[j] + ">");
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						buffer.append("<![CDATA[");
						buffer.append(cell.getStringCellValue());
						buffer.append("]]>");
					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						buffer.append(new Double(cell.getNumericCellValue()).intValue());
					} else {
						throw new RuntimeException(
								"Unknown cell type: " + cell.getCellType() + ". Field: " + headers[j]);
					}
					buffer.append("</" + headers[j] + ">\n");
				}
				if (j == 2) {
					j += 7;
				}
			}

			buffer.append("</entry>\n");

		}
		workbook.close();

		buffer.append("</sheet>");
		FileUtils.writeStringToFile(xmlResult, buffer.toString(), "UTF-8");
	}

	private boolean isEmpty(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().isEmpty();
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return true;
		}
		return false;
	}

}
