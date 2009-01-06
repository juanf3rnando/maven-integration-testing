package org.apache.maven.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * This is a test set for <a href="http://jira.codehaus.org/browse/MNG-818">MNG-818</a>.
 * 
 * @author Brett Porter
 * @version $Id$
 */
public class MavenITmng0818WarDepsNotTransitiveTest
    extends AbstractMavenIntegrationTestCase
{

    /**
     * Test that depending on a WAR doesn't also get its dependencies transitively.
     */
    public void testitMNG0818()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-0818" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "target" );
        verifier.deleteArtifacts( "org.apache.maven.its.it0080" );
        verifier.executeGoal( "validate" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        Collection artifacts = verifier.loadLines( "target/artifacts.txt", "UTF-8" );
        assertEquals( Collections.singletonList( "org.apache.maven.its.it0080:war:war:0.1" ), artifacts );
    }

}