package org.terasoluna.gfw.common.validator.constraints.metas;

public @interface PropertyCondition {

    String propertyName();

    Class<? extends Matcher<?>> matcher() default NotNullMatcher.class;

    String[] matcherConstructorArgs() default {};

    interface Matcher<T> {
        boolean matches(T value);
    }

    final class NotNullMatcher implements Matcher<Object> {
        public boolean matches(Object value) {
            return value != null;
        }
    }
    final class NullMatcher implements Matcher<Object> {
        public boolean matches(Object value) {
            return value == null;
        }
    }

}
