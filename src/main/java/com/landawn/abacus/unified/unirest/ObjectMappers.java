/*
 * Copyright (c) 2021, Haiyang Li.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.landawn.abacus.unified.unirest;

import com.landawn.abacus.util.N;

import kong.unirest.ObjectMapper;

public final class ObjectMappers {
    private ObjectMappers() {
        // singleton.
    }

    public static final ObjectMapper JSON = new ObjectMapper() {
        @Override
        public <T> T readValue(String value, Class<T> valueType) {
            return N.fromJSON(valueType, value);
        }

        @Override
        public String writeValue(Object value) {
            return N.toJSON(value);
        }
    };

    public static final ObjectMapper XML = new ObjectMapper() {
        @Override
        public <T> T readValue(String value, Class<T> valueType) {
            return N.fromXML(valueType, value);
        }

        @Override
        public String writeValue(Object value) {
            return N.toXML(value);
        }
    };
}
