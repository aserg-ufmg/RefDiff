package refdiff.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

@Controller
@EnableAutoConfiguration
public class WebUi {
	
	@RequestMapping("/")
	String home() {
		return "main";
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(WebUi.class, args);
		diff();
	}
	
	public static void diff() throws DiffException {
		
		DiffRowGenerator generator = DiffRowGenerator.create()
			.showInlineDiffs(true)
			.inlineDiffByWord(true)
			.oldTag(f -> "~")
			.newTag(f -> "**")
			.build();
		List<DiffRow> rows = generator.generateDiffRows(
			Arrays.asList("This is a test senctence.", "This is the second line.", "And here is the finish."),
			Arrays.asList("This is a test for diffutils.", "This is the second line."));
		
		System.out.println("|original|new|");
		System.out.println("|--------|---|");
		for (DiffRow row : rows) {
			System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
		}
	}
	
}
