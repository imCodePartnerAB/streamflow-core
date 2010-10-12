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

package se.streamsource.streamflow.client.infrastructure.ui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;

import java.util.Collection;

/**
 * Synchronize an EventList with a collection. This is used for getting updates
 * from the server and showing them in the UI.
 * In conjunction with a SortedList <code>EventListSynch.synchronize()</code> may only be called on the
 * underlying BasicEventList!!
 */
public class EventListSynch
{
   public static <T, P extends T> void synchronize( Collection<T> list, EventList<P> eventList )
   {
      eventList.getReadWriteLock().writeLock().lock();
      try
      {
         if (list.size() == eventList.size())
         {
            // Replace items
            if (eventList instanceof TransactionList)
               ((TransactionList) eventList).beginEvent();

            int idx = 0;
            for (Object item : list)
            {
               if (!item.equals( eventList.get(idx )))
                  eventList.set( idx++, (P) item );
               idx++;
            }

            if (eventList instanceof TransactionList)
               ((TransactionList) eventList).commitEvent();
         } else
         {
            // Replace items
            if (eventList instanceof TransactionList)
               ((TransactionList) eventList).beginEvent();

            eventList.clear();
            eventList.addAll( (Collection<? extends P>) list );

            if (eventList instanceof TransactionList)
               ((TransactionList) eventList).commitEvent();
         }
      } catch (Exception e)
      {
         eventList.getReadWriteLock().writeLock().unlock();
      }
   }
}
