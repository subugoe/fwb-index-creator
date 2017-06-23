package sub.fwb;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import sub.fwb.testing.SolrWrapper;

public class SolrCleaner {

	private static String solrUrl = "http://localhost:8983/solr";

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			solrUrl = args[0];
		}

		SolrClient solrServerClient = new HttpSolrClient(solrUrl);
		SolrWrapper solr = new SolrWrapper(solrServerClient, "fwb");

		solr.clean();
	}

}
