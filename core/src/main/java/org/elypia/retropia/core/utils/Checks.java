/*
 * Copyright 2019-2020 Elypia CIC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elypia.retropia.core.utils;

import java.util.Objects;

/**
 * @author seth@elypia.org (Seth Falco)
 */
public final class Checks {

    /** Require Non-Unknown Format */
    private static final String UNKNOWN_ENUM = "`%s` can not be `%s`#`%s`.";

    private Checks() {
        // Do nothing.
    }

    public static void requireNonUnknown(Enum<?> e) {
        requireNonUnknown(e, "parameter");
    }

    public static void requireNonUnknown(Enum<?> e, String param) {
        Objects.requireNonNull(param);

        if (e == null)
            return;

        if (e.ordinal() == 0 && e.name().equalsIgnoreCase("UNKNOWN"))
            throw new IllegalArgumentException(String.format(UNKNOWN_ENUM, param, e.getDeclaringClass(), e.name()));
    }
}