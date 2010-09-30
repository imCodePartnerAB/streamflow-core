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

package se.streamsource.streamflow.infrastructure.event.source;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.*;

/**
 * Base implementation for EventStores.
 */
public abstract class AbstractEventStoreMixin
      implements EventStore, TransactionVisitor, Activatable
{
   @This
   protected Identity identity;

   protected Logger logger;
   protected ValueType domainEventType;
   protected ValueType transactionEventsType;

   protected ReentrantLock lock = new ReentrantLock();

   @Structure
   protected ModuleSPI module;

   @Structure
   private UnitOfWorkFactory uowf;

   @Structure
   private ValueBuilderFactory vbf;

   private ExecutorService transactionNotifier;

   final private List<TransactionVisitor> listeners = synchronizedList( new ArrayList<TransactionVisitor>() );

   public void activate() throws IOException
   {
      logger = LoggerFactory.getLogger( identity.identity().get() );

      domainEventType = module.valueDescriptor( DomainEvent.class.getName() ).valueType();
      transactionEventsType = module.valueDescriptor( TransactionEvents.class.getName() ).valueType();

      transactionNotifier = Executors.newSingleThreadExecutor();
   }

   public void passivate() throws Exception
   {
      transactionNotifier.shutdown();
      transactionNotifier.awaitTermination( 10000, TimeUnit.MILLISECONDS );
   }

   // TransactionVisitor implementation
   // This is how transactions are put into the store

   public boolean visit( final TransactionEvents transaction )
   {
      // Lock store so noone else can interrupt
      lock();
      try
      {
         storeEvents( transaction );

      } catch (Exception e)
      {
         logger.error( "Could not store events", e );
         return false;
      } finally
      {
         lock.unlock();
      }

      // Notify listeners
      transactionNotifier.submit( new Runnable()
      {
         public void run()
         {
            synchronized (listeners)
            {
               for (TransactionVisitor listener : listeners)
               {
                  try
                  {
                     listener.visit( transaction );
                  } catch (Exception e)
                  {
                     logger.warn( "Could not notify event listener", e );
                  }
               }
            }
         }
      } );

      return true;
   }

   // EventSource implementation

   public void registerListener( TransactionVisitor subscriber )
   {
      listeners.add( subscriber );
   }

   public void unregisterListener( TransactionVisitor subscriber )
   {
      listeners.remove( subscriber );
   }

   abstract protected void rollback()
         throws IOException;

   abstract protected void commit()
         throws IOException;

   abstract protected void storeEvents( TransactionEvents transaction )
         throws IOException;

   /**
    * Fix for this bug:
    * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
    */
   protected void lock()
   {
      while (true)
      {
         try
         {
            lock.tryLock( 1000, TimeUnit.MILLISECONDS );
            break;
         } catch (InterruptedException e)
         {
            // Try again
         }
      }
   }
}
