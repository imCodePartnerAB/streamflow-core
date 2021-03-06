/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.domain.individual;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.ClientCache;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.restlet.client.CommandQueryClientFactory;
import se.streamsource.dci.restlet.client.ResponseHandler;

import java.io.IOException;

/**
 * Entity representing a client-side account
 */
@Mixins({AccountEntity.Mixin.class})
public interface AccountEntity
      extends Account, EntityComposite
{
   interface Data
   {
      // Settings
      Property<AccountSettingsValue> settings();
   }

   class Mixin
         implements AccountSettings, AccountConnection
   {
      @Structure
      Module module;

      @This
      Account account;

      @This
      Data state;

      @Service
      IndividualRepository repo;

      @Service
      ResponseHandler handler;

      // AccountSettings
      public AccountSettingsValue accountSettings()
      {
         return state.settings().get();
      }

      public void updateSettings( AccountSettingsValue newAccountSettings )
      {
         state.settings().set( newAccountSettings );
      }

      public void changePassword( Uniform client, String oldPassword, String newPassword ) throws ResourceException
      {
         Form form = new Form();
         form.set("oldpassword", oldPassword);
         form.set("newpassword", newPassword);
         server(client).getSubClient( "account" ).postCommand( "changepassword", form );

         AccountSettingsValue settings = state.settings().get().<AccountSettingsValue>buildWith().prototype();
         settings.password().set( newPassword );

         updateSettings( settings );
      }

      // AccountConnection
      public CommandQueryClient server( Uniform client )
      {
         AccountSettingsValue settings = accountSettings();
         Reference serverRef = new Reference( settings.server().get() );
         serverRef.setPath( "/streamflow/" );

         AuthenticationFilter filter = new AuthenticationFilter( module.unitOfWorkFactory(), account, client );

         return module.objectBuilderFactory().newObjectBuilder( CommandQueryClientFactory.class ).use( filter, handler, new ClientCache() ).newInstance().newClient( serverRef );
      }

      public CommandQueryClient user( Uniform client )
      {
         return server( client ).getSubClient( "users" ).getSubClient( accountSettings().userName().get() );
      }

      public String version(Uniform client) throws ResourceException, IOException
      {
         CommandQueryClient server = server( client );
         Representation in = server.getClient( "/streamflow/static/" ).query( "version.html", Representation.class );

         String version = in.getText();
         return version;
      }
   }

   class AuthenticationFilter
      implements Uniform
   {
      private UnitOfWorkFactory uowf;
      private AccountSettings account;
      private final Uniform next;

      public AuthenticationFilter( UnitOfWorkFactory uowf, AccountSettings account, Uniform next )
      {
         this.uowf = uowf;
         this.account = account;
         this.next = next;
      }

      public void handle( Request request, Response response )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();
         AccountSettingsValue settings;
         if (uow == null)
         {
            uow = uowf.newUnitOfWork();
            settings = uow.get( account ).accountSettings();
            uow.discard();
         } else
         {
            settings = uow.get( account ).accountSettings();
         }

         request.setChallengeResponse( new ChallengeResponse( ChallengeScheme.HTTP_BASIC, settings.userName().get(), settings.password().get() ) );

         next.handle( request, response );
      }
   }
}
