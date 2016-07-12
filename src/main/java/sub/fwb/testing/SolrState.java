package sub.fwb.testing;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrState {

	private SolrClient solrServerClient;
	private String solrQueryString = "";
	private SolrDocumentList docList;

	public SolrState(SolrClient newSolrThing) {
		solrServerClient = newSolrThing;
	}

	public void ask(String... userInputs) throws Exception {
		solrQueryString = "";
		for (String inputValue : userInputs) {
			if (inputValue.startsWith("\"")) {
				solrQueryString += inputValue + " " + "+article_fulltext:" + inputValue + " ";
			} else {
				solrQueryString += inputValue + " *" + inputValue + "* " + "+article_fulltext:*" + inputValue + "* ";
			}
		}
		askByQuery(solrQueryString);
	}

	public void askByQuery(String query) throws Exception {
		askByQuery(query, "/select");
	}

	public void askByQuery(String query, String requestHandler) throws Exception {
		SolrQuery solrQuery = new SolrQuery(query);
		solrQuery.setRequestHandler(requestHandler);
		solrQuery.set("fl", "lemma,score");
		solrQuery.set("rows", "500");
		// solrQuery.set("tie", "0.01");
		solrQuery.set("qf",
				"lemma^10000 neblem^1000 definition_fulltext^70 article_related_lemma^60 "
						+ "definition_source_citation^55 sense_phraseme^45 sense_word_reference^45 "
						+ "sense_antonym^25 sense_symptom_value^40 sense_syntagma^10 sense_word_formation^20 "
						+ "sense_related_reference^20");

		QueryResponse response = solrServerClient.query(solrQuery);

		docList = response.getResults();
	}

	public String lemma(int resultNumber) {
		return (String) docList.get(resultNumber - 1).getFieldValue("lemma");
	}

	public long results() {
		return docList.getNumFound();
	}

	public int askForNumberOfLemmas(String wordPart) throws Exception {
		SolrState tempSolr = new SolrState(solrServerClient);
		tempSolr.askByQuery("lemma:*" + wordPart + "*");

		return (int) tempSolr.results();
	}

	public void printResults() {
		System.out.println();
		System.out.println(solrQueryString);
		System.out.println(docList.getNumFound() + " results");
		for (int i = 0; i < 20; i++) {
			if (i < docList.getNumFound()) {
				SolrDocument doc = docList.get(i);
				System.out.println(doc.getFieldValue("lemma") + "\t" + doc.getFieldValue("score"));
			}
		}
	}

	public void addDocument(String[][] documentFields) throws Exception {
		SolrInputDocument newDoc = new SolrInputDocument();
		for (String[] docField : documentFields) {
			newDoc.addField(docField[0], docField[1]);
		}
		if (!newDoc.containsKey("id")) {
			newDoc.addField("id", "1234");
		}
		if (!newDoc.containsKey("type")) {
			newDoc.addField("type", "artikel");
		}
		if (!newDoc.containsKey("lemma")) {
			newDoc.addField("lemma", "mylemma");
		}
		solrServerClient.add(newDoc);
		solrServerClient.commit();
	}

	public void clean() throws Exception {
		solrServerClient.deleteByQuery("*:*");
		solrServerClient.commit();
	}

}
