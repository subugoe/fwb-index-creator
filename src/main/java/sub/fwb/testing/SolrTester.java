package sub.fwb.testing;

import static org.junit.Assert.assertEquals;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
		solrQuery.set("rows", "20");
		solrQuery.set("qf", "lemma^200 neblem^90 definition_fulltext^70 article_related_lemma^60 "
				+ "definition_source_citation^55 sense_phraseme^45 sense_word_reference^45 "
				+ "sense_antonym^25 sense_symptom_value^40 sense_syntagma^10 sense_word_formation^20 "
				+ "sense_related_reference^20");

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
		System.out.println();
		System.out.println(docList.getNumFound() + " results");
		for (SolrDocument doc : docList) {
			System.out.println(doc.getFieldValue("lemma") + "\t" + doc.getFieldValue("score"));
		}
	}

	@Test
	public void imbs() throws Exception {

		askSolr("imbs *imbs* +article_fulltext:*imbs*");

		assertEquals(22, results());
		assertEquals("imbs", lemma(1));
		assertEquals("imbis", lemma(2));
	}

	@Test
	public void imbis() throws Exception {

		askSolr("imbis *imbis* +article_fulltext:*imbis*");

		assertEquals(26, results());
		assertEquals("imbis", lemma(1));
		assertEquals("imbisgast", lemma(2));
		assertEquals("imbiskost", lemma(3));
	}
	
	@Test
	public void gericht() throws Exception {

		askSolr("gericht *gericht* +article_fulltext:*gericht*");

		assertEquals(1823, results());
		assertEquals("landgericht", lemma(1));
		assertEquals("lehengericht", lemma(2));
		assertEquals("kaufgericht", lemma(3));
	}
	
	@Test
	public void phrase() throws Exception {

		askSolr("abziehen *abziehen* \"Ziesemer, Gr.\" +article_fulltext:\"Ziesemer, Gr.\" +article_fulltext:*abziehen*");

		assertEquals(24, results());
		assertEquals("abziehen", lemma(1));
		assertEquals("abrechen", lemma(2));
		assertEquals("abschlagen", lemma(3));
	}

	@Test
	public void essen() throws Exception {
		
		askSolr("essen *essen* +article_fulltext:*essen*");

		assertEquals(2410, results());
		assertEquals("geniessen", lemma(1));
		assertEquals("befressen", lemma(2));
		assertEquals("ge|essen", lemma(3));
	}
	
	@Test
	public void imbisBergman() throws Exception {

		askSolr("imbis bergman *imbis* *bergman* +article_fulltext:*imbis* +article_fulltext:*bergman*");

		assertEquals(1, results());
		assertEquals("geben", lemma(1));
	}
	
	@Test
	public void bergleuteBergman() throws Exception {

		askSolr("bergleute bergman *bergleute* *bergman* +article_fulltext:*bergleute* +article_fulltext:*bergman*");

		assertEquals(5, results());
		assertEquals("bergman", lemma(1));
		assertEquals("bergleute", lemma(2));
		assertEquals("berg", lemma(3));
	}
	
	@Ignore
	@Test
	public void complexPhrase() throws Exception {

//		askSolr("{!complexphrase inOrder=true}\"imbi* ward\" {!complexphrase inOrder=true}+article_fulltext:\"imbi* ward\"");

//		assertEquals(24, results());
//		assertEquals("abziehen", lemma(1));
//		assertEquals("abrechen", lemma(2));
//		assertEquals("abschlagen", lemma(3));
	}


}
