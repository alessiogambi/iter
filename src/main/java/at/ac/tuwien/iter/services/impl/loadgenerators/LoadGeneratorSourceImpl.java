package at.ac.tuwien.iter.services.impl.loadgenerators;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;

public class LoadGeneratorSourceImpl implements LoadGeneratorSource {

	private Map<String, LoadGenerator> loadGenerators;
	private Logger logger;

	public LoadGeneratorSourceImpl(Logger logger,
			Map<String, LoadGenerator> configurations) {
		this.logger = logger;
		this.loadGenerators = new HashMap<String, LoadGenerator>();
		this.loadGenerators.putAll(configurations);
	}

	public LoadGenerator getLoadGenerator(String loadGeneratorId) {
		// Lookup via simple name
		if (loadGenerators.containsKey(loadGeneratorId.toLowerCase())) {
			return loadGenerators.get(loadGeneratorId.toLowerCase());
		}

		// Lookup via ServiceID
		for (LoadGenerator loadGenerator : loadGenerators.values()) {
			if (loadGenerator.getServiceID().equalsIgnoreCase(loadGeneratorId)) {
				return loadGenerator;
			}
		}
		logger.error("Cannot find LoadGenerator " + loadGeneratorId);
		throw new IllegalArgumentException(String.format(
				"LoadGenerator %s is not valid !", loadGeneratorId));
	}

}
