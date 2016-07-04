package sub.fwb;

import static org.junit.Assert.assertEquals;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class SolrConfTest {

	private static EmbeddedSolrServer solr;
	private SolrDocumentList docList;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		CoreContainer container = new CoreContainer("solr");
		container.load();

		solr = new EmbeddedSolrServer(container, "fwb");
	}

	@After
	public void tearDown() throws Exception {
		solr.deleteByQuery("*:*");
		solr.commit();

		printResults();
	}

	private void printResults() {
		System.out.println();
		System.out.println(docList.getNumFound() + " results");
		for (int i = 0; i < 20; i++) {
			if (i < docList.getNumFound()) {
				SolrDocument doc = docList.get(i);
				System.out.println(doc.getFieldValue("lemma") + "\t" + doc.getFieldValue("score"));
			}
		}
	}

	private void addToSolr(String[][] documentFields) throws Exception {
		SolrInputDocument newDoc = new SolrInputDocument();
		for (String[] docField : documentFields) {
			newDoc.addField(docField[0], docField[1]);
		}
		if (!newDoc.containsKey("id")) {
			newDoc.addField("id", "1234");
		}
		if (!newDoc.containsKey("type")) {
			newDoc.addField("type", "artikel");
		}
		solr.add(newDoc);
		solr.commit();
	}

	private void askSolrByQuery(String query) throws Exception {
		SolrQuery solrQuery = new SolrQuery(query);
		solrQuery.setRequestHandler("/select");
		solrQuery.set("fl", "*,score");
		QueryResponse response = solr.query(solrQuery);

		docList = response.getResults();
	}

	private String lemma(int resultNumber) {
		return (String) docList.get(resultNumber - 1).getFieldValue("lemma");
	}

	private long results() {
		return docList.getNumFound();
	}

	@Test
	public void shouldFindWithoutPipe() throws Exception {
		String[][] docAdd = { { "lemma", "my|lemma" } };
		addToSolr(docAdd);

		askSolrByQuery("lemma:mylemma");
		
		assertEquals(1, results());
		assertEquals("my|lemma", lemma(1));
	}

}
