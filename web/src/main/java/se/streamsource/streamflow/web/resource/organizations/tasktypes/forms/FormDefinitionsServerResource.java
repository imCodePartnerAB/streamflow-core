/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.organizations.tasktypes.forms;

import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.entity.form.FormsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms
 */
public class FormDefinitionsServerResource
      extends CommandQueryServerResource
{
   public ListValue forms()
   {
      String identity = getRequest().getAttributes().get( "forms" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      FormsQueries forms = uow.get( FormsQueries.class, identity );

      checkPermission( forms );

      return forms.applicableFormDefinitionList();
   }

   public void remove( EntityReferenceDTO formReference ) throws ResourceException
   {
      String identity = getRequest().getAttributes().get( "forms" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      Form form;
      try
      {
         form = uow.get( Form.class, formReference.entity().get().identity() );

         checkPermission( form );
      } catch (NoSuchEntityException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
      }

      Forms forms = uow.get( Forms.class, identity );

      forms.removeForm( form );
   }


   public void create( StringDTO formName )
   {
      String identity = getRequest().getAttributes().get( "forms" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      Forms forms = uow.get( Forms.class, identity );

      checkPermission( forms );

      Form form = forms.createForm();
      form.changeDescription( formName.string().get() );
   }

}