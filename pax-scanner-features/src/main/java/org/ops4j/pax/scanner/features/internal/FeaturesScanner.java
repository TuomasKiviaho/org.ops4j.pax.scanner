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
package org.ops4j.pax.scanner.features.internal;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.kernel.gshell.features.Feature;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.scanner.ProvisionSpec;
import org.ops4j.pax.scanner.ScannedBundle;
import org.ops4j.pax.scanner.ScannedBundleBean;
import org.ops4j.pax.scanner.Scanner;
import org.ops4j.pax.scanner.ScannerException;
import org.ops4j.pax.scanner.common.ScannerConfiguration;
import org.ops4j.pax.scanner.common.ScannerConfigurationImpl;
import org.ops4j.pax.scanner.features.ServiceConstants;
import org.ops4j.util.property.PropertyResolver;

/**
 * A scanner that scans Apache ServiceMix Kernel features files (xml).
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, April 01, 2007
 */
public class FeaturesScanner
    implements Scanner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( FeaturesScanner.class );
    /**
     * PropertyResolver used to resolve properties.
     */
    private PropertyResolver m_propertyResolver;

    /**
     * Creates a new file scanner.
     *
     * @param propertyResolver a propertyResolver; mandatory
     */
    public FeaturesScanner( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
    }

    /**
     * Reads the bundles from the file specified by the urlSpec.
     * {@inheritDoc}
     */
    public List<ScannedBundle> scan( final ProvisionSpec provisionSpec )
        throws ScannerException
    {
        NullArgumentException.validateNotNull( provisionSpec, "Provision spec" );

        LOGGER.debug( "Scanning [" + provisionSpec.getPath() + "]" );

        final ScannerConfiguration config = createConfiguration();

        final Integer defaultStartLevel = getDefaultStartLevel( provisionSpec, config );
        final Boolean defaultStart = getDefaultStart( provisionSpec, config );
        final Boolean defaultUpdate = getDefaultUpdate( provisionSpec, config );

        final FeaturesServiceWrapper featuresService = new FeaturesServiceWrapper();
        try
        {
            featuresService.addRepository( provisionSpec.getPathAsUrl().toURI() );
        }
        catch( Exception e )
        {
            throw new ScannerException( "Repository URL cannot be used", e );
        }
        final List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
        for( FeaturesFilter featuresFilter : FeaturesFilter.fromProvisionSpec( provisionSpec ) )
        {
            scannedBundles.addAll(
                features(
                    featuresService,
                    featuresFilter.getName(), featuresFilter.getVersion(),
                    defaultStartLevel, defaultStart, defaultUpdate
                )
            );
        }
        return scannedBundles;
    }

    /**
     * Determine the scanned bundle sbased on feature name and version
     *
     * @param featuresService feature service (wrapper)
     * @param featureName     feature name
     * @param featureVersion  feature version
     * @param startLevel      start level to be used
     * @param shouldStart     if the scanned bundle shold be started
     * @param shouldUpdate    if the scanned bundle should be updated
     */
    private List<ScannedBundle> features( final FeaturesServiceWrapper featuresService,
                                          final String featureName,
                                          final String featureVersion,
                                          final Integer startLevel,
                                          final Boolean shouldStart,
                                          final Boolean shouldUpdate )
        throws ScannerException
    {
        final List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
        final Feature feature;
        try
        {
            feature = featuresService.getFeature( featureName, featureVersion );
            if( feature == null )
            {
                throw new ScannerException(
                    "No feature named '" + featureName + "' with version '" + featureVersion + "' available"
                );
            }
            for( Feature dependency : feature.getDependencies() )
            {
                scannedBundles.addAll(
                    features(
                        featuresService,
                        dependency.getName(), dependency.getVersion(),
                        startLevel, shouldStart, shouldUpdate
                    )
                );
            }
            for( String bundleUrl : feature.getBundles() )
            {
                final ScannedBundleBean scannedBundle = new ScannedBundleBean(
                    bundleUrl, startLevel, shouldStart, shouldUpdate
                );
                scannedBundles.add( scannedBundle );
                LOGGER.debug( "Installing bundle [" + scannedBundles + "]" );
            }
            return scannedBundles;
        }
        catch( Exception e )
        {
            throw new ScannerException(
                "Cannot find a feature named '" + featureName + "' with version '" + featureVersion + "'", e
            );
        }
    }

    /**
     * Returns the default start level by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec a parser
     * @param config        a configuration
     *
     * @return default start level or null if nos set.
     */

    private Integer getDefaultStartLevel( ProvisionSpec provisionSpec, ScannerConfiguration config )
    {
        Integer startLevel = provisionSpec.getStartLevel();
        if( startLevel == null )
        {
            startLevel = config.getStartLevel();
        }
        return startLevel;
    }

    /**
     * Returns the default start by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec a parser
     * @param config        a configuration
     *
     * @return default start level or null if nos set.
     */
    private Boolean getDefaultStart( final ProvisionSpec provisionSpec, final ScannerConfiguration config )
    {
        Boolean start = provisionSpec.shouldStart();
        if( start == null )
        {
            start = config.shouldStart();
        }
        return start;
    }

    /**
     * Returns the default update by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec a parser
     * @param config        a configuration
     *
     * @return default update or null if nos set.
     */
    private Boolean getDefaultUpdate( final ProvisionSpec provisionSpec, final ScannerConfiguration config )
    {
        Boolean update = provisionSpec.shouldUpdate();
        if( update == null )
        {
            update = config.shouldUpdate();
        }
        return update;
    }

    /**
     * Sets the propertyResolver to use.
     *
     * @param propertyResolver a propertyResolver
     */
    public void setResolver( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
    }

    /**
     * Creates a new scanner configuration.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_propertyResolver, ServiceConstants.PID );
    }

}