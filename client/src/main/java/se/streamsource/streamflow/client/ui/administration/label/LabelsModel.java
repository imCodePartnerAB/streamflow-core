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

package se.streamsource.streamflow.client.ui.administration.label;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.LinkValueListModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * Management of labels on an organizational or user level
 */
public class LabelsModel
   extends LinkValueListModel
      implements EventListener, Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   public EventList<LinkValue> getLabelList()
   {
      return linkValues;
   }

   public void refresh()
   {
      try
      {
         // Get label list
         LinksValue newList = client.query( "index", LinksValue.class );

         EventListSynch.synchronize( newList.links().get(), linkValues );

      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_labels, e );
      }
   }

   public void createLabel( String description )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( description );
         client.postCommand( "createlabel", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_create_label, e );
      }
   }

   public void removeLabel( LinkValue label)
   {
      try
      {
         client.getClient( label ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_label, e );
      }
   }

   public void changeDescription( LinkValue label, String name )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( name );
         client.getClient( label ).putCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_change_description, e );
      }
   }

   public EventList<LinkValue> usages(LinkValue label)
   {
      try
      {
         LinksValue usages = client.getClient(label).query( "usages", LinksValue.class );
         EventList<LinkValue> eventList = new BasicEventList<LinkValue>();
         EventListSynch.synchronize( usages.links().get(), eventList );

         return eventList;
      } catch (ResourceException e)
      {
         throw new OperationException(AdministrationResources.could_not_perform_query, e);
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }
}