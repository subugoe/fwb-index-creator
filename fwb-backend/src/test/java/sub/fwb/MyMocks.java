package sub.fwb;

import static org.mockito.Mockito.*;

public class MyMocks {

	public SourcesParser sourcesParser = mock(SourcesParser.class);
	public WordTypesGenerator wordTyper = mock(WordTypesGenerator.class);
	public Xslt xslt = mock(Xslt.class);
	public FileAccess fileAccess = mock(FileAccess.class);

	public MyMocks() {
		
	}
}
