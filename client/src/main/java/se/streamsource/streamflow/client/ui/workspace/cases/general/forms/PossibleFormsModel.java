/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.util.LinkValueListModel;

public class PossibleFormsModel
   extends LinkValueListModel
{
   public FormDraftModel getFormDraftModel(String id)
   {
      CommandQueryClient possibleFormClient = client.getSubClient( id );

      LinkValue formDraftLink = null;
      try {
         formDraftLink = possibleFormClient.query( "formdraft", LinkValue.class );
      } catch (ResourceException e) {
         if (e.getStatus().getCode() == Status.CLIENT_ERROR_NOT_FOUND.getCode()) {
            possibleFormClient.postCommand( "create" );
            formDraftLink = possibleFormClient.query( "formdraft", LinkValue.class );
         } else {
            throw e;
         }
      }

      // get the form submission value;
      final CommandQueryClient formDraftClient = possibleFormClient.getClient( formDraftLink );

      return module.objectBuilderFactory().newObjectBuilder(FormDraftModel.class).use(formDraftClient).newInstance();
   }
}
