package sub.fwb;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.sf.saxon.s9api.*;

public class IndexerTest {
	
	private OutputStream outputBaos;

	@Before
	public void setUp() throws Exception {
		outputBaos = new ByteArrayOutputStream();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws SaxonApiException {
        Processor proc = new Processor(false);
        XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exp = comp.compile(new StreamSource(new File("src/main/resources/fwb-indexer.xslt")));
        XdmNode source = proc.newDocumentBuilder().build(new StreamSource(new File("src/test/resources/test.xml")));
        Serializer out = proc.newSerializer();
        out.setOutputProperty(Serializer.Property.METHOD, "xml");
        out.setOutputProperty(Serializer.Property.INDENT, "yes");
        out.setOutputStream(outputBaos);
        XsltTransformer trans = exp.load();
        trans.setInitialContextNode(source);
        trans.setDestination(out);
        trans.transform();

        System.out.println(outputBaos.toString());
	}

}
