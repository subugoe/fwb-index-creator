package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheetRange;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SourcesParser {

	private String[] headers = { "sort", "sigle", "kraftliste", "", "", "", "", "", "", "", "pdf", "epdf", "online",
			"eonline", "permalink", "biblio", "citing", "syptom", "name", "sinnwelt", "kategorien" };

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

			appendFromStronglist(row, buffer);

			buffer.append("<field name=\"source_html\"><![CDATA[");
			buffer.append("<div class=\"source-details\">\n");

			appendRowOfSpans("Sigle: ", sigle, buffer);
			appendRowOfSpans("Bibliographie: ", asString(row.getCell(15)), buffer);
			appendRowOfSpans("Zitierweise: ", asString(row.getCell(16)), buffer);

			Map<String, String> links = new HashMap<String, String>();
			String permalink = asString(row.getCell(14));
			if (!permalink.isEmpty()) {
				links.put("Permalink", permalink);
			}
			String digi = asString(row.getCell(12));
			if (!digi.isEmpty()) {
				links.put("Digitalisat online", digi);
			}
			String pdf = asString(row.getCell(10));
			if (!pdf.isEmpty()) {
				links.put("PDF", pdf);
			}

			if (!links.isEmpty()) {
				appendRowWithLinks(links, buffer);
			}

			buffer.append("</div>\n");
			buffer.append("]]></field>\n");
			buffer.append("</doc>\n");

		}
		workbook.close();

		buffer.append("</add>");
		FileUtils.writeStringToFile(xmlResult, buffer.toString(), "UTF-8");
	}

	private void appendFromStronglist(Row row, StringBuffer buffer) {
		String entryKind = asString(row.getCell(18));
		String fieldName = "";
		if ("1".equals(entryKind)) {
			fieldName = "source_author";
		} else if ("2".equals(entryKind)) {
			fieldName = "source_author_secondary";
		} else if ("3".equals(entryKind)) {
			fieldName = "source_herausgeber";
		} else if ("4".equals(entryKind)) {
			fieldName = "source_title";
		} else {
			return;
		}
		String stronglist = asString(row.getCell(2));
		String entryValue = extractUsingRegex("\\$c(.*?)#", stronglist).get(0);
		buffer.append("<field name=\"" + fieldName + "\"><![CDATA[" + entryValue + "]]></field>\n");
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

	private void appendRowOfSpans(String left, String right, StringBuffer buffer) {
		buffer.append("  <div class=\"source-details-row\">\n");
		buffer.append("  <span class=\"column-left\">" + left + "</span>\n");
		buffer.append("  <span class=\"column-right\">" + right + "</span>\n");
		buffer.append("  </div>\n");
	}

	private void appendRowWithLinks(Map<String, String> links, StringBuffer buffer) {
		buffer.append("  <div class=\"source-details-row\">\n");
		buffer.append("    <span class=\"column-left\">Links: </span>\n");
		buffer.append("    <span class=\"column-right\">");
		for (Map.Entry<String, String> entry : links.entrySet()) {
			buffer.append(asHref(entry.getKey(), entry.getValue()));
		}
		buffer.append("    </span>\n");
		buffer.append("  </div>\n");
	}

	private String asHref(String label, String reference) {
		if (reference == null || reference.isEmpty()) {
			return "";
		}
		return "<a class=\"source-details_link\" href=\"" + reference + "\">" + label + "</a>";
	}

	private boolean isEmpty(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().isEmpty();
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return true;
		}
		return false;
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
