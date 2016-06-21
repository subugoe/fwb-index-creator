package sub.fwb.testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SolrTester {
	private HttpSolrServer solrServer = new HttpSolrServer("http://localhost:8983/solr/fwb");
	private SolrDocumentList docList;

	@Before
	public void setUp() throws Exception {
		solrServer.setRequestWriter(new RequestWriter());
		solrServer.setParser(new XMLResponseParser());
	}

	private void askSolr(String query) throws Exception {
		SolrQuery solrQuery = new SolrQuery(query);
		solrQuery.setRequestHandler("/select");
		solrQuery.set("fl", "lemma,score");

		QueryResponse response = solrServer.query(solrQuery);

		docList = response.getResults();
	}

	private String lemma(int resultNumber) {
		return (String) docList.get(resultNumber - 1).getFieldValue("lemma");
	}

	private long results() {
		return docList.getNumFound();
	}

	@After
	public void tearDown() throws Exception {
		System.out.println(docList.getNumFound() + " results");
		for (SolrDocument doc : docList) {
			System.out.println(doc.getFieldValue("lemma") + "\t" + doc.getFieldValue("score"));
		}
	}

	@Test
	public void imbs() throws Exception {

		askSolr("imbs +article_fulltext:*imbs*");

		assertEquals(22, results());

		assertEquals("imbs", lemma(1));
		assertEquals("imbis", lemma(2));
		assertEquals("anheimsch", lemma(3));

	}

}
