package at.ac.tuwien.iter;

import javax.validation.ValidationException;

import org.apache.commons.cli.ParseException;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.gambi.tapestry5.cli.services.CLIParser;

import at.ac.tuwien.iter.modules.Assertions;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.Iter;

/**
 * Main class to start the application.
 * 
 * @author alessiogambi
 * 
 */
public class Main {

	/*
	 * Tapestry Registry for DI-IoC
	 */
	private static Registry registry;

	public static void main(String[] args) throws ValidationException,
			ParseException {

		/*
		 * Setup the inversion of control
		 */
		RegistryBuilder builder = new RegistryBuilder();
		// Load all the modules in the class path
		IOCUtilities.addDefaultModules(builder);
		// Add the locally defined modules (this will automatically load also
		// the declared sub modules)
		builder.add(IterModule.class);
		builder.add(Assertions.class);

		registry = builder.build();
		registry.performRegistryStartup();

		/*
		 * Parse the command line and start the tool
		 */
		try {
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);

			// This thing should be managed by a contribution to CLIParser
			// inside a
			// command
			// something like java -jar DRIVER iter -c cddsa -a --boo 2 ...
			Iter iter = registry.getService(Iter.class);

			iter.start();
		} catch (ParseException e) {
			// Ignore this
			e.printStackTrace();
		} catch (ValidationException e) {
			// Ignore this
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}