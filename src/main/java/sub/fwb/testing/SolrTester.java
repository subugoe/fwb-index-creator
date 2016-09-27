package sub.fwb.testing;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.After;
import org.junit.BeforeClass;
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

	@Test
	public void complexPhrase() throws Exception {

		solr.list("\"imbi* ward\"");

		assertEquals(1, results());
		assertEquals("chiromanzie", lemma(1));
	}

	@Test
	public void es() throws Exception {

		solr.select("artikel_text:es");

		assertEquals(4446, results());
	}

	@Test
	public void negatedQueryShouldCoverAllTerms() throws Exception {

		solr.select(
				"artikel:/.*[^\\|()\\[\\]\\-⁽⁾a-z0-9äöüßoͤúv́aͤñÿu͂Øůaͧuͥóoͮàïêŷǔıͤēëâôeͣîûwͦýãæáéòõœv̈èu̇ŭāōùēīíūėm̃Γͤŭẽũśŏǒǎǔẅẹìǹăṣẏẙẹσĕĩẃåg̮ńỹěçṅȳňṡćęъčẘịǧḥṁạṙľu֔b].*/");

		assertEquals(0, results());
	}

	@Test
	public void dollarSignInKindeln() throws Exception {
		String[][] extraparams = { { "hl.q", "kindeln" } };
		solr.articleHl(extraparams, "internal_id:kindeln.s.3v");
		// This used to lead to an exception in Matcher class
		assertEquals(1, results());
	}

	@Test
	public void maxClauseCountOver1024() throws Exception {

		solr.search("artikel:*e*");
	}

	@Test
	public void dashLach() throws Exception {

		solr.search("-lach");

		assertEquals(5, results());
		assertEquals("-lach", lemma(1));
	}

	@Test
	public void imbs() throws Exception {

		solr.list("imbs");

		assertEquals(29, results());
		assertEquals("imbs", lemma(1));
		assertEquals("imbis", lemma(2));
		assertBestResultsContainWordPart("imbs");
	}

	@Test
	public void imbis() throws Exception {

		solr.list("imbis");

		assertEquals(34, results());
		assertEquals("imbis", lemma(1));
		assertBestResultsContainWordPart("imbis");
	}

	@Test
	public void gericht() throws Exception {

		solr.list("gericht");

		assertEquals(1955, results());
		assertEquals("gerichtsacten", lemma(1));
		assertBestResultsContainWordPart("gericht");
	}

	@Test
	public void phrase() throws Exception {

		solr.list("abziehen \"Ziesemer, Gr.\"");

		assertEquals(24, results());
		assertEquals("abziehen", lemma(1));
		assertBestResultsContainWordPart("abziehen");
	}

	@Test
	public void essen() throws Exception {

		solr.list("essen");

		assertEquals(5432, results());
		assertEquals("geniessen", lemma(1));
		assertBestResultsContainWordPart("essen");
	}

	@Test
	public void imbisBergman() throws Exception {

		solr.list("imbis bergman");

		assertEquals(1, results());
		assertEquals("geben", lemma(1));
	}

	@Test
	public void bergleuteBergman() throws Exception {

		solr.list("bergleute bergman");

		assertEquals(5, results());
		assertEquals("bergman", lemma(1));
		assertEquals("bergleute", lemma(2));
		assertEquals("berg", lemma(3));
	}

	@Test
	public void leben() throws Exception {

		solr.list("leben");

		assertEquals(1648, results());
		assertEquals("leben", lemma(1));
		assertEquals("leben", lemma(2));
		assertBestResultsContainWordPart("leben");
	}

	@Test
	public void christ() throws Exception {

		solr.list("christ");

		assertEquals(2082, results());
		assertEquals("christ", lemma(1));
		assertBestResultsContainWordPart("christ");
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
