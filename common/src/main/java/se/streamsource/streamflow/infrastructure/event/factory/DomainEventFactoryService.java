/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.infrastructure.event.factory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.time.Time;

import javax.security.auth.Subject;
import java.security.AccessController;
import java.security.Principal;
import java.util.Iterator;

/**
 * DomainEvent factory
 */
@Concerns(TransactionNotificationConcern.class)
@Mixins(DomainEventFactoryService.DomainEventFactoryMixin.class)
public interface DomainEventFactoryService
      extends DomainEventFactory, ServiceComposite
{
   class DomainEventFactoryMixin
         implements DomainEventFactory
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      IdentityGenerator idGenerator;

      @Service
      Time time;

      String version;

      public void init( @Structure Application application )
      {
         version = application.version();
      }

      public DomainEvent createEvent( EntityComposite entity, String name, Object[] args )
      {
         ValueBuilder<DomainEvent> builder = vbf.newValueBuilder( DomainEvent.class );

         DomainEvent prototype = builder.prototype();
         prototype.name().set( name );
         prototype.entityType().set( entity.type().getName() );
         prototype.on().set( time.dateNow() );
         prototype.entity().set( entity.identity().get() );

         // Take user either from Subject or UoW
         UnitOfWork uow = uowf.currentUnitOfWork();
         Subject subject = Subject.getSubject( AccessController.getContext() );
         if (subject == null)
         {
            Principal principal = uow.metaInfo().get( Principal.class );
            if (principal != null)
               prototype.by().set( principal.getName() );
            else
               prototype.by().set( "unknown" ); // No user set

         }
         else
         {
            Iterator<Principal> iterator = subject.getPrincipals().iterator();
            if (iterator.hasNext())
            {
               String userName = iterator.next().getName();
               prototype.by().set( userName );
            } else
               prototype.by().set( "unknown" );
         }

         prototype.identity().set( idGenerator.generate( DomainEvent.class ) );
         prototype.usecase().set( uow.usecase().name() );
         prototype.version().set( version );

         // JSON-ify parameters
         JSONStringer json = new JSONStringer();
         try
         {
            JSONWriter params = json.object();
            for (int i = 1; i < args.length; i++)
            {
               params.key( "param" + i );
               if (args == null)
                  params.value( JSONObject.NULL );
               else
                  params.value( args[i] );
            }
            json.endObject();
         } catch (JSONException e)
         {
            throw new IllegalArgumentException( "Could not create event", e );
         }

         prototype.parameters().set( json.toString() );

         DomainEvent event = builder.newInstance();

         return event;
      }
   }
}