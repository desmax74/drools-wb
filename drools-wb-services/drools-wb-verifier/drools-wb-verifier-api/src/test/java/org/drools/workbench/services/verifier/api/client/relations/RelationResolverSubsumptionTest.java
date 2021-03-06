/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.workbench.services.verifier.api.client.relations;

import org.drools.workbench.services.verifier.api.client.AnalyzerConfigurationMock;
import org.drools.workbench.services.verifier.api.client.configuration.AnalyzerConfiguration;
import org.drools.workbench.services.verifier.api.client.index.keys.Key;
import org.drools.workbench.services.verifier.api.client.index.keys.UUIDKey;
import org.drools.workbench.services.verifier.api.client.maps.InspectorList;
import org.drools.workbench.services.verifier.api.client.maps.util.HasKeys;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class RelationResolverSubsumptionTest {

    private AnalyzerConfiguration configuration;

    private RelationResolver relationResolver;
    private InspectorList a;
    private InspectorList b;
    private Person firstItemInB;
    private Person blockingItem;

    @Before
    public void setUp() throws
                        Exception {
        configuration = new AnalyzerConfigurationMock();

        a = new InspectorList( configuration );
        b = new InspectorList( configuration );

        a.add( new Person( 15 ) );

        firstItemInB = spy( new Person( 15 ) );
        b.add( firstItemInB );
        blockingItem = spy( new Person( 10 ) );
        b.add( blockingItem );

        relationResolver = new RelationResolver( a,
                                                 true );
    }

    @Test
    public void empty() throws
                        Exception {
        relationResolver = new RelationResolver( new InspectorList( configuration ) );
        assertTrue( relationResolver.subsumes( new InspectorList( configuration ) ) );
    }

    @Test
    public void emptyListWithItemsSubsumesEmptyLists() throws
                                                       Exception {
        assertTrue( relationResolver.subsumes( new InspectorList( configuration ) ) );
    }

    @Test
    public void recheck() throws
                          Exception {

        assertFalse( relationResolver.subsumes( b ) );

        verify( firstItemInB ).subsumes( any() );

        reset( firstItemInB );

        assertFalse( relationResolver.subsumes( b ) );

        verify( firstItemInB,
                never() ).subsumes( any() );
    }

    @Test
    public void recheckWithUpdate() throws
                                    Exception {

        assertFalse( relationResolver.subsumes( b ) );

        reset( firstItemInB );

        // UPDATE
        blockingItem.setAge( 15 );

        assertTrue( relationResolver.subsumes( b ) );

        verify( firstItemInB ).subsumes( any() );
    }

    @Test
    public void recheckConflictingItemRemoved() throws
                                                Exception {

        assertFalse( relationResolver.subsumes( b ) );

        reset( firstItemInB );

        // UPDATE
        b.remove( blockingItem );

        assertTrue( relationResolver.subsumes( b ) );

        verify( firstItemInB ).subsumes( any() );
    }

    @Test
    public void recheckOtherListBecomesEmpty() throws
                                               Exception {

        assertFalse( relationResolver.subsumes( b ) );

        reset( firstItemInB,
               blockingItem );

        // UPDATE
        b.clear();

        assertTrue( relationResolver.subsumes( b ) );

        verify( firstItemInB,
                never() ).subsumes( any() );
        verify( blockingItem,
                never() ).subsumes( any() );
    }

    public class Person
            implements IsSubsuming,
                       HasKeys {

        int age;

        private UUIDKey uuidKey = configuration.getUUID( this );

        public Person( final int age ) {
            this.age = age;
        }

        @Override
        public UUIDKey getUuidKey() {
            return uuidKey;
        }

        @Override
        public Key[] keys() {
            return new Key[]{
                    uuidKey
            };
        }

        public void setAge( final int age ) {
            this.age = age;
        }

        @Override
        public boolean subsumes( final Object other ) {
            if ( other instanceof Person ) {
                return age == ( (Person) other ).age;
            } else {
                return false;
            }
        }
    }
}