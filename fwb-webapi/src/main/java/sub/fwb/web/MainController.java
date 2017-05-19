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

import sub.fwb.Importer;

@Controller
public class MainController {

	private Environment env = new Environment();
	private LogAccess logAccess = new LogAccess();
	private LockFile lock = new LockFile();

	@RequestMapping(method = RequestMethod.GET, value = "/test2")
	@ResponseBody
	public ResponseEntity<?> getTest() {
		return ResponseEntity.ok().body("some test");
	}

	@RequestMapping(value = "/")
	public String index(Model model) throws IOException, WrongRepositoryStateException, InvalidConfigurationException,
			DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException,
			RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {

		model.addAttribute("log", logAccess.getLogContents());
		if (lock.exists()) {
			return "started";
		}

		GitWrapper git = new GitWrapper();
		git.pull();
		String lastMessage = git.getLastCommitMessage();
		model.addAttribute("commitMessage", lastMessage);

		return "index";
	}

	@RequestMapping(value = "/importstaging")
	public String importstaging(@RequestParam("mailaddress") String mail, Model model) throws IOException {
		if (lock.exists()) {
			model.addAttribute("log", logAccess.getLogContents());
			return "started";
		}
		model.addAttribute("processingMessage", "In Kürze wird ein Bericht verschickt an: " + mail);
		ImporterRunner runner = new ImporterRunner();
		new Thread(runner).start();
		lock.create();
		return "started";
	}
}
