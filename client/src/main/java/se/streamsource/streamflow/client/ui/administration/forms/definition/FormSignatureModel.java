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

package se.streamsource.streamflow.client.ui.administration.forms.definition;

import org.qi4j.api.injection.scope.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import java.util.*;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class FormSignatureModel
      extends Observable
      implements Refreshable, TransactionListener

{
   @Uses
   CommandQueryClient client;

   private RequiredSignatureValue formSignature;

   public void refresh() throws OperationException
   {
      formSignature = client.query( "index", RequiredSignatureValue.class );
      setChanged();
      notifyObservers( this );
   }

   public RequiredSignatureValue getFormSignature()
   {
      return formSignature;
   }

   public void update( RequiredSignatureValue signature )
   {
      client.putCommand( "update", signature );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      // Refresh if the owner of the list has changed
      if (matches( onEntities( client.getReference().getParentRef().getParentRef().getLastSegment() ), transactions ))
         refresh();
   }
}