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

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;

/**
 * This is a test set for <a href="https://issues.apache.org/jira/browse/MNG-4489">MNG-4489</a>.
 * 
 * @author Benjamin Bentmann
 */
public class MavenITmng4489MirroringOfExtensionRepoTest
    extends AbstractMavenIntegrationTestCase
{

    public MavenITmng4489MirroringOfExtensionRepoTest()
    {
        super( "[2.0.3,3.0-alpha-1),[3.0-alpha-6,)" );
    }

    /**
     * Test that repositories contributed by extension POMs during transitive dependency resolution are subject to
     * mirror and authentication configuration.
     */
    public void testit()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-4489" );

        Constraint constraint = new Constraint();
        constraint.setName( Constraint.__BASIC_AUTH );
        constraint.setRoles( new String[] { "user" } );
        constraint.setAuthenticate( true );

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint( constraint );
        constraintMapping.setPathSpec( "/*" );

        HashUserRealm userRealm = new HashUserRealm( "TestRealm" );
        userRealm.put( "testuser", "testtest" );
        userRealm.addUserToRole( "testuser", "user" );

        SecurityHandler securityHandler = new SecurityHandler();
        securityHandler.setUserRealm( userRealm );
        securityHandler.setConstraintMappings( new ConstraintMapping[] { constraintMapping } );

        ResourceHandler repoHandler = new ResourceHandler();
        repoHandler.setResourceBase( testDir.getAbsolutePath() );

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler( securityHandler );
        handlerList.addHandler( repoHandler );
        handlerList.addHandler( new DefaultHandler() );

        Server server = new Server( 0 );
        server.setHandler( handlerList );
        server.start();

        try
        {
            Verifier verifier = newVerifier( testDir.getAbsolutePath() );
            verifier.setAutoclean( false );
            verifier.deleteDirectory( "target" );
            verifier.deleteArtifacts( "org.apache.maven.its.mng4489" );
            Properties filterProps = verifier.newDefaultFilterProperties();
            filterProps.setProperty( "@port@", Integer.toString( server.getConnectors()[0].getLocalPort() ) );
            verifier.filterFile( "settings-template.xml", "settings.xml", "UTF-8", filterProps );
            verifier.addCliOption( "-s" );
            verifier.addCliOption( "settings.xml" );
            verifier.executeGoal( "validate" );
            verifier.verifyErrorFreeLog();
            verifier.resetStreams();

            verifier.assertArtifactPresent( "org.apache.maven.its.mng4489", "ext-dep", "0.1", "jar" );
            verifier.assertArtifactPresent( "org.apache.maven.its.mng4489", "ext-dep", "0.1", "pom" );
        }
        finally
        {
            server.stop();
        }
    }

}
