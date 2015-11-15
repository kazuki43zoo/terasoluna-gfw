/*
 * Copyright (C) 2013-2015 terasoluna.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.terasoluna.gfw.common.validator.constraintvalidators;

import org.terasoluna.gfw.common.validator.constraints.ConditionalNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.terasoluna.gfw.common.validator.constraintvalidators.ConstraintValidatorsUtils.getProperty;

public class ConditionalNullValidator
        implements ConstraintValidator<ConditionalNull, Object> {

    private ConditionMatchers conditionMatchers;
    private String[] targets;
    private String message;

    @Override
    public void initialize(ConditionalNull constraint) {
        this.conditionMatchers = new ConditionMatchers(constraint.conditions(), constraint.compositionType());
        this.targets = constraint.targets();
        this.message = constraint.message();
    }

    public boolean isValid(Object bean, ConstraintValidatorContext context) {
        if (!conditionMatchers.matches(bean)) {
            return true;
        }
        boolean result = true;
        for (String target : targets) {
            Object value = getProperty(bean, target);
            if (value != null) {
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(target)
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                result = false;
            }
        }
        return result;
    }
}
