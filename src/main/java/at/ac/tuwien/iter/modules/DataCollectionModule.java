package at.ac.tuwien.iter.modules;

import java.io.File;
import java.util.List;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.slf4j.Logger;

import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.impl.datacollector.ClientsResultsCollector;
import at.ac.tuwien.iter.services.impl.datacollector.ControllerResultsCollector;
import at.ac.tuwien.iter.services.impl.datacollector.DatabaseManagerService;
import at.ac.tuwien.iter.services.impl.datacollector.ServiceResultsCollector;
import at.ac.tuwien.iter.services.impl.datacollector.TransitionSequenceCollector;
import at.ac.tuwien.iter.utils.DataCollectionSymbolConstants;

/**
 * The data collection module the tapestry-ioc managed classes that define the
 * data collection framework
 * 
 * This module is a sub-module of {@link IterModule}
 * 
 * @author alessiogambi
 * 
 */
public class DataCollectionModule {
	// TODO Improve testability by moving custom contributions inside a specific
	// module
	/*
	 * TODO List: at the moment we repeat several activities in a chain, some of
	 * them can be optimized by exploting the fact that services are invoked
	 * with a given order. For example, to store transitions and controller
	 * results we download the controller db twice, while we could have done it
	 * only once. Again we can put here the matlab stuff (inside some specific
	 * contribution) to invoke matlab and store the partial results.
	 */

	public void contributeApplicationDefaults(
			MappedConfiguration<String, String> configuration) {

		configuration.add(DataCollectionSymbolConstants.BASE_DIR, (new File(
				"datacollection-experiment")).getAbsolutePath());
	}

	@Scope(ScopeConstants.PERTHREAD)
	public static DataCollectionService build(
			List<DataCollectionService> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(DataCollectionService.class, commands);
	}

	@Contribute(DataCollectionService.class)
	public static void addDataCollectors(
			OrderedConfiguration<DataCollectionService> configuration,
			Logger logger, DatabaseManagerService databaseManagerService) {

		// We do not care about order constraints for the moment

		// USE AUTOBUILD - Note that Now the collector with all its' own logger
		// and not the DataCollectionService one
		configuration.addInstance("state-transitions",
				TransitionSequenceCollector.class);

		configuration.add("controller-results", new ControllerResultsCollector(
				logger, databaseManagerService));

		configuration.add("service-results", new ServiceResultsCollector(
				logger, databaseManagerService));

		configuration.add("clients-results", new ClientsResultsCollector(
				logger, databaseManagerService));
	}

	public DatabaseManagerService build(Logger logger,
			@Symbol(DataCollectionSymbolConstants.BASE_DIR) String _baseDir) {

		// TODO Not sure why cannot use the typeCoercion
		File baseDir = new File(_baseDir);
		if (baseDir.isFile()) {
			baseDir = baseDir.getParentFile();
		}

		return new DatabaseManagerService(logger, baseDir);
	}
}
