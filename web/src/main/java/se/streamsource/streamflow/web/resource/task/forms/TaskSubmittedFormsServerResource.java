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

package se.streamsource.streamflow.web.resource.task.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.task.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.domain.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.tasktype.TypedTask;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.Date;

import static org.qi4j.api.entity.EntityReference.*;

/**
 * Mapped to:
 * /tasks/{task}/forms/
 */
public class TaskSubmittedFormsServerResource
      extends CommandQueryServerResource
{
   @Structure
   ValueBuilderFactory vbf;

   public TaskSubmittedFormsServerResource()
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }

   public SubmittedFormsListDTO tasksubmittedforms()
   {
      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();
      SubmittedFormsQueries forms = uowf.currentUnitOfWork().get( SubmittedFormsQueries.class, formsQueryId );

      return forms.getSubmittedForms();
   }

   public void submitform( SubmitFormDTO submitDTO )
   {
      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();
      SubmittedForms forms = uowf.currentUnitOfWork().get( SubmittedForms.class, formsQueryId );

      UserEntity user = uowf.currentUnitOfWork().get( UserEntity.class, getClientInfo().getUser().getIdentifier() );

      ValueBuilder<SubmittedFormValue> formBuilder = vbf.newValueBuilder( SubmittedFormValue.class );

      formBuilder.prototype().submitter().set( getEntityReference( user ) );
      formBuilder.prototype().form().set( submitDTO.form().get() );
      formBuilder.prototype().submissionDate().set( new Date() );
      formBuilder.prototype().values().set( submitDTO.values().get() );

      forms.submitForm( formBuilder.newInstance() );
   }

   public ListValue applicableforms()
   {
      String taskId = getRequest().getAttributes().get( "task" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();

      TypedTask.Data typedTask = uow.get( TypedTask.Data.class, taskId );

      TaskType taskType = typedTask.taskType().get();

      ListValue formsList;
      if (taskType != null)
      {
         FormsQueries forms = uow.get( FormsQueries.class, getEntityReference( taskType ).identity() );
         formsList = forms.applicableFormDefinitionList();
      } else
      {
         formsList = vbf.newValue( ListValue.class );
      }
      return formsList;
   }

   public EffectiveFieldsDTO effectivefields()
   {
      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();

      SubmittedFormsQueries fields = uowf.currentUnitOfWork().get( SubmittedFormsQueries.class, formsQueryId );

      return fields.effectiveFields();
   }

   @Override
   protected String getConditionalIdentityAttribute()
   {
      return "task";
   }
}