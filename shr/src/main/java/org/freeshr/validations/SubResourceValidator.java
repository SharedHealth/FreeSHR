package org.freeshr.validations;


import java.util.List;

public interface SubResourceValidator {
    boolean validates(Object resource);
    List<ShrValidationMessage> validate(Object resource, int entryIndex);
}
