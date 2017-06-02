package sub.fwb.web;

import static org.mockito.Mockito.*;

public class MyMocks {
	public GitWrapper git = mock(GitWrapper.class);
	public LogAccess logAccess = mock(LogAccess.class);
	public LockFile lock = mock(LockFile.class);
	public ImporterRunner runner = mock(ImporterRunner.class);

	public MyMocks(MainController mc) {
		mc.setGit(git);
		mc.setLogAccess(logAccess);
		mc.setLock(lock);
		mc.setImporterRunner(runner);
	}
}
