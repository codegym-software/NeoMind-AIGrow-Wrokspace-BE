package com.springboot.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    
    private String startFieldName;
    private String endFieldName;
    
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startFieldName = constraintAnnotation.startField();
        this.endFieldName = constraintAnnotation.endField();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        try {
            LocalDate startDate = getFieldValue(value, startFieldName);
            LocalDate endDate = getFieldValue(value, endFieldName);
            
            // If either date is null, skip validation (allow nullable dates)
            if (startDate == null || endDate == null) {
                return true;
            }
            
            // Validate that endDate is after or equal to startDate
            if (endDate.isBefore(startDate)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "End date (" + endDate + ") must be after or equal to start date (" + startDate + ")"
                ).addPropertyNode(endFieldName).addConstraintViolation();
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            // Log error and return true to avoid breaking the validation chain
            System.err.println("Error validating date range: " + e.getMessage());
            return true;
        }
    }
    
    private LocalDate getFieldValue(Object object, String fieldName) throws Exception {
        // Try to get field from class
        Class<?> clazz = object.getClass();
        
        // For records, try to use accessor methods
        if (clazz.isRecord()) {
            try {
                var method = clazz.getMethod(fieldName);
                Object result = method.invoke(object);
                return (LocalDate) result;
            } catch (NoSuchMethodException e) {
                // Field doesn't exist or not accessible
                return null;
            }
        }
        
        // For regular classes, use field access
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object result = field.get(object);
            return (LocalDate) result;
        } catch (NoSuchFieldException e) {
            // Field doesn't exist
            return null;
        }
    }
}
