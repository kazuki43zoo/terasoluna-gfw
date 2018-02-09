/*
 * Copyright (C) 2013-2017 NTT DATA Corporation
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
package org.terasoluna.gfw.common.codelist.i18n;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

public class AbstractI18nCodeListTest {

    @Before
    @After
    public void resetLocaleContext() {
        LocaleContextHolder.resetLocaleContext();
        LocaleContextHolder.setDefaultLocale(null);
    }

    @Test
    public void testAsMapSystemDefaultLocale() {
        AbstractI18nCodeList impl = new AbstractI18nCodeList() {

            @Override
            public Map<String, String> asMap(Locale locale) {
                return locale == Locale.getDefault() ? Collections
                        .<String, String> emptyMap() : null;
            }

        };

        // Call super class asMap method
        Map<String, String> map = impl.asMap();
        assertNotNull(map);
    }

    @Test
    public void testAsMapUserDefinedDefaultLocale() {
        LocaleContextHolder.setDefaultLocale(Locale.FRANCE);
        AbstractI18nCodeList impl = new AbstractI18nCodeList() {

            @Override
            public Map<String, String> asMap(Locale locale) {
                return locale == Locale.FRANCE ? Collections
                        .<String, String> emptyMap() : null;
            }

        };

        // Call super class asMap method
        Map<String, String> map = impl.asMap();
        assertNotNull(map);
    }

    @Test
    public void testAsMapRequestLocale() {
        LocaleContextHolder.setLocale(Locale.GERMANY);
        AbstractI18nCodeList impl = new AbstractI18nCodeList() {

            @Override
            public Map<String, String> asMap(Locale locale) {
                return locale == Locale.GERMANY ? Collections
                        .<String, String> emptyMap() : null;
            }

        };

        // Call super class asMap method
        Map<String, String> map = impl.asMap();
        assertNotNull(map);
    }

}
