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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.ops4j.pax.scanner.InstallableBundle;

public class InstallableBundlesImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullList()
    {
        new InstallableBundlesImpl( null );
    }

    @Test
    public void constructorWithEmptyList()
    {
        new InstallableBundlesImpl( new ArrayList<InstallableBundle>() );
    }

    @Test
    public void iterator()
    {
        InstallableBundle installable = createMock( InstallableBundle.class );
        replay( installable );
        List<InstallableBundle> list = new ArrayList<InstallableBundle>();
        list.add( installable );
        Iterator it = new InstallableBundlesImpl( list ).iterator();
        assertNotNull( "Returned iterator is null", it );
        assertTrue( "Iterator should have at least one installable bunlde", it.hasNext() );
        assertEquals( "Installable bundle", installable, it.next() );
        verify( installable );
    }

    @Test
    public void install()
        throws BundleException
    {
        InstallableBundle installable = createMock( InstallableBundle.class );
        expect( installable.install() ).andReturn( installable );
        expect( installable.startIfNecessary() ).andReturn( installable );
        replay( installable );
        List<InstallableBundle> list = new ArrayList<InstallableBundle>();
        list.add( installable );
        new InstallableBundlesImpl( list ).install();
        verify( installable );
    }

}
