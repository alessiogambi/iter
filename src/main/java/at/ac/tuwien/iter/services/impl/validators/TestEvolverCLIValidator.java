package at.ac.tuwien.iter.services.impl.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gambi.tapestry5.cli.data.CLIOption;
import org.gambi.tapestry5.cli.services.CLIValidator;

import at.ac.tuwien.iter.services.TestSuiteEvolverSource;

/*
 * This validator is contributed to the CLI Validation pipeline inside CLI Parser. This class is an example of providing validators without relying on JSR-303
 * 
 */
public class TestEvolverCLIValidator implements CLIValidator {
	private TestSuiteEvolverSource testSuiteEvolverSource;

	public TestEvolverCLIValidator(TestSuiteEvolverSource testSuiteEvolverSource) {
		this.testSuiteEvolverSource = testSuiteEvolverSource;
	}

	private CLIOption getOption(Map<CLIOption, String> options) {

		for (CLIOption cliOption : options.keySet()) {
			if (cliOption.getLongOpt().equals("evolve-with")) {
				return cliOption;
			}
		}
		return null;
	}

	public void validate(Map<CLIOption, String> options, List<String> inputs,
			List<String> accumulator) {

		List<String> validationError = new ArrayList<String>();
		CLIOption o = getOption(options);
		if (o == null) {
			return;
		} else {
			String evolveWith = o.getValue();
			try {

				if (testSuiteEvolverSource.getTestSuiteEvolver(evolveWith) == null) {
					validationError
							.add("TestEvolverCLIValidator: the provided ID ("
									+ evolveWith
									+ ") is not a valid name for TestEvolver !");
				}
			} catch (Throwable e) {
				// Register validation
				validationError
						.add("TestEvolverCLIValidator: the provided ID ("
								+ evolveWith
								+ ") is not a valid name for TestEvolver !");
			}
		}
	}

}
