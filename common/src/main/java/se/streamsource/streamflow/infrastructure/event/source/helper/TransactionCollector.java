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

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class TransactionCollector
      implements TransactionVisitor
{
   List<TransactionEvents> transactions = new ArrayList<TransactionEvents>();

   public boolean visit( TransactionEvents transaction )
   {
      transactions.add( transaction );
      return true;
   }

   public Iterable<TransactionEvents> transactions()
   {
      return transactions;
   }
}