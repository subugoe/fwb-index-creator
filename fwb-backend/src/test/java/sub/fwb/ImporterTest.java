package sub.fwb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class ImporterTest {

	private MyMocks mock;

	@Before
	public void setUp() throws Exception {
		mock = new MyMocks();
	}

	@Test
	public void test() throws Exception {
		Importer importer = new Importer();
		importer.setFileAccess(mock.fileAccess);
		importer.setSourcesParser(mock.sourcesParser);
		importer.setWordTyper(mock.wordTyper);
		importer.setXslt(mock.xslt);
		
		importer.convertAll("/my-excel.xls", "/teis", "/output");
	}

}
