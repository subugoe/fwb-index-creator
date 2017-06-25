package sub.fwb;

import static org.junit.Assert.*;

import org.junit.Test;

public class CoreSwapperTest {

	// @Test
	public void test() throws Exception {
		CoreSwapper swapper = new CoreSwapper();
		swapper.setSolrEndpoint("http://localhost:8983/solr", "fwboffline");
		swapper.switchTo("fwb");
	}

	// @Test
	public void testStatus() throws Exception {
		CoreSwapper swapper = new CoreSwapper();
		swapper.setSolrEndpoint("http://localhost:8983/solr", "fwboffline");
		System.out.println(swapper.getCoreDate());
		swapper.setSolrEndpoint("http://localhost:8983/solr", "fwb");
		System.out.println(swapper.getCoreDate());
	}

}
