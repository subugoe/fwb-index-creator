package sub.fwb;

import java.io.IOException;
import java.util.Date;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;

import sub.fwb.testing.EmbeddedSolr;

public class CoreSwapper {

	private SolrClient solr;
	private String core;

	public void setSolrEndpoint(String solrUrl, String coreName) {
		if ("embedded".equals(solrUrl)) {
			solr = EmbeddedSolr.instance;
		} else {
			solr = new HttpSolrClient(solrUrl);
		}
		core = coreName;
	}

	public String getCoreDate() throws SolrServerException, IOException {
		CoreAdminRequest adminRequest = new CoreAdminRequest();
		adminRequest.setAction(CoreAdminAction.STATUS);
		adminRequest.setCoreName(core);
		CoreAdminResponse response = adminRequest.process(solr);
		return response.getCoreStatus().findRecursive(core, "index", "lastModified").toString();
	}

	public void switchTo(String swapCore) throws SolrServerException, IOException {
		CoreAdminRequest adminRequest = new CoreAdminRequest();
		adminRequest.setAction(CoreAdminAction.SWAP);
		adminRequest.setCoreName(core);
		adminRequest.setOtherCoreName(swapCore);
		adminRequest.process(solr);
	}

}