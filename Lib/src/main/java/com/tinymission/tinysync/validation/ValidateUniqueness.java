package com.tinymission.tinysync.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying a uniqueness validator to a field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@FieldValidation(UniquenessValidator.class)
public @interface ValidateUniqueness {
    String message() default "is already taken";
}
