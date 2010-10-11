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

package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;

/**
 * Management of owned entities
 */
public class DefinitionListModel
   extends LinkValueListModel
{
   private String changeDescription;
   private final String create;

   public DefinitionListModel( String create )
   {
      this("changedescription", create);
   }

   public DefinitionListModel( String changedescription, String create )
   {
      this("index", changedescription, create);
   }

   public DefinitionListModel(String refresh, String changedescription, String create)
   {
      super(refresh);

      this.changeDescription = changedescription;
      this.create = create;
   }

   public void changeDescription( LinkValue link, String newName )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( newName );

         client.getSubClient( link.id().get() ).putCommand( changeDescription, builder.newInstance() );
      } catch (ResourceException e)
      {
         handleException( e );
      }
   }

   public void create( String name )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( name );
      try
      {
         client.postCommand( create, builder.newInstance() );
      } catch (ResourceException e)
      {
         handleException( e );
      }
   }


   public EventList<LinkValue> usages(LinkValue link)
   {
      LinksValue usages = client.getClient( link ).query( "usages", LinksValue.class );
      EventList<LinkValue> eventList = new BasicEventList<LinkValue>();
      EventListSynch.synchronize( usages.links().get(), eventList );

      return eventList;
   }
}