package refdiff.viewer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DiffController {
	
	@GetMapping("/diff")
	public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) throws Exception {
		
		model.addAttribute("name", name);
		return "diff";
	}
}
