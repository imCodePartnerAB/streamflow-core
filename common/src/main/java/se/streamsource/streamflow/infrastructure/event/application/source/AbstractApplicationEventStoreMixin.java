/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.infrastructure.event.application.source;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.io.*;
import org.qi4j.api.service.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.property.*;
import org.qi4j.spi.structure.*;
import org.slf4j.*;
import se.streamsource.streamflow.infrastructure.event.application.*;
import se.streamsource.streamflow.infrastructure.time.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import static java.util.Collections.*;

/**
 * Base implementation for ApplicationEventStores.
 */
public abstract class AbstractApplicationEventStoreMixin
      implements ApplicationEventStore, ApplicationEventStream, Activatable
{
   @Service
   Time time;

   @This
   protected Identity identity;

   protected Logger logger;
   protected ValueType domainEventType;
   protected ValueType transactionEventsType;

   protected Lock lock = new ReentrantLock();

   @Structure
   protected ModuleSPI module;

   @Structure
   private ValueBuilderFactory vbf;

   private ExecutorService transactionNotifier;

   final private List<Output<TransactionApplicationEvents, ? extends Throwable>> listeners = synchronizedList( new ArrayList<Output<TransactionApplicationEvents, ? extends Throwable>>() );

   private long lastTimestamp = 0;

   public void activate() throws IOException
   {
      logger = LoggerFactory.getLogger( identity.identity().get() );

      domainEventType = module.valueDescriptor( ApplicationEvent.class.getName() ).valueType();
      transactionEventsType = module.valueDescriptor( TransactionApplicationEvents.class.getName() ).valueType();

      transactionNotifier = Executors.newSingleThreadExecutor();
   }

   public void passivate() throws Exception
   {
      transactionNotifier.shutdown();
      transactionNotifier.awaitTermination( 10000, TimeUnit.MILLISECONDS );
   }

   // TransactionVisitor implementation
   // This is how transactions are put into the store


   public TransactionApplicationEvents storeEvents( Iterable<ApplicationEvent> events ) throws IOException
   {
      // Create new TransactionApplicationEvents
      ValueBuilder<TransactionApplicationEvents> builder = vbf.newValueBuilder( TransactionApplicationEvents.class );
      Iterables.addAll( builder.prototype().events().get(), events );
      builder.prototype().timestamp().set( getCurrentTimestamp() );

      final TransactionApplicationEvents transactionDomain = builder.newInstance();

      // Lock store so noone else can interrupt
      lock();
      try
      {
         storeEvents( transactionDomain );
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
               Input<TransactionApplicationEvents, RuntimeException> input = Inputs.iterable( Collections.singleton( transactionDomain ) );
               for (Output<TransactionApplicationEvents, ? extends Throwable> listener : listeners)
               {
                  try
                  {
                     input.transferTo( listener );
                  } catch (Throwable e)
                  {
                     logger.warn( "Could not notify event listener", e );
                  }
               }
            }
         }
      } );

      return transactionDomain;
   }

   // EventStream implementation


   public void registerListener( Output<TransactionApplicationEvents, ? extends Throwable> listener )
   {
      listeners.add( listener );
   }


   public void unregisterListener( Output<TransactionApplicationEvents, ? extends Throwable> listener )
   {
      listeners.remove( listener );
   }

   abstract protected void rollback()
         throws IOException;

   abstract protected void commit()
         throws IOException;

   abstract protected void storeEvents( TransactionApplicationEvents transactionDomain )
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

   private synchronized long getCurrentTimestamp()
   {
      long timestamp = time.timeNow();
      if (timestamp <= lastTimestamp)
         timestamp = lastTimestamp + 1; // Increase by one to ensure uniqueness
      lastTimestamp = timestamp;
      return timestamp;
   }
}
