package org.terasoluna.gfw.common.validator.constraints;

import lombok.Data;
import org.joda.time.YearMonth;
import org.junit.Before;
import org.junit.Test;
import org.terasoluna.gfw.common.validator.constraints.metas.PropertyCondition;

import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConditionalNotNullAndNullTest extends AbstractConstraintsTest<ConditionalNotNullAndNullTest.AccountUpdateForm> {

    @Before
    public void before() {
        form = new AccountUpdateForm();
    }

    @Test
    public void testNoInputType() throws Throwable {
        form.setName("Shimizu");
        form.setPassword("password");
        form.setConfirmPassword("password");
        violations = validator.validate(form);
        assertThat(violations.size(), is(1));
        for (ConstraintViolation<AccountUpdateForm> violation : violations) {
            assertThat(violation.getMessage(), is("may not be null"));
        }
    }


    @Test
    public void testInputAllWithNormalAccount() throws Throwable {
        form.setName("Shimizu");
        form.setType(AccountType.NORMAL);
        form.setPassword("password");
        form.setConfirmPassword("password");
        violations = validator.validate(form);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testInputCardInformationWithNormalAccount() throws Throwable {
        form.setName("Shimizu");
        form.setType(AccountType.NORMAL);
        form.setCardNumber("1234567890"); // input with normal account
        form.setCardValidYearMonth(YearMonth.parse("2017-06")); // input with normal account
        form.setPassword("password");
        form.setConfirmPassword("password");
        violations = validator.validate(form);
        assertThat(violations.size(), is(2));
        for (ConstraintViolation<AccountUpdateForm> violation : violations) {
            assertThat(violation.getMessage(), is("must be null"));
        }
    }


    @Test
    public void testInputAllWithPremiumAccount() throws Throwable {
        form.setName("Shimizu");
        form.setType(AccountType.PREMIUM);
        form.setCardNumber("1234567890");
        form.setCardValidYearMonth(YearMonth.parse("2017-06"));
        form.setPassword("password");
        form.setConfirmPassword("password");
        violations = validator.validate(form);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testNoInputCardInformationWithPremiumAccount() throws Throwable {
        form.setName("Shimizu");
        form.setType(AccountType.PREMIUM);
        form.setCardNumber(null); // no input with premium account
        form.setCardValidYearMonth(null); // no input with premium account
        form.setPassword("password");
        form.setConfirmPassword("password");
        violations = validator.validate(form);
        assertThat(violations.size(), is(2));
        for (ConstraintViolation<AccountUpdateForm> violation : violations) {
            assertThat(violation.getMessage(), is("may not be null"));
        }
    }

    @Test
    public void testNoInputConfirmPassword() throws Throwable {
        form.setName("Shimizu");
        form.setType(AccountType.NORMAL);
        form.setPassword("password");
        form.setConfirmPassword(null); // no input confirm password
        violations = validator.validate(form);
        assertThat(violations.size(), is(1));
        for (ConstraintViolation<AccountUpdateForm> violation : violations) {
            assertThat(violation.getMessage(), is("may not be null"));
        }
    }

    @Test
    public void testInputConfirmPasswordOnly() throws Throwable {
        form.setName("Shimizu");
        form.setType(AccountType.NORMAL);
        form.setPassword(null);
        form.setConfirmPassword("password"); // input confirm password
        violations = validator.validate(form);
        assertThat(violations.size(), is(1));
        for (ConstraintViolation<AccountUpdateForm> violation : violations) {
            assertThat(violation.getMessage(), is("must be null"));
        }
    }

    @Test
    public void testNoInputPassword() throws Throwable {
        form.setName("Shimizu");
        form.setType(AccountType.NORMAL);
        form.setPassword(null); // no input password
        form.setConfirmPassword(null);
        violations = validator.validate(form);
        assertThat(violations.size(), is(0));
    }

    @Data
    @ConditionalNotNull.List({
            @ConditionalNotNull(conditions = @PropertyCondition(propertyName = "type", matcher = PremiumAccountMatcher.class), targets = {"cardNumber", "cardValidYearMonth"}),
            @ConditionalNotNull(conditions = {@PropertyCondition(propertyName = "password")}, targets = "confirmPassword")
    })
    @ConditionalNull.List({
            @ConditionalNull(conditions = @PropertyCondition(propertyName = "type", matcher = NormalAccountMatcher.class), targets = {"cardNumber", "cardValidYearMonth"}),
            @ConditionalNull(conditions = @PropertyCondition(propertyName = "password", matcher = PropertyCondition.NullMatcher.class), targets = "confirmPassword")
    })
    public static class AccountUpdateForm {

        @NotNull
        private String name;

        @NotNull
        private AccountType type;

        // @ConditionalNotNull (If type is PREMIUM, may not be null)
        // @ConditionalNotNull (If type is NORMAL, must be null)
        private String cardNumber;
        private YearMonth cardValidYearMonth;

        @Size(min = 8)
        private String password;

        // @ConditionalNotNull (If password is input, may not be null)
        // @ConditionalNotNull (If password is no input, must be null)
        private String confirmPassword;

    }

    public static class PremiumAccountMatcher implements PropertyCondition.Matcher<AccountType> {
        public boolean matches(AccountType value) {
            return AccountType.PREMIUM == value;
        }
    }

    public static class NormalAccountMatcher implements PropertyCondition.Matcher<AccountType> {
        public boolean matches(AccountType value) {
            return AccountType.NORMAL == value;
        }
    }

    enum AccountType {
        NORMAL, PREMIUM
    }

}
