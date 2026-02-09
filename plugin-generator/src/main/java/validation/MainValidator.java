package validation;

public class MainValidator {
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: MainValidator <language>");
			System.err.print("Available languages: ");
			for (plugingenerator.Language l : plugingenerator.Language.values()) {
				System.err.print(l.getName() + " ");
			}
			System.err.println();
			System.exit(1);
		}

		String langName = args[0];
		try {
			plugingenerator.Language lang = plugingenerator.Language.fromName(langName);
			switch (lang) {
				case JAVA:
					new validation.langValidator.JavaValidator().run();
					break;
				case C:
					new validation.langValidator.CValidator().run();
					break;
				case JS:
					new validation.langValidator.JsValidator().run();
					break;
				case PYTHON:
					new validation.langValidator.PythonValidator().run();
					break;
				case GO:
					new validation.langValidator.GoValidator().run();
					break;
				case PHP:
					new validation.langValidator.PhpValidator().run();
					break;
				case RUBY:
					new validation.langValidator.RubyValidator().run();
					break;
				default:
					System.err.println("No validator available for: " + langName);
					System.exit(2);
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Unknown language: " + langName);
			System.err.print("Available: ");
			for (plugingenerator.Language l : plugingenerator.Language.values()) {
				System.err.print(l.getName() + " ");
			}
			System.err.println();
			System.exit(1);
		}
	}
}
