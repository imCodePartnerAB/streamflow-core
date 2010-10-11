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

package se.streamsource.streamflow.client.ui.caze;

import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for handling a form submission and subsequently submitting it
 */
public class FormSubmissionModel
{
   private ValueBuilderFactory vbf;
   private CommandQueryClient client;
   private FormSubmissionValue formSubmission;
   private List<FormSubmissionWizardPage> pages;

   public FormSubmissionModel( @Uses CommandQueryClient client,
                               @Structure ObjectBuilderFactory obf,
                               @Structure ValueBuilderFactory vbf )
   {
      this.vbf = vbf;
      this.client = client;
      try
      {
         formSubmission = (FormSubmissionValue) client.query( "formsubmission", FormSubmissionValue.class ).buildWith().prototype();
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_get_form_submission, e );
      }
      pages = new ArrayList<FormSubmissionWizardPage>( formSubmission.pages().get().size() );
      for (PageSubmissionValue page : formSubmission.pages().get())
      {
         if (page.fields().get() != null && page.fields().get().size() > 0)
         {
            pages.add( obf.newObjectBuilder( FormSubmissionWizardPage.class )
                  .use( this, page ).newInstance() );
         }
      }
   }

   public WizardPage[] getPages()
   {
      WizardPage[] wizardPages = new WizardPage[pages.size()];
      pages.toArray( wizardPages );
      return wizardPages;
   }

   public String getTitle()
   {
      return formSubmission.description().get();
   }

   public void updateField( EntityReference reference, String value ) throws ResourceException
   {
      ValueBuilder<FieldValueDTO> builder = vbf.newValueBuilder( FieldValueDTO.class );
      builder.prototype().field().set( reference );
      builder.prototype().value().set( value );

      client.postCommand( "updatefield", builder.newInstance() );
   }

   public void previousPage()
   {
      client.putCommand( "previouspage" );
   }

   public void nextPage()
   {
      client.putCommand( "nextpage" );
   }

/* TODO This is all wrong. Why is a model holding the view!???
   public void notifyEvent( DomainEvent event )
   {
      FormSubmissionValue value = vbf.newValueFromJSON( FormSubmissionValue.class, EventParameters.getParameter( event, "param1" ) );

      if (value.form().get().identity().equals( formSubmission.form().get().identity() ))
      {
         for (int i = 0; i < value.pages().get().size(); i++)
         {
            PageSubmissionValue pageSubmissionValue = value.pages().get().get( i );
            FormSubmissionWizardPage submissionWizardPage = pages.get( i );

            submissionWizardPage.updatePage( pageSubmissionValue );
         }
      }
   }
*/
}