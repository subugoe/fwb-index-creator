package sub.fwb;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import sub.fwb.testing.SolrState;

public class EmbeddedSolrTest {

	private static SolrState solr;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		CoreContainer container = new CoreContainer("solr");
		container.load();
		EmbeddedSolrServer solrEmbedded = new EmbeddedSolrServer(container, "fwb");
		solr = new SolrState(solrEmbedded);
	}

	@After
	public void afterEach() throws Exception {
		solr.clean();
		solr.printResults();
	}

	//@Test
	public void should() throws Exception {
		String[][] doc = { { "zitat", "new" } };
		solr.addDocument(doc);

		solr.askByQuery("zitat:old", "/search");

		assertEquals(1, results());
	}

	@Test
	public void shouldReplaceAccentedLetter() throws Exception {
		String[][] doc = { { "zitat", "únser" } };
		solr.addDocument(doc);

		solr.askByQuery("zitat:unser");

		assertEquals(1, results());
	}

	@Test
	public void shouldRemoveCombinedLetter() throws Exception {
		String[][] doc = { { "zitat", "svͤlen" } };
		solr.addDocument(doc);

		solr.askByQuery("zitat:svlen");

		assertEquals(1, results());
	}

	@Test
	public void shouldHighlightChristDifferently() throws Exception {
		String[][] doc = { { "artikel_text", "christ krist" }, { "zitat", "christ krist" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl", "on" } };
		solr.ask(extraParams, "christ");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "christ");
		assertNotHighlighted("artikel_text", "krist");
		assertHighlighted("zitat", "christ", "krist");
	}

	@Test
	public void shouldHighlightArticleAndCitationDifferently() throws Exception {
		String[][] doc = { { "artikel_text", "und vnd" }, { "zitat", "und vnd" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl", "on" } };
		solr.ask(extraParams, "und");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "und");
		assertNotHighlighted("artikel_text", "vnd");
		assertHighlighted("zitat", "und", "vnd");
	}

	@Test
	public void shouldFindPartialAlternativeSpellings() throws Exception {
		String[][] doc = { { "zitat", "wvnde" } };
		solr.addDocument(doc);

		solr.askByQuery("zitat:*unt*");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindAlternativeSpellings() throws Exception {
		String[][] doc = { { "zitat", "vnd katze" } };
		solr.addDocument(doc);

		solr.askByQuery("zitat:(+und +unt +vnt +vnd +katze +chatze +qatze +catze +gedza)");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindUmlaut() throws Exception {
		String[][] doc = { { "artikel_text", "bär" } };
		solr.addDocument(doc);

		solr.askByQuery("artikel_text:bar");

		assertEquals(1, results());
	}

	@Test
	public void shouldDeleteNonbreakingSpace() throws Exception {
		String[][] doc = { { "artikel_text", "test abc" } };
		solr.addDocument(doc);

		solr.askByQuery("artikel_text:test");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindPipe() throws Exception {
		String[][] doc = { { "artikel_text", "test |" } };
		solr.addDocument(doc);

		solr.askByQuery("artikel_text:|");

		assertEquals(1, results());
	}

	@Test
	public void shouldIgnoreSpecialChars() throws Exception {
		String[][] doc = { { "artikel_text", "& test1, ›test2‹" } };
		solr.addDocument(doc);

		solr.askByQuery("artikel_text:test1");
		assertEquals(1, results());
		solr.askByQuery("artikel_text:test2");
		assertEquals(1, results());
		solr.askByQuery("artikel_text:&");
		assertEquals(0, results());
	}

	@Test
	public void shouldFindWithoutPipe() throws Exception {
		String[][] doc = { { "lemma", "my|lemma" } };
		solr.addDocument(doc);

		solr.askByQuery("lemma:mylemma");

		assertEquals(1, results());
		assertEquals("my|lemma", lemma(1));
	}

	private String lemma(int resultNumber) {
		return solr.lemma(resultNumber);
	}

	private long results() {
		return solr.results();
	}

	private void assertHighlighted(String fieldName, String... words) {
		assertHighlighted(true, fieldName, words);
	}

	private void assertNotHighlighted(String fieldName, String... words) {
		assertHighlighted(false, fieldName, words);
	}

	private void assertHighlighted(boolean forReal, String fieldName, String... words) {
		String hlText = solr.getHighlightings().get("1234").get(fieldName).get(0);
		for (String word : words) {
			String hlWord = "<em>" + word + "</em>";
			if (forReal) {
				assertThat(hlText, containsString(hlWord));
			} else {
				assertThat(hlText, not(containsString(hlWord)));
			}
		}
	}

}
