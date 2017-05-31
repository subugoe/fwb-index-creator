package sub.fwb;

import static org.mockito.Mockito.*;

public class MyMocks {

	public SourcesParser sourcesParser = mock(SourcesParser.class);
	public WordTypesGenerator wordTyper = mock(WordTypesGenerator.class);
	public Xslt xslt = mock(Xslt.class);
	public FileAccess fileAccess = mock(FileAccess.class);
	public Uploader uploader = mock(Uploader.class);

	public MyMocks(Importer importer) {
		importer.setFileAccess(fileAccess);
		importer.setSourcesParser(sourcesParser);
		importer.setWordTyper(wordTyper);
		importer.setXslt(xslt);
		importer.setUploader(uploader);
	}

}
