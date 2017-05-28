package sub.fwb;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileAccessTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		FileAccess access = new FileAccess();
		List<File> files = access.getAllXmlFilesFromDir(new File("src/test/resources"));
		assertThat(files.size(), greaterThan(0));
		for (File f : files) {
			// System.out.println(f.getAbsolutePath());
		}
	}

}
