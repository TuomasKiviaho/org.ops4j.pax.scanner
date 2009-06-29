/*
 * Copyright 2007 Alin Dreghiciu.
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
package org.ops4j.pax.scanner;

import java.util.List;

/**
 * A scanner handles a certain scheme.
 *
 * @author Alin Dreghiciu
 * @since August 17, 2007
 */
public interface Scanner
{

    /**
     * Property name for schema.
     */
    public final static String SCHEMA_PROPERTY = "provision.schema";

    /**
     * Based on the path the scanner should return a list of scanned bundles for bundles that should be installed.
     *
     * @param provisionSpec provisioning specification
     *
     * @return a list of bundle references
     *
     * @throws ScannerException - If an exception eccured during scanning
     * @throws MalformedSpecificationException
     *                          - If the path is malformed
     */
    List<ScannedBundle> scan( ProvisionSpec provisionSpec )
        throws MalformedSpecificationException, ScannerException;

}
