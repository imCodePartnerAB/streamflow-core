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

package se.streamsource.streamflow.client.ui.administration;

import java.io.IOException;
import java.util.Observable;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Uniform;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.overview.OverviewProjectsNode;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryModel;
import se.streamsource.streamflow.client.ui.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.client.ui.task.TasksModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceUserAssignmentsNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceUserDelegationsNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceUserInboxNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceUserWaitingForNode;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
/**
 * JAVADOC
 */
public class AccountModel extends Observable implements EventListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   UnitOfWorkFactory uowf;

   @Service
   IndividualRepository individualRepository;

   @Structure
   ValueBuilderFactory vbf;

   @Service
   Uniform client;

   @Uses
   Account account;

   // @Uses
   // private ContactValue contact;

   private WorkspaceModel workspaceModel;
   private OverviewModel overviewModel;
   private SearchResultTableModel searchResults;
   private AdministrationModel administrationModel;
   private TasksModel tasksModel;
   public CommandQueryClient tasksClient;
   private CommandQueryClient contactClient;
   private ContactValue contact;

   public AccountSettingsValue settings()
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      Account acc = uow.get(account);
      try
      {
         return acc.accountSettings();
      } finally
      {
         uow.discard();
      }
   }

   public void updateSettings(AccountSettingsValue value)
         throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      uow.get(account).updateSettings(value);
      uow.complete();
      setChanged();
      notifyObservers();
   }

   public String test() throws IOException, ResourceException
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      try
      {
         return uow.get(account).version(client);
      } finally
      {
         uow.discard();
      }
   }

   private CommandQueryClient contactResource()
   {
      return this.userResource().getSubClient("contact");
   }

   public CommandQueryClient userResource()
   {
      UnitOfWork uow = uowf.newUnitOfWork();

      try
      {
         return uow.get(account).user(client);
      } finally
      {
         uow.discard();
      }
   }

   public CommandQueryClient serverResource()
   {
      UnitOfWork uow = uowf.newUnitOfWork();

      try
      {
         return uow.get(account).server(client);
      } finally
      {
         uow.discard();
      }
   }

   public TreeValue organizations() throws ResourceException
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      Account acc = uow.get(account);
      try
      {
         return acc.user(client).getSubClient("administration").query(
               "organizations", TreeValue.class);

      } finally
      {
         uow.discard();
      }
   }

   public void remove() throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      Account acc = uow.get(account);
      individualRepository.individual().removeAccount(acc);
      uow.complete();
   }

   public void changePassword(ChangePasswordCommand changePasswordCommand)
         throws Exception
   {
      UnitOfWork uow = uowf.newUnitOfWork();

      try
      {
         Account account1 = uow.get(account);
         account1.changePassword(client, changePasswordCommand);
         uow.complete();
      } catch (Exception ex)
      {
         uow.discard();
         throw ex;
      }
   }

   // Contact Details
   public ContactValue getContact()
   {
      try
      {
         CommandQueryClient client = userResource();
         this.contact = (ContactValue) client.getSubClient("contact").query(
               "index", ContactValue.class).buildWith().prototype();
         return contact;
      } catch (Exception e)
      {
         throw new OperationException(TaskResources.could_not_refresh, e);
      }
   }

   public ContactPhoneValue getPhoneNumber()
   {
      if (contact.phoneNumbers().get().isEmpty())
      {
         ContactPhoneValue phone = vbf.newValue(ContactPhoneValue.class)
               .<ContactPhoneValue> buildWith().prototype();
         contact.phoneNumbers().get().add(phone);
      }
      return contact.phoneNumbers().get().get(0);
   }

   public ContactEmailValue getEmailAddress()
   {
      if (contact.emailAddresses().get().isEmpty())
      {
         ContactEmailValue email = vbf.newValue(ContactEmailValue.class)
               .<ContactEmailValue> buildWith().prototype();
         contact.emailAddresses().get().add(email);
      }
      return contact.emailAddresses().get().get(0);
   }

   public void changeName(String newName) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder(StringValue.class);
      builder.prototype().string().set(newName);
      contactClient = contactResource();
      contactClient.putCommand("changename", builder.newInstance());
   }

   public void changePhoneNumber(String newPhoneNumber)
         throws ResourceException
   {
      ValueBuilder<ContactPhoneValue> builder = vbf
            .newValueBuilder(ContactPhoneValue.class);
      builder.prototype().phoneNumber().set(newPhoneNumber);
      contactClient = contactResource();
      contactClient.putCommand("changephonenumber", builder.newInstance());
   }

   public void changeEmailAddress(String newEmailAddress)
         throws ResourceException
   {
      ValueBuilder<ContactEmailValue> builder = vbf
            .newValueBuilder(ContactEmailValue.class);
      builder.prototype().emailAddress().set(newEmailAddress);
      contactClient = contactResource();
      contactClient.putCommand("changeemailaddress", builder.newInstance());
   }

   public TasksModel tasks()
   {
      if (tasksModel == null)
      {
         tasksClient = serverResource().getSubClient("tasks");
         tasksModel = obf.newObjectBuilder(TasksModel.class).use(this,
               tasksClient).newInstance();
      }

      return tasksModel;
   }

   public WorkspaceModel workspace()
   {
      if (workspaceModel == null)
      {
         CommandQueryClient resource = userResource();
         CommandQueryClient userInboxClient = resource
               .getSubClient("workspace").getSubClient("user").getSubClient(
                     "inbox");
         TaskTableModel inboxModel = obf.newObjectBuilder(TaskTableModel.class)
               .use(userInboxClient).newInstance();
         WorkspaceUserInboxNode userInboxNode = obf.newObjectBuilder(
               WorkspaceUserInboxNode.class).use(inboxModel, userInboxClient)
               .newInstance();

         CommandQueryClient userAssignmentsClient = resource.getSubClient(
               "workspace").getSubClient("user").getSubClient("assignments");
         TaskTableModel assignmentsModel = obf.newObjectBuilder(
               TaskTableModel.class).use(userAssignmentsClient).newInstance();
         WorkspaceUserAssignmentsNode userAssignmentsNode = obf
               .newObjectBuilder(WorkspaceUserAssignmentsNode.class).use(
                     assignmentsModel, userAssignmentsClient).newInstance();

         CommandQueryClient userDelegationsClient = resource.getSubClient(
               "workspace").getSubClient("user").getSubClient("delegations");
         TaskTableModel delegationsModel = obf.newObjectBuilder(
               TaskTableModel.class).use(userDelegationsClient).newInstance();
         WorkspaceUserDelegationsNode userDelegationsNode = obf
               .newObjectBuilder(WorkspaceUserDelegationsNode.class).use(
                     delegationsModel, userDelegationsClient).newInstance();

         CommandQueryClient userWaitingForClient = resource.getSubClient(
               "workspace").getSubClient("user").getSubClient("waitingfor");
         TaskTableModel waitingForModel = obf.newObjectBuilder(
               TaskTableModel.class).use(userWaitingForClient).newInstance();
         WorkspaceUserWaitingForNode userWaitingForNode = obf.newObjectBuilder(
               WorkspaceUserWaitingForNode.class).use(waitingForModel,
               userWaitingForClient).newInstance();

         workspaceModel = obf.newObjectBuilder(WorkspaceModel.class).use(this,
               resource, userInboxNode, userAssignmentsNode,
               userDelegationsNode, userWaitingForNode, tasks()).newInstance();
      }

      return workspaceModel;
   }

   public OverviewModel overview()
   {
      if (overviewModel == null)
      {
         CommandQueryClient client = userResource().getSubClient("overview")
               .getSubClient("projects");
         OverviewProjectsNode overviewProjects = obf.newObjectBuilder(
               OverviewProjectsNode.class).use(client, this).newInstance();

         OverviewSummaryModel summaryModel = obf.newObjectBuilder(
               OverviewSummaryModel.class).use(
               userResource().getSubClient("overview")).newInstance();

         overviewModel = obf.newObjectBuilder(OverviewModel.class).use(this,
               tasks(), overviewProjects, summaryModel).newInstance();
      }

      return overviewModel;
   }

   public SearchResultTableModel search()
   {
      if (searchResults == null)
      {
         searchResults = obf.newObjectBuilder(SearchResultTableModel.class)
               .use(tasks(), tasksClient).newInstance();
      }

      return searchResults;
   }

   public AdministrationModel administration()
   {
      if (administrationModel == null)
      {
         administrationModel = obf.newObjectBuilder(AdministrationModel.class)
               .use(this, tasks()).newInstance();
      }

      return administrationModel;
   }

   public void notifyEvent(DomainEvent event)
   {
      if (workspaceModel != null)
         workspaceModel.notifyEvent(event);

      if (overviewModel != null)
         overviewModel.notifyEvent(event);

      if (searchResults != null)
         searchResults.notifyEvent(event);

      if (administrationModel != null)
         administrationModel.notifyEvent(event);

      if (tasksModel != null)
         tasksModel.notifyEvent(event);
   }

}
