package sub.fwb;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MainTest {

	private Main main = new Main();
	private ByteArrayOutputStream baos;
	private Importer importerMock = mock(Importer.class);

	@Before
	public void setUp() throws Exception {
		baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		main.setLogOutput(out);
		main.setImporter(importerMock);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldExecuteAllStages() throws Exception {
		String[] args = { "-convert", "-compare", "-upload", "-test", "-excel", "target/sources.xls", "-teidir",
				"target/teis", "-solrxmldir", "target/output", "-solr", "http://localhost/solr", "-core", "mycore" };
		main.execute(args);

		verify(importerMock).convertAll("target/sources.xls", "target/teis", "target/output");
		verify(importerMock).compareAll("target/teis", "target/output");
		verify(importerMock).uploadAll("target/output", "http://localhost/solr", "mycore");
		verify(importerMock).runTests("http://localhost/solr", "mycore");

		assertThat(mainOutput(), containsString("minutes"));
	}

	@Test
	public void shouldExecuteConversion() throws Exception {
		String[] args = { "-convert", "-excel", "target/sources.xls", "-teidir", "target/teis", "-solrxmldir",
				"target/output" };
		main.execute(args);

		verify(importerMock).convertAll("target/sources.xls", "target/teis", "target/output");
		verify(importerMock, times(0)).uploadAll(anyString(), anyString(), anyString());
	}

	@Test
	public void shouldExecuteUpload() throws Exception {
		String[] args = { "-upload", "-solrxmldir", "target/output", "-solr", "http://localhost/solr", "-core", "mycore" };
		main.execute(args);

		verify(importerMock, times(0)).convertAll(anyString(), anyString(), anyString());
		verify(importerMock).uploadAll("target/output", "http://localhost/solr", "mycore");
	}

	@Test
	public void shouldComplainAboutWrongArgument() throws Exception {
		String[] args = { "-somethingwrong" };
		main.execute(args);

		verify(importerMock, times(0)).convertAll(anyString(), anyString(), anyString());
		verify(importerMock, times(0)).uploadAll(anyString(), anyString(), anyString());

		assertThat(mainOutput(), containsString("Illegal arguments"));
	}

	@Test
	public void shouldComplainAboutMissingArgument() throws Exception {
		String[] args = { "-convert" };
		main.execute(args);

		assertThat(mainOutput(), containsString("Missing required arguments"));
	}

	private String mainOutput() {
		return new String(baos.toByteArray());
	}
}
