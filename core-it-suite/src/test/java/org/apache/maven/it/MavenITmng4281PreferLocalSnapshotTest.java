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
import java.util.Properties;

/**
 * This is a test set for <a href="http://jira.codehaus.org/browse/MNG-4281">MNG-4281</a>.
 * 
 * @author Benjamin Bentmann
 */
public class MavenITmng4281PreferLocalSnapshotTest
    extends AbstractMavenIntegrationTestCase
{

    public MavenITmng4281PreferLocalSnapshotTest()
    {
        super( ALL_MAVEN_VERSIONS );
    }

    /**
     * Test that remote snapshots are not preferred over snapshots that have just been locally built.
     */
    public void testit()
        throws Exception
    {
        // NOTE: It's crucial to build the two projects in isolation to disable reactor resolution

        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-4281" );

        Verifier verifier = new Verifier( new File( testDir, "dependency" ).getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteArtifacts( "org.apache.maven.its.mng4281" );
        verifier.executeGoal( "validate" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        verifier.assertArtifactPresent( "org.apache.maven.its.mng4281", "dependency", "0.1-SNAPSHOT", "jar" );
        verifier.assertArtifactPresent( "org.apache.maven.its.mng4281", "dependency", "0.1-SNAPSHOT", "pom" );

        verifier = new Verifier( new File( testDir, "project" ).getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.getCliOptions().add( "-s" );
        verifier.getCliOptions().add( "settings.xml" );
        verifier.filterFile( "settings-template.xml", "settings.xml", "UTF-8", verifier.newDefaultFilterProperties() );
        verifier.executeGoal( "validate" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        Properties checksums = verifier.loadProperties( "target/checksum.properties" );
        assertChecksum( "7c564b3fbeda6db61b62c35e58a8ef672e712400", checksums );
    }

    private void assertChecksum( String checksum, Properties checksums )
    {
        String actual = checksums.getProperty( "dependency-0.1-SNAPSHOT.jar" );
        assertEquals( checksum, actual.toLowerCase( java.util.Locale.ENGLISH ) );
    }

}