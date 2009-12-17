/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.*;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Uniform;
import org.restlet.resource.ClientResource;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.Individual;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.task.TasksQuery;

import java.util.logging.Logger;

/**
 * JAVADOC
 */
@Mixins(DummyDataService.Mixin.class)
public interface DummyDataService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      IndividualRepository individualRepository;

      @Service
      Uniform client;

      @Structure
      Application app;

      public void activate() throws Exception
      {
         if (!app.mode().equals( Application.Mode.development ))
            return;

         try
         {
            UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Create account" ) );

            Individual individual = individualRepository.individual();

            ValueBuilder<AccountSettingsValue> builder = vbf.newValueBuilder( AccountSettingsValue.class );
            builder.prototype().name().set( "Test server administrator" );
            builder.prototype().server().set( "http://localhost:8040/streamflow" );
            builder.prototype().userName().set( "administrator" );
            builder.prototype().password().set( "administrator" );

            final Account account = individual.newAccount();
            account.updateSettings( builder.newInstance() );

            final Account account2 = individual.newAccount();
            builder.prototype().name().set( "Test server someuser" );
            builder.prototype().userName().set("someuser");
            builder.prototype().password().set("someuser");
            account2.updateSettings( builder.newInstance() );


            CommandQueryClient server = account.server( client );
            ClientResource version = new ClientResource(server.getReference().clone().addSegment( "static" ).addSegment( "version.html" ));
            version.setNext( server.getClient() );

            String response = account.version( client );
            System.out.println( response );
            CommandQueryClient user = account.server( client ).getSubClient( "users" ).getSubClient( "administrator" );
            TasksQuery query = vbf.newValue( TasksQuery.class );
            System.out.println( user.getSubClient( "workspace").getSubClient( "user" ).getSubClient( "inbox" ).query("tasks", query, ListValue.class ).items().get().size() );

            uow.complete();

         } catch (Exception e)
         {
            Logger.getLogger( getClass().getName() ).warning( "Could not create dummy account" );
            e.printStackTrace();
         }
      }

      public void passivate() throws Exception
      {
      }

   }

}