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
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;

import java.util.logging.Logger;

public class TaskConversationsModel
   implements EventListener, Refreshable, EventVisitor
{
   @Uses
   CommandQueryClient client;

   @Uses
   private TaskConversationModel conversationDetail;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   WeakModelMap<String,TaskConversationModel> conversationModels = new WeakModelMap<String,TaskConversationModel>(){

      @Override
      protected TaskConversationModel newModel( String key )
      {
         return obf.newObjectBuilder( TaskConversationModel.class ).use(
               client.getSubClient( key ),
               obf.newObjectBuilder( TaskConversationParticipantsModel.class ).
                     use(client.getSubClient( key ).getSubClient( "participants" )).newInstance(),
               obf.newObjectBuilder( MessagesModel.class ).
                     use( client.getSubClient(key).getSubClient( "messages" )).newInstance()).newInstance();
      }
   };

   TransactionList<ConversationDTO> conversations = new TransactionList<ConversationDTO>(new BasicEventList<ConversationDTO>( ));

   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "createdConversation", "addedParticipant", "createdMessage" );

   public void refresh()
   {
      try
      {
         LinksValue newConversations = client.query( "index", LinksValue.class );
         EventListSynch.synchronize( newConversations.links().get(), conversations );
      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public EventList<ConversationDTO> conversations()
   {
      return  conversations;
   }

   public void createConversation( String topic )
   {
      try
      {
         ValueBuilder<StringDTO> newTopic = vbf.newValue( StringDTO.class ).buildWith();
         newTopic.prototype().string().set( topic );
         client.postCommand( "create", newTopic.newInstance() );
         
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_create_conversation, e );
      }
   }

    public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
      for(TaskConversationModel conversationModel : conversationModels)
      {
         conversationModel.notifyEvent( event );
      }
   }

   public boolean visit( DomainEvent event )
   {
      if (client.getReference().getParentRef().getLastSegment().equals( event.entity().get() ))
      {
         Logger.getLogger( "workspace" ).info( "Refresh task conversations" );
         refresh();
      } else if( event.name().get().equals("createdMessage"))
      {
         for (ConversationDTO conversation : conversations)
         {
            if(conversation.id().get().equals( event.entity().get() ))
            {
               ConversationDTO updated = conversation.<ConversationDTO>buildWith().prototype();
               updated.messages().set( conversation.messages().get() + 1 );
               conversations.set( conversations.indexOf(conversation), updated );
            }
         }
      } else if( event.name().get().equals("addedParticipant"))
      {
         for (ConversationDTO conversation : conversations)
         {
            if(conversation.id().get().equals( event.entity().get() ))
            {
               ConversationDTO updated = conversation.<ConversationDTO>buildWith().prototype();
               updated.participants().set( conversation.participants().get() + 1 );
               conversations.set( conversations.indexOf(conversation), updated );
            }
         }
      }

      return false;
   }
}
