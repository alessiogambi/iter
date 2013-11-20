package at.ac.tuwien.iter.services.impl.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import at.ac.tuwien.iter.annotations.ValidLoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;

/**
 * This should be loaded automagically using the autobuild feature of the
 * TapestryConstraintValidatorFactory of the CLIParser module (That is taken
 * from
 * http://tawus.wordpress.com/2011/05/12/tapestry-magic-12-tapestry-ioc-aware
 * -jsr-303-custom-validators/)
 * 
 * @author alessiogambi
 * 
 */
public class LoadGeneratorValidator implements
		ConstraintValidator<ValidLoadGenerator, String> {

	private LoadGeneratorSource loadGeneratorSource;

	public LoadGeneratorValidator(LoadGeneratorSource loadGeneratorSource) {
		this.loadGeneratorSource = loadGeneratorSource;
	}

	public void initialize(ValidLoadGenerator arg0) {
	}

	public boolean isValid(String loadGeneratorId,
			ConstraintValidatorContext arg1) {
		try {
			return loadGeneratorSource.getLoadGenerator(loadGeneratorId) != null;
		} catch (Throwable e) {
			// This is raised if we cannot find the load generator with the
			// given id
			return false;
		}
	}
}
