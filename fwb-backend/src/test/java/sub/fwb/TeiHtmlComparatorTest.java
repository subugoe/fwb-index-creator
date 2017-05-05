package sub.fwb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sub.fwb.testing.TeiHtmlComparator;

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
		
		File tei = new File("/home/dennis/temp/in_tei11/b/bes/besetzung.besetzung.s.1f.xml");
		File solr = new File("/home/dennis/temp/out/besetzung.besetzung.s.1f.xml");
		
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

	//@Test
	public void test3() throws IOException {
		
		File tei = new File("/home/dennis/temp/in_tei11/b/bar/baren.bären.rI.3vu.xml");
		File solr = new File("/home/dennis/temp/out/baren.bären.rI.3vu.xml");
		
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

	//@Test
	public void test4() throws IOException {
		
		File tei = new File("/home/dennis/temp/in_tei11/g/gut/gut.gut.s.4adj.xml");
		File solr = new File("/home/dennis/temp/out/gut.gut.s.4adj.xml");
		
		TeiHtmlComparator comparator = new TeiHtmlComparator();
		comparator.compareTexts(tei, solr);
	}

}
