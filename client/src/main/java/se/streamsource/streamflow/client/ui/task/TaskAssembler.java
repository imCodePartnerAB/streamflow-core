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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.client.ui.task.conversations.AllParticipantsModel;
import se.streamsource.streamflow.client.ui.task.conversations.AllParticipantsView;
import se.streamsource.streamflow.client.ui.task.conversations.MessagesModel;
import se.streamsource.streamflow.client.ui.task.conversations.MessagesView;
import se.streamsource.streamflow.client.ui.task.conversations.TaskConversationModel;
import se.streamsource.streamflow.client.ui.task.conversations.TaskConversationParticipantsModel;
import se.streamsource.streamflow.client.ui.task.conversations.TaskConversationParticipantsView;
import se.streamsource.streamflow.client.ui.task.conversations.TaskConversationView;
import se.streamsource.streamflow.client.ui.task.conversations.TaskConversationsModel;
import se.streamsource.streamflow.client.ui.task.conversations.TaskConversationsView;

/**
 * JAVADOC
 */
public class TaskAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      UIAssemblers.addViews( module, TasksView.class, TasksDetailView2.class, TableSelectionView.class, TaskContactsAdminView.class,
            TaskFormsAdminView.class, TaskSubmittedFormsAdminView.class );

      UIAssemblers.addDialogs( module, AddCommentDialog.class, TaskLabelsDialog.class );

      UIAssemblers.addMV( module, TaskTableModel.class, TaskTableView.class );

      UIAssemblers.addMV( module, TaskInfoModel.class, TaskInfoView.class );

      UIAssemblers.addModels( module, TasksModel.class, TaskFormsModel.class );

      UIAssemblers.addMV( module,
            TaskModel.class,
            TaskDetailView.class );

      UIAssemblers.addMV( module,
            TaskCommentsModel.class,
            TaskCommentsView.class );
      
      UIAssemblers.addMV( module,
            TaskContactsModel.class,
            TaskContactsView.class );

      UIAssemblers.addMV( module,
            TaskContactModel.class,
            TaskContactView.class );

      UIAssemblers.addMV( module,
            TaskGeneralModel.class,
            TaskGeneralView.class );

      UIAssemblers.addMV( module,
            TaskLabelsModel.class,
            TaskLabelsView.class );

      UIAssemblers.addMV( module,
            TaskLabelSelectionModel.class,
            TaskLabelSelectionView.class );

      UIAssemblers.addMV( module,
            PossibleTaskTypesModel.class,
            TaskTypesDialog.class );

      UIAssemblers.addMV( module,
            TaskEffectiveFieldsValueModel.class,
            TaskEffectiveFieldsValueView.class );

      UIAssemblers.addMV( module,
            TaskSubmittedFormsModel.class,
            TaskSubmittedFormsView.class );

      UIAssemblers.addMV( module,
            TaskSubmittedFormModel.class,
            TaskSubmittedFormView.class );

      UIAssemblers.addMV( module,
            FormSubmissionModel.class,
            FormSubmissionWizardPage.class );

      UIAssemblers.addMV( module,
              PossibleFormsModel.class,
              PossibleFormsView.class );

      UIAssemblers.addMV( module, 
    		  TaskActionsModel.class, 
    		  TaskActionsView.class );

      // conversations
      UIAssemblers.addMV( module,
            AllParticipantsModel.class,
            AllParticipantsView.class );

      UIAssemblers.addMV( module,
            MessagesModel.class,
            MessagesView.class );

      UIAssemblers.addMV( module,
            TaskConversationModel.class,
            TaskConversationView.class );

      UIAssemblers.addMV( module,
            TaskConversationsModel.class,
            TaskConversationsView.class );

      UIAssemblers.addMV( module,
            TaskConversationParticipantsModel.class,
            TaskConversationParticipantsView.class );

   }
}
