package com.ebp.openQuarterMaster.lib.core.validation.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.List;

public abstract class Validator<A extends Annotation, T> implements ConstraintValidator<A, T> {
	
	protected boolean processValidationResults(List<String> validationErrors, ConstraintValidatorContext context) {
		if (validationErrors == null || validationErrors.isEmpty()) {
			return true;
		}
		if (context != null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
				String.join("; ", validationErrors)
			).addConstraintViolation();
		}
		return false;
	}
}
