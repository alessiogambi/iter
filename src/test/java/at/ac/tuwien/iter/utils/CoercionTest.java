package at.ac.tuwien.iter.utils;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.TypeCoercer;

import at.ac.tuwien.iter.modules.IterModule;

public class CoercionTest {
	public static void main(String[] args) {
		RegistryBuilder builder = new RegistryBuilder();
		IOCUtilities.addDefaultModules(builder);
		builder.add(IterModule.class);
		Registry registry = builder.build();

		TypeCoercer typeCoercer = registry.getService(TypeCoercer.class);
		double[] convertToNumberArray = new double[] { 1.0, 2.0, 3.0 };
		Number[] numbers = typeCoercer.coerce(convertToNumberArray,
				Number[].class);

		double[] doubles = typeCoercer.coerce(numbers, double[].class);
	}
}
