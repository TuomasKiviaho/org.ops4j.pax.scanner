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
package org.ops4j.pax.scanner.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.scanner.Scanner;
import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.util.property.DictionaryPropertyResolver;
import org.ops4j.util.property.PropertyResolver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Abstract bundle activator for scanners.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public abstract class AbstractScannerActivator<T extends Scanner>
    implements BundleActivator
{

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog( AbstractScannerActivator.class );
    /**
     * The bundle context.
     */
    private BundleContext m_bundleContext;
    /**
     * Registred scanner.
     */
    private T m_scanner;
    /**
     * Scanner service registration. Usef for cleanup.
     */
    private ServiceRegistration m_scannerReg;
    /**
     * Managed service registration. Used for cleanup.
     */
    private ServiceRegistration m_managedServiceReg;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
        throws Exception
    {
        NullArgumentException.validateNotNull( bundleContext, "Bundle context" );
        m_bundleContext = bundleContext;
        registerScanner();
        registerManagedService();
        LOG.debug( "Scanner for schema [" + getSchema() + "] started" );
    }

    /**
     * Performs cleanup:<br/>
     * * Unregister scanner;<br/>
     * * Unregister managed service;<br/>
     * * Release bundle context.
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( final BundleContext bundleContext )
    {
        NullArgumentException.validateNotNull( bundleContext, "Bundle context" );
        if( m_scannerReg != null )
        {
            m_scannerReg.unregister();
        }
        if( m_managedServiceReg != null )
        {
            m_managedServiceReg.unregister();
        }
        m_bundleContext = null;
        LOG.debug( "Scanner for schema [" + getSchema() + "] stopped" );
    }

    /**
     * Registers the scanner. Will be used by provisioning service via white box pattern.
     */
    private void registerScanner()
    {
        m_scanner = createScanner( m_bundleContext );
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put( Scanner.SCHEMA_PROPERTY, getSchema() );
        m_scannerReg = m_bundleContext.registerService( Scanner.class.getName(), m_scanner, props );
    }

    /**
     * Registers a managed service to listen on configuration updates.
     */
    private void registerManagedService()
    {
        final Dictionary<String, String> props = new Hashtable<String, String>();
        props.put( Constants.SERVICE_PID, getPID() );
        m_managedServiceReg = m_bundleContext.registerService(
            ManagedService.class.getName(),
            new ManagedService()
            {
                /**
                 * Sets the resolver on scanner.
                 *
                 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
                 */
                public void updated( final Dictionary config )
                    throws ConfigurationException
                {
                    if( config == null )
                    {
                        setResolver( new BundleContextPropertyResolver( m_bundleContext ) );
                    }
                    else
                    {
                        setResolver(
                            new DictionaryPropertyResolver(
                                config,
                                new BundleContextPropertyResolver( m_bundleContext )
                            )
                        );
                    }
                }
            },
            props
        );
    }

    /**
     * Returns the scanner.
     *
     * @return a scanner
     */
    protected T getScanner()
    {
        return m_scanner;
    }

    /**
     * Scanner factory method.
     *
     * @param bundleContext a bundle scanner
     *
     * @return the created file scanner.
     */
    protected abstract T createScanner( BundleContext bundleContext );

    /**
     * Returns the persistence id (PID) for the scanner.
     *
     * @return a PID
     */
    protected abstract String getPID();

    /**
     * Returns the scanner schema.
     *
     * @return scanner schema
     */
    protected abstract String getSchema();

    /**
     * Sets the propertyResolver to use.
     *
     * @param propertyResolver a resoler
     */
    protected abstract void setResolver( PropertyResolver propertyResolver );
}

