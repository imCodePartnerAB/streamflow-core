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

package se.streamsource.streamflow.web.context.surface.accesspoints;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class AccessPointsContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      UserAuthentication authentication = role( UserAuthentication.class );

      if (!(authentication instanceof ProxyUser))
      {
         TitledLinksBuilder builder = new TitledLinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<LinkValue> valueBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         valueBuilder.prototype().href().set( ".." );
         valueBuilder.prototype().id().set( "" );
         valueBuilder.prototype().text().set( "back" );

         builder.addLink( valueBuilder.newInstance() );
         builder.addTitle( "USER NOT A PROXY USER" );

         return builder.newLinks();
      }

      ProxyUser proxyUser = (ProxyUser) authentication;
      Organization organization = proxyUser.organization().get();

      AccessPoints.Data data = (AccessPoints.Data) organization;

      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

      linksBuilder.addDescribables( data.accessPoints() );

      return linksBuilder.newLinks();
   }
}