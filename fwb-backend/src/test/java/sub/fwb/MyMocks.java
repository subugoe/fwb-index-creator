package sub.fwb;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyMocks {

	public SourcesParser sourcesParser = mock(SourcesParser.class);
	public WordTypesGenerator wordTyper = mock(WordTypesGenerator.class);
	public Xslt xslt = mock(Xslt.class);
	public FileAccess fileAccess = mock(FileAccess.class);

	public MyMocks() {
		List<File> files = new ArrayList<>();
		files.add(new File("/teis/file1"));
		files.add(new File("/teis/file2"));
		when(fileAccess.getAllXmlFilesFromDir(any(File.class))).thenReturn(files);
	}
}
