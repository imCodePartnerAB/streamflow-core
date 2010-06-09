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

package se.streamsource.streamflow.infrastructure.event.source.helper;

import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

/**
 * Takes a list of TransactionEvents and filters them according to a given event specification.
 */
public class TransactionTimestampFilter
      implements TransactionVisitor
{
   private TransactionVisitor visitor;
   private long lastTimestamp;

   public TransactionTimestampFilter( long lastTimestamp, TransactionVisitor visitor )
   {
      this.lastTimestamp = lastTimestamp;
      this.visitor = visitor;
   }

   public boolean visit( TransactionEvents transaction )
   {
      try
      {
         return visitor.visit( transaction );
      } finally
      {
         lastTimestamp = transaction.timestamp().get();
      }
   }

   /**
    * Timestamp of the last evalutated transaction. This can be used as input
    * to the next call to {@link se.streamsource.streamflow.infrastructure.event.source.EventStore#transactionsAfter(long, TransactionVisitor)} }.
    *
    * @return
    */
   public long lastTimestamp()
   {
      return lastTimestamp;
   }
}