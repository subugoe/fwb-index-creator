package sub.fwb.web;

import static org.mockito.Mockito.*;

import sub.fwb.CoreSwapper;

public class MyMocks {
	public GitWrapper git = mock(GitWrapper.class);
	public LogAccess logAccess = mock(LogAccess.class);
	public LockFile lock = mock(LockFile.class);
	public ImporterRunner runner = mock(ImporterRunner.class);
	public Environment env = mock(Environment.class);
	public CoreSwapper swapper = mock(CoreSwapper.class);

	public MyMocks(MainController mc) {
		mc.setGit(git);
		mc.setLogAccess(logAccess);
		mc.setLock(lock);
		mc.setImporterRunner(runner);
		mc.setEnvironment(env);
		mc.setCoreSwapper(swapper);
	}
}
