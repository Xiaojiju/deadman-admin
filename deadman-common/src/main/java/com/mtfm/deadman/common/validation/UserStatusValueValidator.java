package com.mtfm.deadman.common.validation;

import com.mtfm.deadman.common.enums.UserStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link UserStatusValue} 校验器。
 */
public class UserStatusValueValidator implements ConstraintValidator<UserStatusValue, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value == UserStatus.ACTIVE.getValue() || value == UserStatus.DISABLED.getValue();
    }
}
