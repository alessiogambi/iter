package at.ac.tuwien.iter.modules;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.slf4j.Logger;

import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.MathEngineDao;
import at.ac.tuwien.iter.services.impl.assertions.DoodleWebServiceAvgResponseTimeAssertion;
import at.ac.tuwien.iter.services.impl.assertions.FailedRequestAssertion;
import at.ac.tuwien.iter.services.impl.datacollector.DatabaseManagerService;

/**
 * Use only a subset of possible assertions during some of the tests
 * 
 * @author alessiogambi
 * 
 */
public class TestAssertions {

	@Contribute(AssertionService.class)
	public static void addAssertions(
			OrderedConfiguration<AssertionService> configuration,
			Logger logger, MathEngineDao dao, DatabaseManagerService dbService) {

		// Assertions over the Clients Requests ! Note that those require the
		// output being a valid JMeter XML !
		configuration.add("NoFailedRequests", new FailedRequestAssertion(
				logger, dbService));
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
