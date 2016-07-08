package sub.fwb;

import static org.junit.Assert.assertEquals;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

	@Ignore
	@Test
	public void shouldFindPartialAlternativeSpellings() throws Exception {
		String[][] doc = { { "definition_source_citation", "wvnde" } };
		solr.addDocument(doc);

		solr.askByQuery("definition_source_citation:*unt*");

		assertEquals(1, results());
	}

	@Ignore
	@Test
	public void shouldFindAlternativeSpellings() throws Exception {
		String[][] doc = { { "definition_source_citation", "vnd katze" } };
		solr.addDocument(doc);

		solr.askByQuery("definition_source_citation:(+und +unt +vnt +vnd +katze +chatze +qatze +catze +gedza)");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindUmlaut() throws Exception {
		String[][] doc = { { "article_fulltext", "bär" } };
		solr.addDocument(doc);

		solr.askByQuery("article_fulltext:bar");

		assertEquals(1, results());
	}

	@Test
	public void shouldDeleteNonbreakingSpace() throws Exception {
		String[][] doc = { { "article_fulltext", "test abc" } };
		solr.addDocument(doc);

		solr.askByQuery("article_fulltext:test");

		assertEquals(1, results());
	}

	@Test
	public void shouldFindPipe() throws Exception {
		String[][] doc = { { "article_fulltext", "test |" } };
		solr.addDocument(doc);

		solr.askByQuery("article_fulltext:|");

		assertEquals(1, results());
	}

	@Test
	public void shouldIgnoreSpecialChars() throws Exception {
		String[][] doc = { { "article_fulltext", "& test1, ›test2‹" } };
		solr.addDocument(doc);

		solr.askByQuery("article_fulltext:test1");
		assertEquals(1, results());
		solr.askByQuery("article_fulltext:test2");
		assertEquals(1, results());
		solr.askByQuery("article_fulltext:&");
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

}
