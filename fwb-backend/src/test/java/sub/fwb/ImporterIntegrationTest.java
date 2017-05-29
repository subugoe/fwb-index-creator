package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sub.fwb.testing.EmbeddedSolr;

public class ImporterIntegrationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		String inputExcel = "src/test/resources/import/sources.xlsx";
		String teiInputDir = "src/test/resources/import";
		String solrXmlDir = "target/solrxml";
		String solrUrl = "embedded";

		if (new File(solrXmlDir).exists()) {
			FileUtils.forceDelete(new File(solrXmlDir));
		}

		Importer importer = new Importer();
		importer.convertAll(inputExcel, teiInputDir, solrXmlDir);
		importer.compareAll(teiInputDir, solrXmlDir, true);
		importer.uploadAll(solrXmlDir, solrUrl, true, true);

		SolrQuery solrQuery = new SolrQuery("lemma:test");
		solrQuery.setRequestHandler("/search");
		QueryResponse response = EmbeddedSolr.instance.query(solrQuery);
		EmbeddedSolr.instance.close();

		assertEquals("test", response.getResults().get(0).getFieldValue("lemma"));
	}

}