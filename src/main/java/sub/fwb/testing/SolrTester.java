package sub.fwb.testing;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SolrTester {
	private static SolrState solr;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		String solrUrl = System.getProperty("SOLR_URL_FOR_TESTS", "http://localhost:8983/solr/fwb");
		SolrClient solrServerClient = new HttpSolrClient(solrUrl);
		solr = new SolrState(solrServerClient);
	}

	@After
	public void afterEach() throws Exception {
		if (System.getProperty("SOLR_URL_FOR_TESTS") != null) {
			solr.printQueryString();
		} else {
			solr.printResults();
		}
	}

	@Ignore
	@Test
	public void complexPhrase() throws Exception {

		// solr.ask("{!complexphrase inOrder=true}\"imbi* ward\" {!complexphrase
		// inOrder=true}+article_fulltext:\"imbi* ward\"");

		// assertEquals(24, results());
		// assertEquals("abziehen", lemma(1));
		// assertEquals("abrechen", lemma(2));
		// assertEquals("abschlagen", lemma(3));
	}

	@Test
	public void es() throws Exception {

		solr.askByQuery("article_fulltext:es");

		// String s = "";
		// for (int i = 1; i <= results(); i++) {
		// s += lemma(i) + "\n";
		// }
		// FileUtils.writeStringToFile(new
		// java.io.File("/home/dennis/html.txt"), s);

		assertEquals(4172, results());
	}

	@Test
	public void maxClauseCountOver1024() throws Exception {

		solr.askByQuery("article_fulltext:*e*", "/selecthl");

		assertEquals(34678, results());
	}

	@Test
	public void imbs() throws Exception {

		solr.ask("imbs");

		assertEquals(22, results());
		assertEquals("imbs", lemma(1));
		assertEquals("imbis", lemma(2));
		assertBestResultsContainWordPart("imbs");
	}

	@Test
	public void imbis() throws Exception {

		solr.ask("imbis");

		assertEquals(31, results());
		assertEquals("imbis", lemma(1));
		assertBestResultsContainWordPart("imbis");
	}

	@Test
	public void gericht() throws Exception {

		solr.ask("gericht");

		assertEquals(1823, results());
		assertEquals("landgericht", lemma(1));
		assertBestResultsContainWordPart("gericht");
	}

	@Test
	public void phrase() throws Exception {

		solr.ask("abziehen", "\"Ziesemer, Gr.\"");

		assertEquals(24, results());
		assertEquals("abziehen", lemma(1));
		assertBestResultsContainWordPart("abziehen");
	}

	@Test
	public void essen() throws Exception {

		solr.ask("essen");

		assertEquals(2414, results());
		assertEquals("geniessen", lemma(1));
		assertBestResultsContainWordPart("essen");
	}

	@Test
	public void imbisBergman() throws Exception {

		solr.ask("imbis", "bergman");

		assertEquals(1, results());
		assertEquals("geben", lemma(1));
	}

	@Test
	public void bergleuteBergman() throws Exception {

		solr.ask("bergleute", "bergman");

		assertEquals(5, results());
		assertEquals("bergman", lemma(1));
		assertEquals("bergleute", lemma(2));
		assertEquals("berg", lemma(3));
	}

	@Test
	public void leben() throws Exception {

		solr.ask("leben");

		assertEquals(1498, results());
		assertEquals("leben", lemma(1));
		assertEquals("leben", lemma(2));
		assertBestResultsContainWordPart("leben");
	}

	@Test
	public void christ() throws Exception {

		solr.ask("christ");

		assertEquals(1448, results());
		assertEquals("christ", lemma(1));
		assertBestResultsContainWordPart("christ");
	}

	// @Test
	public void deleteAllDocuments() throws Exception {
		solr.clean();
	}

	private String lemma(int resultNumber) {
		return solr.lemma(resultNumber);
	}

	private long results() {
		return solr.results();
	}

	private void assertBestResultsContainWordPart(String wordPart) throws Exception {
		int numLemmas = solr.askForNumberOfLemmas(wordPart);
		for (int i = 1; i <= numLemmas; i++) {
			String currentLemma = solr.lemma(i).toLowerCase();
			assertThat(currentLemma, containsString(wordPart));
		}
	}

}
