package sub.fwb.testing;

import static org.junit.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SolrTester {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void imbs() throws Exception {
		String xml = askSolr(
				"q=(lemma%3Aimbs+neblem%3Aimbs+article_fulltext%3Aimbs+article_fulltext%3A*imbs*+)+AND+article_fulltext%3A*imbs*+%0A&sort=score+desc%2Clemma+asc&rows=10&fl=lemma%2Cscore%2Cneblem&wt=xml&indent=true&hl=off&omitHeader=true");

		assertXpathEvaluatesTo("imbs", "//doc[1]/str", xml);
		assertXpathEvaluatesTo("imbis", "//doc[2]/str", xml);
		assertXpathEvaluatesTo("anheimsch", "//doc[3]/str", xml);
	}


	
	
	
	
	private String askSolr(String query) throws IOException {
		
		URL queryUrl = new URL("http://localhost:8983/solr/fwb/select?" + query);
		URLConnection connection = queryUrl.openConnection();

		String s = IOUtils.toString(connection.getInputStream());

		System.out.println(s);
		return s;
	}

}
