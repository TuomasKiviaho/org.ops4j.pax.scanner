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
package org.ops4j.pax.scanner.composite.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.scanner.MalformedSpecificationException;
import org.ops4j.pax.scanner.ProvisionService;
import org.ops4j.pax.scanner.ProvisionSpec;
import org.ops4j.pax.scanner.ScannedBundle;
import org.ops4j.pax.scanner.ScannerException;
import org.ops4j.pax.scanner.common.ScannerConfiguration;
import org.ops4j.util.property.PropertyResolver;

/**
 * {@link CompositeScanner} unit tests.
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, March 07, 2007
 */
public class CompositeScannerTest
{

    @Test( expected = NullArgumentException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new CompositeScanner(
            createMock( PropertyResolver.class ),
            createMock( ProvisionService.class )
        ).scan( null );
    }

    @Test
    public void scan()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( config.getCertificateCheck() ).andReturn( false );
        List<ScannedBundle> refs = new ArrayList<ScannedBundle>();
        final ScannedBundle ref = createMock( ScannedBundle.class );
        expect( ref.getLocation() ).andReturn( null ).anyTimes();
        refs.add( ref );
        expect( provisionService.scan( "scan-bundle:file:bundle1.txt" ) ).andReturn( refs );
        expect( provisionService.scan( "scan-file:file:foo.bundles@5" ) ).andReturn( refs );
        expect( provisionService.scan( "scan-dir:file:foo@5@nostart" ) ).andReturn( refs );
        expect( provisionService.scan( "scan-pom:http:somewhere/foo/pom.xml@nostart" ) ).andReturn( refs );
        expect( provisionService.scan(
            "scan-file:" + new URL( file.toURL(), "relative" ).toExternalForm() + "@5@nostart@update"
        )
        ).andReturn( refs );

        replay( config, provisionService, ref );
        List<ScannedBundle> scannedBundles = createScanner( config, provisionService ).scan(
            new ProvisionSpec( "scan-composite:" + file.toURL().toExternalForm() + "@5@start@update" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Nuber of bundles", 5, scannedBundles.size() );
        verify( config, provisionService );
    }

    @Test( expected = ScannerException.class )
    public void scanWithInvalidFile()
        throws ScannerException, MalformedURLException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );

        expect( config.getCertificateCheck() ).andReturn( false );

        replay( config, provisionService );
        createScanner( config, provisionService ).scan(
            new ProvisionSpec( "scan-composite:file:inexistent" )
        );
        verify( config, provisionService );
    }

    @Test
    public void scanWithEmptyFile()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );
        File file = FileUtils.getFileFromClasspath( "scanner/empty.txt" );

        expect( config.getCertificateCheck() ).andReturn( false );

        replay( config, provisionService );
        List<ScannedBundle> scannedBundles = createScanner( config, provisionService ).scan(
            new ProvisionSpec( "scan-composite:" + file.toURL().toExternalForm() + "@10@update" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Nuber of bundles", 0, scannedBundles.size() );
        verify( config, provisionService );
    }

    @Test
    public void scanValidFileWithProperties()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );
        final Recorder recorder = createMock( Recorder.class );
        File file = FileUtils.getFileFromClasspath( "scanner/properties.txt" );

        expect( config.getCertificateCheck() ).andReturn( false );

        recorder.record( "prop.1=value.1" );
        recorder.record( "prop.2=value.2" );

        replay( config, recorder, provisionService );
        Properties sysPropsBackup = System.getProperties();
        try
        {
            System.setProperties(
                new Properties()
                {

                    @Override
                    public synchronized Object setProperty( String key, String value )
                    {
                        recorder.record( key + "=" + value );
                        return null;
                    }

                }
            );
            List<ScannedBundle> scannedBundles = createScanner( config, provisionService ).scan(
                new ProvisionSpec( "scan-composite:" + file.toURL().toExternalForm() )
            );
            assertNotNull( "Returned list is null", scannedBundles );
            verify( config, recorder, provisionService );
        }
        finally
        {
            System.setProperties( sysPropsBackup );
        }


    }

    private CompositeScanner createScanner( final ScannerConfiguration config,
                                            final ProvisionService provisionService )
    {
        return new CompositeScanner(
            createMock( PropertyResolver.class ),
            provisionService
        )
        {
            @Override
            ScannerConfiguration createConfiguration()
            {
                return config;
            }

        };
    }

}