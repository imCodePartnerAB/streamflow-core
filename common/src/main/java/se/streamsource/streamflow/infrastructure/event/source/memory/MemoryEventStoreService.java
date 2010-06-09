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

package se.streamsource.streamflow.infrastructure.event.source.memory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.AbstractEventStoreMixin;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * In-Memory EventStore. Mainly used for testing.
 */
@Mixins(MemoryEventStoreService.MemoryEventStoreMixin.class)
public interface MemoryEventStoreService
      extends EventStore, EventListener, Activatable, ServiceComposite
{
   public List<DomainEvent> getEvents();

   public void clearEvents();

   abstract class MemoryEventStoreMixin
         extends AbstractEventStoreMixin
         implements EventListener, MemoryEventStoreService
   {
      private TreeMap<Long, String> store = new TreeMap<Long, String>();

      public void activate() throws IOException
      {
         super.activate();
      }

      public void passivate() throws Exception
      {
      }

      public void transactionsAfter( long afterTimestamp, TransactionVisitor visitor )
      {
         // Lock datastore first
         lock.lock();
         try
         {
            Long startTime = afterTimestamp + 1;
            Collection<String> txsAfterDate = store.tailMap( startTime ).values();

            for (String txJson : txsAfterDate)
            {
               JSONTokener tokener = new JSONTokener( txJson );
               JSONObject json = (JSONObject) tokener.nextValue();
               TransactionEvents tx = (TransactionEvents) transactionEventsType.fromJSON( json, module );

               if (!visitor.visit( tx ))
               {
                  return;
               }
            }
         } catch (JSONException e)
         {
            logger.log( Level.WARNING, "Could not deserialize events", e );
         } finally
         {
            lock.unlock();
         }
      }

      public void transactionsBefore( long beforeTimestamp, TransactionVisitor visitor )
      {
         // Lock datastore first
         lock.lock();
         try
         {
            Long startTime = beforeTimestamp - 1;
            Collection<String> txsBeforeDate = store.headMap( startTime ).values();

            // Reverse the list - this could be done more easily in JDK1.6
            LinkedList<String> values = new LinkedList<String>();
            for (String json : txsBeforeDate)
            {
               values.addFirst( json );
            }

            for (String txJson : values)
            {
               JSONTokener tokener = new JSONTokener( txJson );
               JSONObject json = (JSONObject) tokener.nextValue();
               TransactionEvents tx = (TransactionEvents) transactionEventsType.fromJSON( json, module );

               if (!visitor.visit( tx ))
               {
                  return;
               }
            }
         } catch (JSONException e)
         {
            logger.log( Level.WARNING, "Could not deserialize events", e );
         } finally
         {
            lock.unlock();
         }
      }

      protected void rollback()
            throws IOException
      {
      }

      protected void commit()
            throws IOException
      {
      }

      protected void storeEvents( TransactionEvents transaction )
            throws IOException
      {
         String jsonString = transaction.toString();
         store.put( transaction.timestamp().get(), jsonString );
      }

      @Structure
      UnitOfWorkFactory uowf;

      @Override
      public void notifyEvent( DomainEvent event )
      {
         super.notifyEvent( event );
         if (events == null)
         {
            events = new ArrayList<DomainEvent>();
            UnitOfWork uow = uowf.currentUnitOfWork();
            uow.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {

               public void beforeCompletion() throws UnitOfWorkCompletionException
               {
                  events = null;
               }

               public void afterCompletion( UnitOfWorkCallback.UnitOfWorkStatus status )
               {
               }
            } );
         }

         events.add( event );
      }

      public List<DomainEvent> getEvents()
      {
         return events;
      }

      public void clearEvents()
      {
         events = null;
      }

      private List<DomainEvent> events;

   }
}