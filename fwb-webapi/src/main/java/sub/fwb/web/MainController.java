package sub.fwb.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

	@RequestMapping(method = RequestMethod.GET, value = "/test2")
	@ResponseBody
	public ResponseEntity<?> getTest() {
		return ResponseEntity.ok().body("some test");
	}

	@RequestMapping(value="/")
	public String index(Model model) {
		model.addAttribute("test", "blablub");
		System.out.println("inside222222222");
		return "index";
	}

	@RequestMapping(value="/importstaging")
	public String importstaging(@RequestParam("mailaddress") String mail, Model model) {
		model.addAttribute("processingMessage", "In Kürze wird ein Bericht verschickt an: " + mail);
		return "message";
	}
}
