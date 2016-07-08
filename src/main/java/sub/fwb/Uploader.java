package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.apache.solr.common.SolrInputDocument;

public class Uploader {

	private XMLEventReader eventReader;
	private SolrInputDocument currentSolrDoc;
	private List<SolrInputDocument> allDocs = new ArrayList<>();
	private final int MAX_DOCS = 2000;
	private SolrClient solr;
	
	private Set<String> ids = new HashSet<>();
	
	public Uploader(String solrUrl) {
		solr = new HttpSolrClient(solrUrl);
	}

	public static void main(String[] args) throws SolrServerException, IOException {
		File inputDir = new File(args[0]);
		String solrUrl = args[1];
		File[] xmls = inputDir.listFiles();
		Uploader uploader = new Uploader(solrUrl);
		uploader.cleanSolr();
		System.out.println("Uploading files:");
		for (File x : xmls) {
			uploader.add(x);
		}
		uploader.commitToSolr();
	}
	
	public void add(File file) throws SolrServerException, IOException {
		XMLInputFactory factory = XMLInputFactory.newInstance();

		FileInputStream is = new FileInputStream(file);
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
		} finally {
			is.close();
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
			System.out.print(" ..." + ids.size());
		}
	}
	
	public void cleanSolr() {
		try {
			solr.deleteByQuery("*:*");
			solr.commit();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void commitToSolr() {
		try {
			flushDocs();
			solr.commit();
			System.out.println();
			System.out.println("Done");
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}
}
