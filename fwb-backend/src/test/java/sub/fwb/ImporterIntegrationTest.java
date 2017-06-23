package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sub.fwb.testing.EmbeddedSolr;

public class ImporterIntegrationTest {

	private File coreProps1 = new File("solr-embedded/fwb/core.properties");
	private File coreProps1Copy = new File("solr-embedded/fwb/core.properties.copy");
	private File coreProps2 = new File("solr-embedded/fwboffline/core.properties");
	private File coreProps2Copy = new File("solr-embedded/fwboffline/core.properties.copy");
	private String solrXmlDir = "target/solrxml";

	private String importCore = "fwboffline";
	private String onlineCore = "fwb";

	@Before
	public void setUp() throws Exception {
		FileUtils.copyFile(coreProps1, coreProps1Copy);
		FileUtils.copyFile(coreProps2, coreProps2Copy);
		if (new File(solrXmlDir).exists()) {
			FileUtils.forceDelete(new File(solrXmlDir));
		}

		CoreContainer container = new CoreContainer("solr-embedded");
		container.load();
		EmbeddedSolr.instance = new EmbeddedSolrServer(container, importCore);
		EmbeddedSolr.instance.deleteByQuery(importCore, "*:*");
		EmbeddedSolr.instance.commit(importCore);
		EmbeddedSolr.instance.deleteByQuery(onlineCore, "*:*");
		EmbeddedSolr.instance.commit(onlineCore);
	}

	@After
	public void tearDown() throws Exception {
		if (coreProps1Copy.exists() && coreProps2Copy.exists()) {
			FileUtils.forceDelete(coreProps1);
			FileUtils.forceDelete(coreProps2);
			FileUtils.moveFile(coreProps1Copy, coreProps1);
			FileUtils.moveFile(coreProps2Copy, coreProps2);
		}
	}

	@Test
	public void test() throws Exception {
		String inputExcel = "src/test/resources/import/sources.xlsx";
		String teiInputDir = "src/test/resources/import";

		/* This is used as a signal to use the embedded Solr in classes: */ Uploader u; CoreSwapper c;
		String solrUrl = "embedded";

		Importer importer = new Importer();
		importer.convertAll(inputExcel, teiInputDir, solrXmlDir);
		importer.compareAll(teiInputDir, solrXmlDir);
		importer.uploadAll(solrXmlDir, solrUrl, importCore);
		importer.swapCores(solrUrl, importCore, onlineCore);

		SolrQuery solrQuery = new SolrQuery("lemma:test");
		solrQuery.setRequestHandler("/search");
		QueryResponse response = EmbeddedSolr.instance.query(onlineCore, solrQuery);
		EmbeddedSolr.instance.close();

		assertEquals("test", response.getResults().get(0).getFieldValue("lemma"));
	}

}
