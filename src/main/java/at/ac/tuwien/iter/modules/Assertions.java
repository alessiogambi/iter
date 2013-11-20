package at.ac.tuwien.iter.modules;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.slf4j.Logger;

import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.MathEngineDao;
import at.ac.tuwien.iter.services.impl.assertions.AbsoluteFailedRequestAssertion;
import at.ac.tuwien.iter.services.impl.assertions.DoodleWebServiceAvgResponseTimeAssertion;
import at.ac.tuwien.iter.services.impl.assertions.FailedRequestAssertion;
import at.ac.tuwien.iter.services.impl.assertions.InelasticityAssertion;
import at.ac.tuwien.iter.services.impl.assertions.PlasticityAssertion;
import at.ac.tuwien.iter.services.impl.assertions.RelativeFailedRequestAssertion;
import at.ac.tuwien.iter.services.impl.datacollector.DatabaseManagerService;

/**
 * This module is where we conveniently collect user provided assertions to be
 * run by the software.
 * 
 * We have not placed the contributions inside the {@link AssertionModule} to
 * improve the testability of the code.
 * 
 * @author alessiogambi
 * 
 */
public class Assertions {

	@Contribute(AssertionService.class)
	public static void addAssertions(
			OrderedConfiguration<AssertionService> configuration,
			Logger logger, MathEngineDao dao, DatabaseManagerService dbService) {

		// We do not care about order constraints for the moment
		configuration.add("Plasticity", new PlasticityAssertion(logger, dao));

		configuration.add("Inelasticity",
				new InelasticityAssertion(logger, dao));

		// Assertions over the Clients Requests ! Note that those require the
		// output being a valid JMeter XML !
		configuration.add("NoFailedRequests", new FailedRequestAssertion(
				logger, dbService));
		// Absolute Values
		configuration.add("Max10RequestsFailed",
				new AbsoluteFailedRequestAssertion(logger, dbService, 10));
		configuration.add("Max100RequestsFailed",
				new AbsoluteFailedRequestAssertion(logger, dbService, 10));
		// Relative Values
		configuration
				.add("Max1PercRequestsFailed",
						new RelativeFailedRequestAssertion(logger, dbService,
								1 / 100.0));
		configuration
				.add("Max5PercRequestsFailed",
						new RelativeFailedRequestAssertion(logger, dbService,
								5 / 100.0));
		configuration.add("Max10PercRequestsFailed",
				new RelativeFailedRequestAssertion(logger, dbService,
						10 / 100.0));

		// QoS Related - thresholds can be read as well by file. TODO
		// NOTE THAT THIS IS SPECIFIC OF THE APPLICATION, SO WE MUST DEAL WITH
		// THIS IN A DIFFERENT WAY IN THE FUTURE
		double avgCreatePollMillis = 2000.0;
		double avgDeletePollMillis = 1500.0;
		double avgGetAllPollsMillis = 2000.0;
		double avgGetPollMillis = 500.0;
		double avgVotePollMillis = 1000.0;

		// THE ORDER HERE IS IMPORTATN !!!
		double[] thresholds = new double[] { avgCreatePollMillis,
				avgDeletePollMillis, avgGetAllPollsMillis, avgGetPollMillis,
				avgVotePollMillis };
		configuration.add("AvgRT",
				new DoodleWebServiceAvgResponseTimeAssertion(logger, dbService,
						thresholds));

	}
}
