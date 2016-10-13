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

	@Test
	public void shouldHighlightOnlyExactTerm() throws Exception {
		String[][] doc = { { "zitat", "Imbis imbis" }, { "zitat_text", "Imbis imbis" }, { "artikel", "Imbis imbis" },
				{ "artikel_text", "Imbis imbis" } };
		solr.addDocument(doc);

		solr.search("EXAKT Imbis");
		assertEquals(1, results());
		assertHighlighted("artikel_text", "Imbis");
		assertNotHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldFindOnlyExactTerm() throws Exception {
		String[][] doc = { { "zitat", "Imbis" }, { "zitat_text", "Imbis" }, { "artikel", "Imbis" },
				{ "artikel_text", "Imbis" } };
		solr.addDocument(doc);

		solr.search("EXAKT Imbis");
		assertEquals(1, results());

		solr.search("EXAKT imbis");
		assertEquals(0, results());
	}

	@Test
	public void shouldHighlightOnlyExactMatchInCitation() throws Exception {
		String[][] doc = { { "zitat", "Imbis imbis" }, { "zitat_text", "Imbis imbis" } };
		solr.addDocument(doc);

		solr.search("zitat:Imbis EXAKT");
		assertEquals(1, results());

		assertHighlighted("artikel_text", "Imbis");
		assertNotHighlighted("artikel_text", "imbis");
	}

	@Test
	public void shouldFindOnlyExactInCitation() throws Exception {
		String[][] doc = { { "zitat", "Imbis" }, { "zitat_text", "Imbis" } };
		solr.addDocument(doc);

		solr.search("EXAKT zitat:Imbis");
		assertEquals(1, results());

		solr.search("EXAKT zitat:imbis");
		assertEquals(0, results());
	}

	@Test
	public void shouldHighlightBdvOnly() throws Exception {
		String[][] doc = { { "artikel", "imbis <!--start bdv1--><div>imbisinbdv</div><!--end bdv1-->" },
				{ "bdv", "<!--start bdv1--><div>imbisinbdv</div><!--end bdv1-->" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl.q", "bdv:imbis" } };
		solr.articleHl(extraParams, "id:1234");

		assertEquals(1, results());
		assertNotHighlighted("artikel", "imbis");
		assertHighlighted("artikel", "imbisinbdv");
	}

	@Test
	public void shouldHighlightInArticle() throws Exception {
		String[][] doc = { { "artikel", "imbis" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl.q", "imbis" } };
		solr.articleHl(extraParams, "id:1234");

		assertEquals(1, results());
		assertHighlighted("artikel", "imbis");
	}

	@Test
	public void shouldOverwriteArticleHighlighting() throws Exception {
		String[][] doc = { { "artikel", "bla" }, { "bdv", "bla" }, { "artikel_text", "different" },
				{ "bdv_text", "bla" } };
		solr.addDocument(doc);

		solr.search("bdv:bla");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "bla");
	}

	@Test
	public void shouldHighlightInsideHtml() throws Exception {
		String[][] doc = { { "bdv", "<div>bla</div>" } };
		solr.addDocument(doc);

		String[][] extraParams = { { "hl", "on" } };
		solr.select(extraParams, "bdv:bla");

		assertEquals(1, results());
		assertHighlighted("bdv", "bla");
	}

	@Test
	public void shouldRemoveDash() throws Exception {
		String[][] doc = { { "artikel", "legatar(-ius)" } };
		solr.addDocument(doc);

		solr.search("legatarius");

		assertEquals(1, results());
	}

	@Test
	public void shouldNotHighlightTes() throws Exception {
		String[][] doc = { { "zitat", "das" }, { "zitat_text", "das" }, { "artikel_text", "tes das" },
				{ "artikel", "tes das" } };
		solr.addDocument(doc);

		solr.search("das");

		assertEquals(1, results());
		assertNotHighlighted("artikel_text", "tes");
	}

	@Test
	public void shouldHighlightQuote() throws Exception {
		String[][] doc = { { "zitat", "und" }, { "zitat_text", "und" }, { "artikel_text", "und" },
				{ "artikel", "und" } };
		solr.addDocument(doc);

		solr.search("vnd");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "und");
	}

	@Test
	public void shouldFilterNonletters() throws Exception {
		String[][] doc = { { "artikel", "bla" } };
		solr.addDocument(doc);

		solr.search("artikel:#+bl,;.");

		assertEquals(1, results());
	}

	@Test
	public void shouldSearchInCitations() throws Exception {
		String[][] doc = { { "zitat", "únser" } };
		solr.addDocument(doc);

		solr.search("unser");

		assertEquals(1, results());
	}

	@Test
	public void shouldReplaceAccentedLetter() throws Exception {
		String[][] doc = { { "zitat", "únser" } };
		solr.addDocument(doc);

		solr.list("zitat:unser");

		assertEquals(1, results());
	}

	@Test
	public void shouldRemoveCombinedLetter() throws Exception {
		String[][] doc = { { "zitat", "svͤlen" } };
		solr.addDocument(doc);

		solr.search("zitat:svlen");

		assertEquals(1, results());
	}

	@Test
	public void shouldHighlightChristDifferently() throws Exception {
		String[][] doc = { { "artikel", "christ krist" }, { "artikel_text", "christ krist" },
				{ "zitat", "christ krist" }, { "zitat_text", "christ krist" } };
		solr.addDocument(doc);

		solr.search("christ");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "christ");
		assertNotHighlighted("artikel_text", "krist");
		assertHighlighted("zitat_text", "christ", "krist");
	}

	@Test
	public void shouldHighlightArticleAndCitationDifferently() throws Exception {
		String[][] doc = { { "artikel", "und vnd" }, { "zitat", "und vnd" }, { "artikel_text", "und vnd" },
				{ "zitat_text", "und vnd" } };
		solr.addDocument(doc);

		solr.search("und");

		assertEquals(1, results());
		assertHighlighted("artikel_text", "und");
		assertNotHighlighted("artikel_text", "vnd");
		assertHighlighted("zitat_text", "und", "vnd");
	}

	@Test
	public void shouldFindPartialAlternativeSpellings() throws Exception {
		String[][] doc = { { "zitat", "wvnde" } };
		solr.addDocument(doc);

		solr.search("zitat:unt");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindAlternativeSpellings() throws Exception {
		String[][] doc = { { "zitat", "vnd katze" } };
		solr.addDocument(doc);

		solr.select("zitat:(+und +unt +vnt +vnd +katze +chatze +qatze +catze +gedza)");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindUmlaut() throws Exception {
		String[][] doc = { { "artikel_text", "bär" } };
		solr.addDocument(doc);

		solr.select("artikel_text:bar");

		assertEquals(1, results());
	}

	@Test
	public void shouldDeleteNonbreakingSpace() throws Exception {
		String[][] doc = { { "artikel_text", "test abc" } };
		solr.addDocument(doc);

		solr.select("artikel_text:test");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindPipe() throws Exception {
		String[][] doc = { { "artikel_text", "test |" } };
		solr.addDocument(doc);

		solr.select("artikel_text:|");

		assertEquals(1, results());
	}

	@Test
	public void shouldIgnoreSpecialChars() throws Exception {
		String[][] doc = { { "artikel_text", "& test1, ›test2‹" } };
		solr.addDocument(doc);

		solr.select("artikel_text:test1");
		assertEquals(1, results());
		solr.select("artikel_text:test2");
		assertEquals(1, results());
		solr.select("artikel_text:&");
		assertEquals(0, results());
	}

	@Test
	public void shouldFindWithoutPipe() throws Exception {
		String[][] doc = { { "lemma", "my|lemma" } };
		solr.addDocument(doc);

		solr.search("lemma:mylemma");

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
		// System.out.println(hlText);
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
