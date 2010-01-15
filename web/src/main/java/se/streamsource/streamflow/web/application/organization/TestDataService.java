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

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.PageBreakFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.web.domain.entity.gtd.Inbox;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormTemplates;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organizations.Organizations;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.ProjectRole;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.task.Task;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Generates test data
 */
@Mixins(TestDataService.Mixin.class)
public interface TestDataService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      @Structure
      Application app;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      public void activate() throws Exception
      {
         UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Test data" ) );

         UserEntity user = uow.get( UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME );

         Organizations orgs = uow.get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );

         User testUser = orgs.createUser( "testuser", "testuser" );
         User someUser = orgs.createUser( "someuser", "someuser" );
         User someUser2 = orgs.createUser( "someuser2", "someuser2" );

         OrganizationEntity organization = (OrganizationEntity) user.organizations().iterator().next();
         organization.changeDescription( "WayGroup" );
         testUser.join( organization );
         someUser.join( organization );
         someUser2.join( organization );

         Label question = organization.createLabel( "Question" );
         Label issueChase = organization.createLabel( "Issue chase" );
         Label suggestion = organization.createLabel( "Suggestion" );

         Label minor = organization.createLabel( "Minor" );
         Label major = organization.createLabel( "Major" );
         Label critical = organization.createLabel( "Critical" );

         // Create task types
         TaskType newFeature = organization.createTaskType( "New feature" );
         TaskType bug = organization.createTaskType( "Bug" );
         bug.addLabel( minor );
         bug.addLabel( major );
         bug.addLabel( critical );
         TaskType improvement = organization.createTaskType( "Improvement" );
         improvement.addLabel( minor );
         improvement.addLabel( major );
         TaskType passwordReset = organization.createTaskType( "Reset password" );

         // Create suborganizations
         OrganizationalUnit jayway = organization.createOrganizationalUnit( "Jayway" );
         OrganizationalUnit streamSource = organization.createOrganizationalUnit( "StreamSource" );
         OrganizationalUnit admin = organization.createOrganizationalUnit( "Administration" );

         // Create groups
         Group developers = jayway.createGroup( "Developers" );
         Group admins = admin.createGroup( "Administrators" );

         developers.addParticipant( user );
         developers.addParticipant( someUser );

         admins.addParticipant( testUser );
         admins.addParticipant( someUser );

         FormTemplates forms = (FormTemplates) organization;

         ProjectRole agent = organization.createProjectRole( "Agent" );
         ProjectRole manager = organization.createProjectRole( "Manager" );

         // Create tasks
         for (int i = 0; i < 30; i++)
            user.createTask().changeDescription( "Arbetsuppgift " + i );

         // Create project
         Project project = jayway.createProject( "StreamFlow" );

         project.addSelectedTaskType( newFeature );
         project.addSelectedTaskType( bug );
         project.addSelectedTaskType( improvement );

         Form bugreport = bug.createForm();
         bugreport.changeDescription( "Bug Report" );
         bugreport.changeNote( "A form to capture a bug report" );
         ValueBuilder<TextFieldValue> builder = vbf.newValueBuilder( TextFieldValue.class );
         builder.prototype().width().set( 30 );
         ValueBuilder<DateFieldValue> dateBuilder = vbf.newValueBuilder( DateFieldValue.class );
         ValueBuilder<PageBreakFieldValue> pageBreakBuilder = vbf.newValueBuilder( PageBreakFieldValue.class );
         ValueBuilder<NumberFieldValue> numberBuilder = vbf.newValueBuilder( NumberFieldValue.class );
         ValueBuilder<SelectionFieldValue> selectionBuilder = vbf.newValueBuilder( SelectionFieldValue.class );
         List<String> values = new ArrayList<String>();
         values.add( "Critical" );
         values.add( "High" );
         values.add( "Normal" );
         values.add( "Low" );
         selectionBuilder.prototype().values().set( values );
         builder.prototype().mandatory().set( true );
         bugreport.createField( "Bugname", builder.newInstance() );
         numberBuilder.prototype().integer().set( true );
         numberBuilder.prototype().mandatory().set( true );
         bugreport.createField( "Bug ID", numberBuilder.newInstance() );
         builder.prototype().rows().set( 5 );
         builder.prototype().mandatory().set( false );
         bugreport.createField( "Description", builder.newInstance() );
         builder.prototype().mandatory().set( true );
         bugreport.createField( "Date Information", pageBreakBuilder.newInstance() );
         dateBuilder.prototype().mandatory().set( true );
         bugreport.createField( "Discovered", dateBuilder.newInstance() );
         selectionBuilder.prototype().mandatory().set( true );
         bugreport.createField( "Priority", selectionBuilder.newInstance() );
         selectionBuilder.prototype().multiple().set( true );
         selectionBuilder.prototype().mandatory().set( false );
         values.clear();
         values.add( "Server" );
         values.add( "Client" );
         bugreport.createField( "Bug Location", selectionBuilder.newInstance() ).changeNote( "Indicate what part of the application the bug is. Optional" );

         Form statusForm = bug.createForm();
         statusForm.changeDescription( "StatusForm" );
         statusForm.changeNote( "This is the Status form. \nWhen urgencies occur please upgrade the status of the current task" );
         Field statusField = statusForm.createField( "Status", builder.newInstance() );

         organization.createFormTemplate( bugreport );

         Form emailForm = improvement.createForm();
         emailForm.changeDescription( "Email form" );
         emailForm.changeNote( "Form for entering and sending an email" );
         builder.prototype().rows().set( 0 );
         emailForm.createField( "To", builder.newInstance() ).changeNote( "Enter address of receiver. Note it must be a valid email" );
         emailForm.createField( "Subject", builder.newInstance() ).changeNote( "Subject of the mail" );
         builder.prototype().rows().set( 10 );
         emailForm.createField( "Content", builder.newInstance() ).changeNote( "Mail content" );

         Form resetPasswordForm = passwordReset.createForm();
         resetPasswordForm.changeDescription( "Reset password" );
         resetPasswordForm.changeNote( "Reset password for a user" );
         builder.prototype().rows().set( 0 );
         resetPasswordForm.createField( "Username", builder.newInstance() ).changeNote( "Username whose password should be reset" );

         // Create labels
         project.addLabel( question );
         project.addLabel( issueChase );
         project.addLabel( suggestion );

         for (int i = 0; i < 50; i++)
         {
            Label label = organization.createLabel( "Label " + i );
            project.addLabel( label );
         }

         project.addMember( user );

         // Create project
         Project info2 = jayway.createProject( "StreamForm" );
         info2.addSelectedTaskType( newFeature );
         info2.addSelectedTaskType( bug );
         info2.addSelectedTaskType( improvement );

         info2.addMember( (Member) developers );
         info2.addMember( user );

         Project itSupport = admin.createProject( "IT support" );
         itSupport.addSelectedTaskType( passwordReset );
         itSupport.addMember( user );

         Project invoicing = admin.createProject( "Invoicing" );
         invoicing.addMember( user );

         // Create tasks
         Task task = ((Inbox)project).createTask();
         task.changeDescription( "Arbetsuppgift 0" );

         SubmittedFormValue submitted = createSubmittedForm( user, statusForm, statusField, "Progress is slow" );
         task.submitForm( submitted );

         submitted = createSubmittedForm( user, statusForm, statusField, "Progress is getting better" );
         task.submitForm( submitted );


         for (int i = 1; i < 30; i++)
            ((Inbox)project).createTask().changeDescription( "Arbetsuppgift " + i );

         // Create labels
         for (int i = 1; i < 10; i++)
            user.createLabel( "Label " + i );


         uow.complete();
      }

      private SubmittedFormValue createSubmittedForm( UserEntity user, Form form, Field field, String value )
      {
         ValueBuilder<SubmittedFormValue> builder = vbf.newValueBuilder( SubmittedFormValue.class );
         builder.prototype().submissionDate().set( new Date() );
         builder.prototype().submitter().set( EntityReference.getEntityReference( user ) );
         builder.prototype().form().set( EntityReference.getEntityReference( form ) );

         List<SubmittedFieldValue> list = builder.prototype().values().get();

         ValueBuilder<SubmittedFieldValue> fieldBuilder = vbf.newValueBuilder( SubmittedFieldValue.class );
         fieldBuilder.prototype().field().set( EntityReference.getEntityReference( field ) );
         fieldBuilder.prototype().value().set( value );
         list.add( fieldBuilder.newInstance() );

         return builder.newInstance();
      }


      public void passivate() throws Exception
      {
      }
   }
}