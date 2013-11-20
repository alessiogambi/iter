package at.ac.tuwien.iter.services.impl.datacollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.ioc.internal.services.SystemPropertiesSymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class TransitionSequenceCollectorTest {

	@Test
	public void collect() {

		List<SymbolProvider> providers = new ArrayList<SymbolProvider>();
		providers.add(new SystemPropertiesSymbolProvider());

		TransitionSequenceCollector collector = new TransitionSequenceCollector(
				LoggerFactory.getLogger(TransitionSequenceCollector.class),
				"ale", "ale");

		at.ac.tuwien.iter.data.Test test = at.ac.tuwien.iter.data.Test
				.newInstance(
						"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx",
						"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
						"http://128.130.172.198:8081/memcached/autocles-experiment283277176241747--c8d9ec00-73cf-40dc-a10b-824f39549d13",
						"TriangleLoadGenerator", 189.55, 61.0);

		double[] transitions = collector
				.loadSeriesFromDBAndDropDB(
						"http://128.130.172.198:8081/memcached/autocles-experiment511579257018216-controllerResults",
						test);

		System.out.println("TransitionSequenceCollectorTest.collect() "
				+ Arrays.toString(transitions));
	}
}
