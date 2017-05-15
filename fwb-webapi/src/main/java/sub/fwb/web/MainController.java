package sub.fwb.web;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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
	private Importer importer = new Importer();

	@RequestMapping(method = RequestMethod.GET, value = "/test2")
	@ResponseBody
	public ResponseEntity<?> getTest() {
		return ResponseEntity.ok().body("some test");
	}

	@RequestMapping(value = "/")
	public String index(Model model) throws IOException, WrongRepositoryStateException, InvalidConfigurationException,
			DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException,
			RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {

		GitWrapper git = new GitWrapper();
		git.pull();
		String lastMessage = git.getLastCommitMessage();

		model.addAttribute("commitMessage", lastMessage);

		return "index";
	}

	@RequestMapping(value = "/importstaging")
	public String importstaging(@RequestParam("mailaddress") String mail, Model model) {
		String solrStagingUrl = env.getVariable("SOLR_STAGING_URL");
		model.addAttribute("processingMessage", "In Kürze wird ein Bericht verschickt an: " + mail);
		return "message";
	}
}
