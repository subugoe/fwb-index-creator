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
		buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		buffer.append("<add>\n");

		XSSFWorkbook workbook = new XSSFWorkbook(file);

		XSSFSheet sheet = workbook.getSheetAt(0);

		// for (int i = 1; i <= 49; i++) {
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {

			buffer.append("<doc>\n");
			Row row = sheet.getRow(i);
			buffer.append("<field name=\"type\">quelle</field>\n");
			String sigle = asString(row.getCell(1));
			buffer.append("<field name=\"id\">source_" + sigle + "</field>\n");
			buffer.append("<field name=\"source_html\"><![CDATA[");
			buffer.append("<div class=\"source-details\">\n");

			appendSpan("Sigle: ", sigle, buffer);
			appendSpan("Bibliographie: ", asString(row.getCell(15)), buffer);
			appendSpan("Zitierweise: ", asString(row.getCell(16)), buffer);
			String permalink = asString(row.getCell(14));
			if (!permalink.isEmpty()) {
				appendLink("Permalink: ", permalink, buffer);
			}
			String online = asString(row.getCell(12));
			if (!online.isEmpty()) {
				appendLink("Digitalisat online: ", online, buffer);
			}
			String pdf = asString(row.getCell(10));
			if (!pdf.isEmpty()) {
				appendSpan("PDF: ", pdf, buffer);
			}

			buffer.append("</div>\n");
			buffer.append("]]></field>\n");
			buffer.append("</doc>\n");

		}
		workbook.close();

		buffer.append("</add>");
		FileUtils.writeStringToFile(xmlResult, buffer.toString(), "UTF-8");
	}

	private String asString(Cell cell) {
		String result = "";
		if (cell != null && !isEmpty(cell)) {
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				result += cell.getStringCellValue();
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				result += new Double(cell.getNumericCellValue()).intValue();
			} else {
				throw new RuntimeException("Unknown cell type: " + cell.getCellType() + ".");
			}
		}
		return result;
	}

	private void appendSpan(String left, String right, StringBuffer buffer) {
		buffer.append("  <div class=\"source-details-row\">\n");
		buffer.append("  <span class=\"column-left\">" + left + "</span>\n");
		buffer.append("  <span class=\"column-right\">" + right + "</span>\n");
		buffer.append("  </div>\n");
	}

	private void appendLink(String left, String right, StringBuffer buffer) {
		buffer.append("  <div class=\"source-details-row\">\n");
		buffer.append("  <span class=\"column-left\">" + left + "</span>\n");
		buffer.append("  <span class=\"column-right\">" + asHref(right) + "</span>\n");
		buffer.append("  </div>\n");
	}

	private String asHref(String link) {
		return "<a href=\"" + link + "\">" + link + "</a>";
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
