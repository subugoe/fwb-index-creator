package sub.fwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class Xslt {
    private Processor processor = new Processor(false);
    private XsltExecutable exe;
    
	public Xslt(String xsltPath) throws SaxonApiException, FileNotFoundException {
        this(new FileInputStream((new File(xsltPath))));
	}
	
	public Xslt(InputStream xsltStream) throws SaxonApiException {
        XsltCompiler comp = processor.newXsltCompiler();
        exe = comp.compile(new StreamSource(xsltStream));
	}
	
	public void transform(String inputXmlPath, OutputStream outputXmlStream) throws SaxonApiException {
        XdmNode source = processor.newDocumentBuilder().build(new StreamSource(new File(inputXmlPath)));
        Serializer out = processor.newSerializer();
        out.setOutputStream(outputXmlStream);
        XsltTransformer transformer = exe.load();
        transformer.setInitialContextNode(source);
        transformer.setDestination(out);
        transformer.transform();
	}
}
