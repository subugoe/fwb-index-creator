package sub.fwb;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IndexerTest {
	
	private OutputStream outputBaos;
	private static Xslt xslt;
	
	@BeforeClass
	public static void beforeAllTests() throws Exception {
		xslt = new Xslt("src/main/resources/fwb-indexer.xslt");
	}
	
	@Before
	public void beforeEachTest() throws Exception {
		outputBaos = new ByteArrayOutputStream();
	}

	@After
	public void afterEachTest() {
        //System.out.println(outputBaos.toString());	
	}
	
	@Test
	public void shouldTransformPrintedSource() throws Exception {
		xslt.transform("src/test/resources/printedSource.xml", outputBaos);
        String result = outputBaos.toString();

        assertXpathEvaluatesTo("artikel", "//field[@name='type']", result);
        assertXpathEvaluatesTo("Some printed source", "//field[@name='printedSource']", result);
        assertXpathEvaluatesTo("08", "//field[@name='volume']", result);
        assertXpathEvaluatesTo("1", "//field[@name='col']", result);
	}

}