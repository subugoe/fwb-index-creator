package sub.fwb.testing;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
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
	private String solrQueryString = "";

	@Before
	public void setUp() throws Exception {
		solrServer.setRequestWriter(new RequestWriter());
		solrServer.setParser(new XMLResponseParser());
	}

	private void askSolr(String... userInputs) throws Exception {
		solrQueryString = "";
		for (String inputValue : userInputs) {
			if (inputValue.startsWith("\"")) {
				solrQueryString += inputValue + " " + "+article_fulltext:" + inputValue + " ";
			} else {
				solrQueryString += inputValue + " *" + inputValue + "* " + "+article_fulltext:*" + inputValue + "* ";
			}
		}
		SolrQuery solrQuery = new SolrQuery(solrQueryString);
		solrQuery.setRequestHandler("/select");
		solrQuery.set("fl", "lemma,score");
		solrQuery.set("rows", "500");
		//solrQuery.set("tie", "0.01");
		solrQuery.set("qf",
				"lemma^10000 neblem^1000 definition_fulltext^70 article_related_lemma^60 "
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

	private void assertBestResultsContainWordPart(String wordPart) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery("lemma:*" + wordPart + "*");
		solrQuery.setRequestHandler("/select");
		solrQuery.set("fl", "lemma");
		solrQuery.set("rows", "500");

		QueryResponse response = solrServer.query(solrQuery);

		SolrDocumentList lemmas = response.getResults();
		int numLemmas = (int) lemmas.getNumFound();
		for (int i = 0; i < numLemmas; i++) {
			String currentLemma = (String) docList.get(i).getFieldValue("lemma");
			assertThat(currentLemma.toLowerCase(), containsString(wordPart));
		}
	}

	@After
	public void tearDown() throws Exception {
		System.out.println();
		System.out.println(solrQueryString);
		System.out.println(docList.getNumFound() + " results");
		for (int i = 0; i < 20; i++) {
			if (i < docList.getNumFound()) {
				SolrDocument doc = docList.get(i);
				System.out.println(doc.getFieldValue("lemma") + "\t" + doc.getFieldValue("score"));
			}
		}
	}

	@Test
	public void imbs() throws Exception {

		askSolr("imbs");

		assertEquals(22, results());
		assertEquals("imbs", lemma(1));
		assertEquals("imbis", lemma(2));
		assertBestResultsContainWordPart("imbs");
	}

	@Test
	public void imbis() throws Exception {

		askSolr("imbis");

		assertEquals(26, results());
		assertEquals("imbis", lemma(1));
		assertBestResultsContainWordPart("imbis");
	}

	@Test
	public void gericht() throws Exception {

		askSolr("gericht");

		assertEquals(1823, results());
		assertEquals("landgericht", lemma(1));
		assertBestResultsContainWordPart("gericht");
	}

	@Test
	public void phrase() throws Exception {

		askSolr("abziehen", "\"Ziesemer, Gr.\"");

		assertEquals(24, results());
		assertEquals("abziehen", lemma(1));
		assertEquals("abrechen", lemma(2));
		assertEquals("abschlagen", lemma(3));
		assertBestResultsContainWordPart("abziehen");
	}

	@Test
	public void essen() throws Exception {

		askSolr("essen");

		assertEquals(2410, results());
		assertEquals("geniessen", lemma(1));
		assertBestResultsContainWordPart("essen");
	}

	@Test
	public void imbisBergman() throws Exception {

		askSolr("imbis", "bergman");

		assertEquals(1, results());
		assertEquals("geben", lemma(1));
	}

	@Test
	public void bergleuteBergman() throws Exception {

		askSolr("bergleute", "bergman");

		assertEquals(5, results());
		assertEquals("bergman", lemma(1));
		assertEquals("bergleute", lemma(2));
		assertEquals("berg", lemma(3));
	}

	@Test
	public void leben() throws Exception {

		askSolr("leben");

		assertEquals(1496, results());
		assertEquals("leben", lemma(1));
		assertEquals("leben", lemma(2));
		assertBestResultsContainWordPart("leben");
	}

	@Test
	public void christ() throws Exception {

		askSolr("christ");

		assertEquals(1448, results());
		assertEquals("christ", lemma(1));
		assertBestResultsContainWordPart("christ");
	}

	@Ignore
	@Test
	public void complexPhrase() throws Exception {

		// askSolr("{!complexphrase inOrder=true}\"imbi* ward\" {!complexphrase
		// inOrder=true}+article_fulltext:\"imbi* ward\"");

		// assertEquals(24, results());
		// assertEquals("abziehen", lemma(1));
		// assertEquals("abrechen", lemma(2));
		// assertEquals("abschlagen", lemma(3));
	}

}
