/*
 * Copyright 2009 Alin Dreghiciu.
 * Copyright 2011 Andreas Pieber
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.scanner.features;

/**
 * An enumeration of constants related to features scanner.
 *
 * @author Alin Dreghiciu, Andreas Pieber
 * @since 0.18.0, April 01, 2009
 */
public interface ServiceConstants {

    /**
     * Service PID used for configuration.
     */
    static final String PID = "org.ops4j.pax.scanner.features";
    /**
     * Scanner scheme.
     */
    static final String SCHEMA = "scan-features";
    /**
     * Features separator.
     */
    static final String FEATURE_SEPARATOR = ",";
    /**
     * Feature name / version separator.
     */
    static final String VERSION_SEPARATOR = "/";
}
