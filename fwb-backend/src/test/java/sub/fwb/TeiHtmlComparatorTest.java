package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.ComparisonFailure;
import sub.fwb.testing.TeiHtmlComparator;

public class TeiHtmlComparatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldAcceptEqualTexts() throws IOException {

		File tei = new File("src/test/resources/textComparisons/lemma_tei.xml");
		File solr = new File("src/test/resources/textComparisons/lemma_solr.xml");

		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

	@Test(expected=ComparisonFailure.class)
	public void shouldRejectDifferentTexts() throws IOException {

		File tei = new File("src/test/resources/textComparisons/lemma_tei.xml");
		File solr = new File("src/test/resources/textComparisons/lemma_solr_different_text.xml");

		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

	@Ignore
	@Test
	public void test2() throws IOException {

		File tei = new File("/home/dennis/temp/test/munzfreiheit.münzfreiheit.s.1f.xml");
		File solr = new File("/home/dennis/temp/out/munzfreiheit.münzfreiheit.s.1f.xml");

		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

}
