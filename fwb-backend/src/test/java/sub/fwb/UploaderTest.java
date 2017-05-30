package sub.fwb;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UploaderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void test() throws SolrServerException, IOException {
		Uploader up = new Uploader();
		up.setSolrUrl("http://localhost:8983/solr/fwb");
		up.reloadCore();
	}

}
