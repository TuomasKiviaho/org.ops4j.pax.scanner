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
package org.ops4j.pax.scanner.dir.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of lister that list content of a system file directory.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public class DirectoryLister
    implements Lister
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( DirectoryLister.class );

    /**
     * The root directory to be listed.
     */
    private final File m_dir;
    /**
     * File name filter.
     */
    private final Pattern m_filter;

    /**
     * Creates a new directory lister.
     *
     * @param dir    the base directory from where the files should be listed
     * @param filter filter to be used to filter entries from the directory
     */
    public DirectoryLister( final File dir, final Pattern filter )
    {
        NullArgumentException.validateNotNull( dir, "Directory" );
        NullArgumentException.validateNotNull( filter, "Filter" );
        m_dir = dir;
        m_filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    public List<URL> list()
        throws MalformedURLException
    {
        final List<URL> content = new ArrayList<URL>();
        // first we get all files
        final List<String> fileNames = listFiles( m_dir, "" );
        // then we filter them based on configured filter
        for( String fileName : fileNames )
        {
            if( m_filter == null || m_filter.matcher( fileName ).matches() )
            {
                File fileToAdd = new File( m_dir, fileName );
                if( !fileToAdd.isHidden() && !fileName.startsWith( "." ) )
                {
                    content.add( fileToAdd.toURL() );
                }
            }
        }
        return content;
    }

    /**
     * Lists recursively files form a directory
     *
     * @param dir        the directory to list
     * @param parentName name of the parent; to be used in construction the relative path of the returned files
     *
     * @return a list foi files from the dir and any subfolders
     */
    private List<String> listFiles( final File dir, final String parentName )
    {
        LOGGER.trace( "Scanning " + dir.getAbsolutePath() );
        final List<String> fileNames = new ArrayList<String>();
        File[] files = null;
        if( dir.canRead() )
        {
            files = dir.listFiles();
        }
        if( files != null )
        {
            for( File file : files )
            {
                if( file.isDirectory() )
                {
                    fileNames.addAll( listFiles( file, parentName + file.getName() + "/" ) );
                }
                else
                {
                    fileNames.add( parentName + file.getName() );
                }
            }
        }
        return fileNames;
    }

}
