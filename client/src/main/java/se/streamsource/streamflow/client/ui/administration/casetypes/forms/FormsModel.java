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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.DefinitionListModel;

/**
 * JAVADOC
 */
public class FormsModel
   extends DefinitionListModel
{
   public FormsModel( )
   {
      super( "createform" );
   }

   public EventList<LinkValue> getPossibleMoveTo()
   {
      BasicEventList<LinkValue> possibleLinks = new BasicEventList<LinkValue>();
      possibleLinks.addAll( client.query( "possiblemoveto", LinksValue.class ).links().get() );
      return possibleLinks;
   }

   public void moveForm( LinkValue selected, LinkValue selectedLink )
   {
      client.getClient( selected ).postLink( selectedLink );
   }
}