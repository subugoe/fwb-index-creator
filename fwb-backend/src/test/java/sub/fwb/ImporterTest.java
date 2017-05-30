package sub.fwb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class ImporterTest {

	private MyMocks mock;
	private Importer importer = new Importer();

	@Before
	public void setUp() throws Exception {
		mock = new MyMocks();
		importer.setFileAccess(mock.fileAccess);
		importer.setSourcesParser(mock.sourcesParser);
		importer.setWordTyper(mock.wordTyper);
		importer.setXslt(mock.xslt);
	}

	@Test
	public void shouldConvertExcelAndTwoTeis() throws Exception {

		importer.convertAll("/my-excel.xls", "/teis", "/output");

		verify(mock.sourcesParser).convertExcelToXml(new File("/my-excel.xls"), new File("/output/0-sources.xml"));
		verify(mock.fileAccess).createOutputStream(new File("/output"), "file1");
		verify(mock.xslt).transform("/teis/file1", null);
		verify(mock.fileAccess).createOutputStream(new File("/output"), "file2");
		verify(mock.xslt).transform("/teis/file2", null);
	}

}
