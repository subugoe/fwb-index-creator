package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;

public class Uploader {

	private XMLEventReader eventReader;
	private SolrInputDocument currentSolrDoc;
	private List<SolrInputDocument> allDocs = new ArrayList<>();
	private final int MAX_DOCS = 2000;
	private SolrClient solr;
	private String coreUrl;

	private Set<String> ids = new HashSet<>();

	public Uploader(String solrUrl) {
		solr = new HttpSolrClient(solrUrl);
		coreUrl = solrUrl;
	}

	public void add(File file) throws SolrServerException, IOException {
		InputStream is = new FileInputStream(file);
		try {
			add(is);
		} finally {
			is.close();
		}
	}

	public void add(InputStream is) throws SolrServerException, IOException {
		XMLInputFactory factory = XMLInputFactory.newInstance();

		try {
			eventReader = factory.createXMLEventReader(is);

			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();

				switch (event.getEventType()) {

				case XMLStreamConstants.START_ELEMENT:
					handleStartElement(event.asStartElement());
					break;
				case XMLStreamConstants.END_ELEMENT:
					handleEndElement(event.asEndElement());
					break;
				default:
					// ignore all of the other events
				}
			}

			eventReader.close();

		} catch (XMLStreamException e) {
			throw new IllegalArgumentException("Error reading XML", e);
		}

		if (allDocs.size() >= MAX_DOCS) {
			flushDocs();
		}
	}

	private void handleStartElement(StartElement startTag) throws XMLStreamException {
		String name = startTag.getName().getLocalPart();
		if ("doc".equals(name)) {
			currentSolrDoc = new SolrInputDocument();
		}

		if (name.equals("field")) {
			String fieldName = startTag.getAttributeByName(new QName("name")).getValue();
			XMLEvent nextEvent = eventReader.peek();
			if (nextEvent.isCharacters()) {
				String fieldValue = nextEvent.asCharacters().getData();
				currentSolrDoc.addField(fieldName, fieldValue);

				if (fieldName.equals("id")) {
					if (ids.contains(fieldValue)) {
						// System.out.println("double id: " + fieldValue);
					}
					ids.add(fieldValue);
				}
			}
		}

	}

	private void handleEndElement(EndElement endTag) {
		String name = endTag.getName().getLocalPart();
		if ("doc".equals(name)) {
			allDocs.add(currentSolrDoc);
		}
	}

	private void flushDocs() throws SolrServerException, IOException {
		if (!allDocs.isEmpty()) {
			solr.add(allDocs);
			allDocs.clear();
			allDocs = new ArrayList<>();
		}
	}

	public void cleanSolr() throws SolrServerException, IOException {
		solr.deleteByQuery("*:*");
	}

	public void reloadCore() throws SolrServerException, IOException {
		String urlWithoutCore = coreUrl.replaceAll("/[^/]+$", "");
		String coreName = coreUrl.replaceAll("^.*/", "");
		CoreAdminRequest adminRequest = new CoreAdminRequest();
		adminRequest.setAction(CoreAdminAction.RELOAD);
		adminRequest.setCoreName(coreName);
		adminRequest.process(new HttpSolrClient(urlWithoutCore));
	}

	public void commitToSolr() throws SolrServerException, IOException {
		flushDocs();
		solr.commit();
		solr.optimize();
	}

	public void rollbackChanges() {
		try {
			solr.rollback();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}
}
