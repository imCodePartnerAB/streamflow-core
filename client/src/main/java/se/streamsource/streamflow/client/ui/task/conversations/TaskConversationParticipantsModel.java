/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.task.conversations;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

public class TaskConversationParticipantsModel
   implements EventListener, EventVisitor, Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   SortedList<LinkValue> participants = new SortedList<LinkValue>(new BasicEventList<LinkValue>( ), new LinkComparator());
   EventVisitorFilter eventFilter = new EventVisitorFilter( this,"removedParticipant", "addedParticipant" );

   public EventList<LinkValue> participants()
   {
      return participants;
   }

   public EventList<LinkValue> possibleParticipants()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query("possibleparticipants", LinksValue.class);
         list.addAll(listValue.links().get());

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e);
      }
   }

   public void addParticipant( EntityReference participant )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( participant );
         client.postCommand( "addparticipant", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_add_conversation_participant, e );
      }
   }

   public void removeParticipant( LinkValue link )
   {
      try
      {
         client.getSubClient( link.id().get() ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_remove_conversation_participant, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      refresh();
      
      return false;
   }

   public void refresh() throws OperationException
   {
     try
      {
         LinksValue participants = client.query( "index", LinksValue.class );
         EventListSynch.synchronize( participants.links().get(), this.participants );

      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }
}