package at.ac.tuwien.iter.modules;

import java.util.List;

import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.slf4j.Logger;

import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.AssertionService;

/**
 * The assertion module contains the tapestry-ioc managed classes that define
 * the assertion framework
 * 
 * This module is a SubModule of {@link IterModule}
 * 
 * @author alessiogambi
 * 
 */
public class AssertionModule {

	/**
	 * The AssertionService implements the Chain of Command pattern. Each
	 * assertion is checked and has the possibility to interrupt the whole
	 * checking process. The idea is to register several assertions to be
	 * checked against the test result to produce a final report for the user.
	 * 
	 * @param commands
	 * @param chainBuilder
	 * @return
	 */
	@Scope(ScopeConstants.PERTHREAD)
	public static AssertionService build(final Logger logger,
			List<AssertionService> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {

		if (logger.isDebugEnabled()) {
			logger.info("AssertionModule.build() from : ");
			for (AssertionService assertion : commands) {
				logger.info("\t " + assertion.getClass());
			}
		}
		commands.add(new AssertionService() {

			public void check(TestResult testResult) {
				logger.debug("Empty assertion");
			}
		});
		return chainBuilder.build(AssertionService.class, commands);
	}
}
