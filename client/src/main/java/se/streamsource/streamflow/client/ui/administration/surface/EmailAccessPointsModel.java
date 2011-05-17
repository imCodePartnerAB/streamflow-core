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

package se.streamsource.streamflow.client.ui.administration.surface;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Form;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * TODO
 */
public class EmailAccessPointsModel
   extends LinkValueListModel
      implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   @Uses
   CommandQueryClient client;

   public EmailAccessPointsModel()
   {
      relationModelMapping("resource", EmailAccessPointModel.class);
   }

   public void createEmailAccessPoint( String email )
   {
      Form form = new Form();
      form.set("email", email);
      client.postCommand( "create", form.getWebRepresentation() );
   }
}
