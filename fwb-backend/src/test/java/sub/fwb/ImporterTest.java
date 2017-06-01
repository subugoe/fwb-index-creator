package sub.fwb;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;

public class ImporterTest {

	private MyMocks mock;
	private Importer importer = new Importer();

	@Before
	public void setUp() throws Exception {
		mock = new MyMocks(importer);
	}

	@Test
	public void shouldConvertExcelAndTwoTeis() throws Exception {

		prepareFileAccess("/teis/a.xml", "/teis/b.xml");
		importer.convertAll("/my-excel.xls", "/teis", "/output");

		verify(mock.sourcesParser).convertExcelToXml(new File("/my-excel.xls"), new File("/output/0-sources.xml"));
		verify(mock.fileAccess).createOutputStream(new File("/output"), "a.xml");
		verify(mock.xslt).transform("/teis/a.xml", null);
		verify(mock.fileAccess).createOutputStream(new File("/output"), "b.xml");
		verify(mock.xslt).transform("/teis/b.xml", null);
	}

	@Test
	public void shouldUpload() throws Exception {

		prepareFileAccess("/output/a.xml", "/output/b.xml");
		importer.uploadAll("/output", "http://localhost/solr");

		verify(mock.uploader).add(new File("/output/a.xml"));
		verify(mock.uploader).add(new File("/output/b.xml"));
		verify(mock.uploader).commitToSolr();
	}

	@Test
	public void shouldFailWhileUploading() throws Exception {
		doThrow(new SolrServerException("Intentional test exception")).when(mock.uploader).cleanSolr();
		importer.uploadAll("/output", "http://localhost/solr");
		
		verify(mock.uploader).rollbackChanges();
	}

	private void prepareFileAccess(String... files) {
		List<File> filesList = new ArrayList<>();
		for (String file : files) {
			filesList.add(new File(file));
		}
		when(mock.fileAccess.getAllXmlFilesFromDir(any(File.class))).thenReturn(filesList);
	}

}
