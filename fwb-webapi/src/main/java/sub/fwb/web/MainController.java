package sub.fwb.web;

import java.io.IOException;

import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class MainController {

	private GitWrapper git = new GitWrapper();
	private Environment env = new Environment();
	private LogAccess logAccess = new LogAccess();
	private LockFile lock = new LockFile();
	private ImporterRunner runner = new ImporterRunner();

	@RequestMapping(method = RequestMethod.GET, value = "/test2")
	@ResponseBody
	public ResponseEntity<?> getTest() {
		return ResponseEntity.ok().body("some test");
	}

	@RequestMapping(value = "/")
	public String index(Model model) throws Exception {

		model.addAttribute("log", logAccess.getLogContents());
		if (lock.exists()) {
			return "started";
		}

		String lastMessage = "";
		try{
			git.init();
			git.pull();
			lastMessage = git.getLastCommitMessage();
		} catch(Exception e) {
			lastMessage = "Not a git directory.";
		}
		model.addAttribute("commitMessage", lastMessage);

		return "index";
	}

	@RequestMapping(value = "/importstaging")
	public String importstaging(Model model) throws IOException {
		if (lock.exists()) {
			model.addAttribute("log", logAccess.getLogContents());
			return "started";
		}
		new Thread(runner).start();
		lock.create();
		return "started";
	}

	// for unit testing
	void setGit(GitWrapper newGit) {
		git = newGit;
	}
	void setLogAccess(LogAccess newLogAccess) {
		logAccess = newLogAccess;
	}
	void setLock(LockFile newLock) {
		lock = newLock;
	}
	void setImporterRunner(ImporterRunner newRunner) {
		runner = newRunner;
	}
}
