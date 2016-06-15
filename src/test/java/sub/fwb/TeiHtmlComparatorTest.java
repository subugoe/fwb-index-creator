package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TeiHtmlComparatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		
		File tei = new File("src/test/resources/textComparisons/lemma_tei.xml");
		File solr = new File("src/test/resources/textComparisons/lemma_solr.xml");
		
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

	@Ignore
	@Test
	public void test2() throws IOException {
		
		File tei = new File("/home/dennis/temp/in_tei/i/imb/imbis.imbis.s.0m.xml");
		File solr = new File("/home/dennis/temp/out/imbis.imbis.s.0m.xml");
		
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

}
