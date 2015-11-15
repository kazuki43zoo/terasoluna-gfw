package org.terasoluna.gfw.common.validator.constraintvalidators;


import org.terasoluna.gfw.common.validator.constraints.metas.CompositionType;
import org.terasoluna.gfw.common.validator.constraints.metas.PropertyCondition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.terasoluna.gfw.common.validator.constraintvalidators.ConstraintValidatorsUtils.getProperty;
import static org.terasoluna.gfw.common.validator.constraintvalidators.ConstraintValidatorsUtils.reportFailedToInitialize;

public class ConditionMatchers {

    private static Map<Class<? extends PropertyCondition.Matcher>, PropertyCondition.Matcher> sharedPropertyConditionMatchers = new ConcurrentHashMap<>();
    private final PropertyCondition[] propertyConditions;
    private final CompositionType compositionType;
    private final Map<String, PropertyCondition.Matcher> propertyConditionMatchers;

    public ConditionMatchers(PropertyCondition[] propertyConditions, CompositionType compositionType) {
        Map<String, PropertyCondition.Matcher> propertyConditionMatchers = new HashMap<>();
        try {
            for (PropertyCondition condition : propertyConditions) {
                String[] constructorArgs = condition.matcherConstructorArgs();
                if (constructorArgs.length == 0) {
                    if (!sharedPropertyConditionMatchers.containsKey(condition.matcher())) {
                        sharedPropertyConditionMatchers.put(condition.matcher(),
                                condition.matcher().newInstance());
                    }
                } else {
                    if (!propertyConditionMatchers.containsKey(condition.propertyName())) {
                        Class<?>[] argTypes = new Class<?>[constructorArgs.length];
                        for (int i = 0; i < argTypes.length; i++) {
                            argTypes[i] = String.class;
                        }
                        propertyConditionMatchers.put(condition.propertyName(),
                                condition.matcher().getConstructor(argTypes).newInstance((Object[]) constructorArgs));
                    }
                }
            }
        } catch (Exception e) {
            throw reportFailedToInitialize(e);
        }
        this.propertyConditions = propertyConditions;
        this.compositionType = compositionType;
        this.propertyConditionMatchers = Collections.unmodifiableMap(propertyConditionMatchers);

    }

    public boolean matches(Object bean) {
        for (PropertyCondition condition : propertyConditions) {
            Object conditionValue = getProperty(bean, condition.propertyName());
            @SuppressWarnings("unchecked")
            boolean matches = getPropertyMatcher(condition).matches(conditionValue);
            if (compositionType == CompositionType.OR && matches) return true;
            if (compositionType == CompositionType.AND && !matches) return false;
            if (compositionType == CompositionType.NONE && matches) return false;
        }
        return (compositionType != CompositionType.OR);
    }

    private PropertyCondition.Matcher getPropertyMatcher(PropertyCondition condition) {
        PropertyCondition.Matcher matcher = propertyConditionMatchers.get(condition.propertyName());
        if (matcher == null) {
            matcher = sharedPropertyConditionMatchers.get(condition.matcher());
        }
        return matcher;
    }

}
