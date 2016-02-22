package sub.fwb;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

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

	@Test
	public void test() throws Exception {
        
        xslt.transform("src/test/resources/test.xml", outputBaos);

        System.out.println(outputBaos.toString());
	}

}
