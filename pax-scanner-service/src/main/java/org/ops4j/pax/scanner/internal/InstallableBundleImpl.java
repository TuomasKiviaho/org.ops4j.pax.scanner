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
package org.ops4j.pax.scanner.internal;

import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.scanner.InstallableBundle;
import org.ops4j.pax.scanner.ScannedBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.startlevel.StartLevel;

public class InstallableBundleImpl
    implements InstallableBundle
{

    /**
     * Holds the scanned bundle to be installed.
     */
    private final ScannedBundle m_scannedBundle;
    /**
     * The installed bundle. Null before installation.
     */
    private Bundle m_bundle;
    /**
     * Bundle context where the bundle is installed.
     */
    private final BundleContext m_bundleContext;
    /**
     * The start level service or null if not available.
     */
    private final StartLevel m_startLevelService;
    /**
     * The internal state.
     */
    private State m_state;

    /**
     * Creates a new Installable Bundle with no start level service.
     *
     * @param bundleContext a bundle context; mandatory
     * @param scannedBundle scanned bundle; mandatory
     */
    public InstallableBundleImpl( final BundleContext bundleContext,
                                  final ScannedBundle scannedBundle )
    {
        this( bundleContext, scannedBundle, null );
    }

    /**
     * Creates a new Installable Bundle with a start level service that can be null.
     *
     * @param bundleContext     a bundle context; mandatory
     * @param scannedBundle     scanned bundle; mandatory
     * @param startLevelService a start level service; optional
     */
    public InstallableBundleImpl( final BundleContext bundleContext,
                                  final ScannedBundle scannedBundle,
                                  final StartLevel startLevelService )
    {
        NullArgumentException.validateNotNull( bundleContext, "Bundle context" );
        NullArgumentException.validateNotNull( scannedBundle, "Scanned bundle" );
        m_bundleContext = bundleContext;
        m_scannedBundle = scannedBundle;
        m_startLevelService = startLevelService;
        m_state = new NotInstalledState();
    }

    /**
     * @see InstallableBundle#getBundle()
     */
    public Bundle getBundle()
    {
        return m_bundle;
    }

    /**
     * @see InstallableBundle#install()
     */
    public InstallableBundle install()
        throws BundleException
    {
        m_state.install();
        return this;
    }

    /**
     * @see org.ops4j.pax.scanner.InstallableBundle#startIfNecessary()
     */
    public InstallableBundle startIfNecessary()
        throws BundleException
    {
        if( m_scannedBundle.shouldStart() )
        {
            start();
        }
        return this;
    }

    /**
     * @see InstallableBundle#start()
     */
    public InstallableBundle start()
        throws BundleException
    {
        m_state.start();
        return this;
    }

    /**
     * Performs the actual installation.
     *
     * @throws org.osgi.framework.BundleException
     *          see install()
     */
    private void doInstall()
        throws BundleException
    {
        final String location = m_scannedBundle.getLocation();
        if( location == null )
        {
            throw new BundleException( "The scanned bundle has no location" );
        }
        // get current time to be ubale to verify if the bundle was already installed before the install below
        long currentTime = System.currentTimeMillis();
        m_bundle = m_bundleContext.installBundle( location );
        // if the bundle was modified (installed/updated) before then force an update
        Boolean shouldUpdate = m_scannedBundle.shouldUpdate();
        if( shouldUpdate != null && shouldUpdate && m_bundle.getLastModified() < currentTime )
        {
            m_bundle.update();
        }
        if( m_bundle == null )
        {
            throw new BundleException( "The bundle could not be installed due to unknown reason" );
        }
        m_state = new InstalledState();
        if( m_startLevelService != null )
        {
            Integer startLevel = m_scannedBundle.getStartLevel();
            if( startLevel != null )
            {
                m_startLevelService.setBundleStartLevel( m_bundle, startLevel );
            }
        }
        startIfNecessary();
    }

    /**
     * Performs the actual start.
     *
     * @throws BundleException see start()
     */
    private void doStart()
        throws BundleException
    {
        if( m_bundle != null )
        {
            m_bundle.start();
        }
        m_state = new StartedState();
    }

    /**
     * Internal state of the installable bundle.
     */
    private class State
    {

        /**
         * Does nothing.
         *
         * @throws org.osgi.framework.BundleException
         *          Can not happen.
         */
        void install()
            throws BundleException
        {
            // installation in the actual state
        }

        /**
         * Does nothing.
         *
         * @throws org.osgi.framework.BundleException
         *          Can not happen.
         */
        void start()
            throws BundleException
        {
            // start in the actual state
        }

    }

    /**
     * When the bundle was not installed = initial state.
     */
    private class NotInstalledState
        extends State
    {

        /**
         * Installs the bundle.
         */
        void install()
            throws BundleException
        {
            doInstall();
        }

        /**
         * Starts the bundle.
         */
        void start()
            throws BundleException
        {
            install();
            doStart();
        }

    }

    /**
     * When the bundle was installed. Only start should do something.
     */
    private class InstalledState
        extends State
    {

        /**
         * Starts the bundle.
         */
        void start()
            throws BundleException
        {
            doStart();
        }

    }

    /**
     * When the bundle was installed and started. There is nothing more to do.
     */
    private class StartedState
        extends State
    {

    }

}
