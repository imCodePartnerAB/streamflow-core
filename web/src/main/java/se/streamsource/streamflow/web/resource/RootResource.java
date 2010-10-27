/*
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

package se.streamsource.streamflow.web.resource;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.SubResource;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.resource.account.AccountResource;
import se.streamsource.streamflow.web.resource.administration.AdministrationResource;
import se.streamsource.streamflow.web.resource.overview.OverviewResource;
import se.streamsource.streamflow.web.resource.surface.SurfaceResource;
import se.streamsource.streamflow.web.resource.workspace.WorkspaceResource;

/**
 * JAVADOC
 */
public class RootResource
      extends CommandQueryResource
{
   @SubResource
   public void account()
   {
      subResource( AccountResource.class );
   }

   @SubResource
   public void workspace()
   {
      subResource( WorkspaceResource.class );
   }

   @SubResource
   public void overview()
   {
      subResource( OverviewResource.class );
   }

   @SubResource
   public void administration()
   {
      setRole(UsersEntity.class, UsersEntity.USERS_ID);
      subResource( AdministrationResource.class );
   }

   @SubResource
   public void surface()
   {
      setRole( UsersEntity.class, UsersEntity.USERS_ID );
      subResource( SurfaceResource.class );
   }

/*
@SubResource
public void users()
{
   setRole( UsersEntity.class, UsersEntity.USERS_ID );
   subResource( UsersResource.class );
}

@SubResource
public void cases()
{
   subResource( WorkspaceCasesResource.class );
}

@SubResource
public void organizations()
{
   setRole( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
   subResource( OrganizationsResource.class );
}

@SubResource
public void surface()
{
   setRole( UsersEntity.class, UsersEntity.USERS_ID );
   subResource( SurfaceResource.class );
}

@SubResource
public void services()
{
   subResourceContexts( ServicesContext.class );
}*/
}