/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.scanner.composite.internal;

import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ops4j.pax.scanner.InstallableBundles;
import org.ops4j.pax.scanner.MalformedSpecificationException;
import org.ops4j.pax.scanner.ProvisionService;
import org.ops4j.pax.scanner.ScannedBundle;
import org.ops4j.pax.scanner.ScannerException;
import org.ops4j.pax.scanner.common.AbstractScannerActivator;
import org.ops4j.pax.scanner.composite.ServiceConstants;
import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.util.property.PropertyResolver;

/**
 * Bundle activator for composite scanner.
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, March 07, 2007
 */
public final class Activator
    extends AbstractScannerActivator<CompositeScanner>
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompositeScanner createScanner( final BundleContext bundleContext )
    {
        return new CompositeScanner(
            new BundleContextPropertyResolver( bundleContext ),
            new ProvisionService()
            {
                //TODO looking up the service each time is not good performance wise.
                public List<ScannedBundle> scan( String spec )
                    throws MalformedSpecificationException, ScannerException
                {
                    return getProvisionService().scan( spec );
                }

                public InstallableBundles wrap( List<ScannedBundle> scannedBundles )
                {
                    return getProvisionService().wrap( scannedBundles );
                }

                private ProvisionService getProvisionService()
                {
                    final ServiceReference sr = bundleContext.getServiceReference( ProvisionService.class.getName() );
                    if( sr == null )
                    {
                        throw new RuntimeException( "Provision service not available" );
                    }
                    final Object service = bundleContext.getService( sr );
                    if( service == null )
                    {
                        throw new RuntimeException( "Provision service not available" );
                    }
                    return (ProvisionService) service;
                }

            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPID()
    {
        return ServiceConstants.PID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSchema()
    {
        return ServiceConstants.SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setResolver( final PropertyResolver propertyResolver )
    {
        getScanner().setResolver( propertyResolver );
    }

}