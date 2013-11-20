package at.ac.tuwien.iter.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import at.ac.tuwien.iter.services.impl.validators.LoadGeneratorValidator;

/**
 * Custom JSR-303 validation annotation. This annotation marks the its target
 * element to be a valid LoadGenerator, which means that the provided id/name
 * must match one of the load generators enabled by the user
 * 
 * @author alessiogambi
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Constraint(validatedBy = LoadGeneratorValidator.class)
public @interface ValidLoadGenerator {
	public abstract String message() default "The provided load generator is not valid.";

	public abstract Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
