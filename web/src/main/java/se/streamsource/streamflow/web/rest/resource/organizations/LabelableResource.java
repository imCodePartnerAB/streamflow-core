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
package se.streamsource.streamflow.web.rest.resource.organizations;

import static se.streamsource.dci.api.RoleMap.role;

import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.general.LabelableContext;
import se.streamsource.streamflow.web.context.workspace.cases.general.LabeledContext;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;

/**
 * JAVADOC
 */
public class LabelableResource
   extends CommandQueryResource
   implements SubResources
{
   public LabelableResource( )
   {
      super( LabelableContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      findManyAssociation( role(Labelable.Data.class).labels(), segment );
      subResourceContexts( LabeledContext.class );
   }
}