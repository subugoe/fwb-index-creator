package sub.fwb;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import sub.fwb.testing.SolrState;

public class SolrCleaner {
	
	private static String solrUrl = "http://localhost:8983/solr/fwb";

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			solrUrl = args[0];
		}
		
		SolrClient solrServerClient = new HttpSolrClient(solrUrl);
		SolrState solr = new SolrState(solrServerClient);
		
		solr.clean();
	}

}
