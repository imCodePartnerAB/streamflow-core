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

package se.streamsource.streamflow.web.resource.surface.endusers;

import org.qi4j.api.unitofwork.*;
import org.restlet.data.*;
import org.restlet.resource.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.streamflow.web.application.security.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

/**
 * JAVADOC
 */
public class EndUsersResource
      extends CommandQueryResource
      implements SubResources
{
   public EndUsersResource()
   {
      super( EndUsersContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      ProxyUser proxyUser = RoleMap.role(ProxyUser.class);
      try
      {
         RoleMap.current().set(proxyUser.getEndUser(segment));
      } catch (NoSuchEntityException e)
      {
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }
      RoleMap.current().set( new UserPrincipal( segment ) );

      subResource( EndUserResource.class );
   }
}