/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.query.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.domain.user.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;

/**
 * JAVADOC
 */
@Mixins(ProxyUsers.Mixin.class)
public interface ProxyUsers
{
   // Commands

   ProxyUser createProxyUser( String name, @Password String password )
         throws IllegalArgumentException;

   interface Data
   {
      @Aggregated
      ManyAssociation<ProxyUser> proxyUsers();

      ProxyUser createdProxyUser( @Optional DomainEvent event, String id, String description, String password );
   }

   abstract class Mixin
         implements ProxyUsers, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      QueryBuilderFactory qbf;

      @This
      Organization organization;

      public ProxyUser createProxyUser( String description, String password )
            throws IllegalArgumentException
      {
         return createdProxyUser( null, idGen.generate( Identity.class ), description, password );
      }

      public ProxyUser createdProxyUser( @Optional DomainEvent event, String id, String description, String password )
      {
         EntityBuilder<ProxyUser> builder = uowf.currentUnitOfWork().newEntityBuilder( ProxyUser.class, id );
         builder.instance().organization().set( organization );
         UserAuthentication.Data userEntity = builder.instanceFor( UserAuthentication.Data.class );
         userEntity.userName().set( id );
         userEntity.hashedPassword().set( userEntity.hashPassword( password ) );

         Describable.Data desc = builder.instanceFor( Describable.Data.class );
         desc.description().set( description );
         ProxyUser proxyUser = builder.newInstance();

         proxyUsers().add( proxyUser );

         return proxyUser;
      }
   }
}