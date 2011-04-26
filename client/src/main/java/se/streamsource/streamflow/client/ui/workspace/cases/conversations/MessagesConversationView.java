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

package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;

public class MessagesConversationView extends MessagesView
{

   private static final long serialVersionUID = 8844722606127717377L;

   public MessagesConversationView(@Service ApplicationContext context, @Uses CommandQueryClient client,
         @Structure ObjectBuilderFactory obf)
   {
      super(context,client,obf);
   }
   
   @Action
   public void writeMessage()
   {
      super.writeMessage();
   }
   
   @Action
   public Task sendMessage()
   {
      return super.sendMessage();
   }
   
   @Action
   public void cancelNewMessage()
   {
      super.cancelNewMessage();
   }

   @Action
   public void closeMessageDetails()
   {
      super.closeMessageDetails();
   }
}