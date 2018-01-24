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
package org.terasoluna.gfw.common.exception;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for resolving exception code.
 * <p>
 * Determines exception code from the passed exception object<br>
 * In case of {@code SystemException}, the value of {@link SystemException#getCode()} is considered as exception code. <br>
 * If the value of {@code SystemException#getCode()} is null, exception code is decided based on specification similar to
 * exceptions<br>
 * other than {@code SystemException}<br>
 * In case of exception other than {@code SystemException}, exception code is decided as defined in mappings in
 * {@link #exceptionMappings}<br>
 * If there is no definition in {@link #exceptionMappings}, then {@link #defaultExceptionCode} is taken as exception code.
 * </p>
 */
public class SimpleMappingExceptionCodeResolver implements
                                                ExceptionCodeResolver {

    /**
     * logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(
            SimpleMappingExceptionCodeResolver.class);

    /**
     * Mapping rules between exception code and exception class name.
     */
    private LinkedHashMap<String, String> exceptionMappings;

    /**
     * Default exception code.
     * <p>
     * Exception code used when it could not be resolved using the mapping rules.
     * </p>
     */
    private String defaultExceptionCode;

    /**
     * Whether to use nested cause exception.
     */
    private boolean useCause;

    /**
     * Set the Mapping rules between exception code and exception class name.
     * <p>
     * [Specification of setting {@link #exceptionMappings}]
     * <ul>
     * <li>Set the class name or parent class name of exception in {@code key} of {@code Map}.</li>
     * <li>Corresponding exception code must be set to {@code value}.</li>
     * <li>It is possible to specify a part of class name in {@code key}. For example, by setting {@code NotFound} in<br>
     * {@code key}, {@code java.io.FileNotFoundException} as well as {@code javax.naming.NameNotFoundException} are covered.
     * <li>If multiple {@code key} are set, it will be applicable in the order of addition to the {@code Map}</li>
     * </ul>
     * @param exceptionMappings Mapping rules between exception code and exception class name.
     */
    public void setExceptionMappings(
            LinkedHashMap<String, String> exceptionMappings) {
        this.exceptionMappings = exceptionMappings;
    }

    /**
     * Sets default exception code.
     * <p>
     * Exception code that must be applied when it could not be resolved based on rules, must be specified.<br>
     * Default is {@code null}
     * </p>
     * @param defaultExceptionCode Default exception code
     */
    public void setDefaultExceptionCode(String defaultExceptionCode) {
        this.defaultExceptionCode = defaultExceptionCode;
    }

    /**
     * Sets whether to use nested cause exception.
     * <p>
     * Default is {@code false}.
     * </p>
     * @param useCause If set {@code true}, use nested cause exception.
     * @since 5.5.0
     */
    public void setUseCause(boolean useCause) {
        this.useCause = useCause;
    }

    /**
     * Resolves exception code.
     * <p>
     * Determines the exception code corresponding to specified exception. <br>
     * Returns default exception code if exception code could not be determined based on rules. If default exception<br>
     * code is also not set, then returns {@code null}
     * </p>
     * @param ex Exception
     * @return Corresponding exception code.
     */
    @Override
    public String resolveExceptionCode(Exception ex) {

        if (ex == null) {
            logger.warn(
                    "target exception is null. return defaultExceptionCode.");
            return defaultExceptionCode;
        }

        List<Throwable> targets = new LinkedList<>();
        targets.add(ex);
        if (useCause) {
            Throwable cause = ex.getCause();
            while (cause != null) {
                targets.add(0, cause);
                cause = cause.getCause();
            }
        }

        for (Throwable target : targets) {
            if (target instanceof ExceptionCodeProvider) {
                String code = ((ExceptionCodeProvider) target).getCode();
                if (code != null) {
                    return code;
                }
            }
        }

        if (exceptionMappings == null || exceptionMappings.isEmpty()) {
            return defaultExceptionCode;
        }

        for (Throwable target : targets) {
            for (Entry<String, String> entry : exceptionMappings.entrySet()) {
                String targetExceptionName = entry.getKey();
                Class targetClass = target.getClass();
                while (targetClass != Object.class) {
                    if (targetClass.getName().contains(targetExceptionName)) {
                        return entry.getValue();
                    }
                    targetClass = targetClass.getSuperclass();
                }
            }
        }

        return defaultExceptionCode;
    }

}
