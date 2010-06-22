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

package se.streamsource.streamflow.client.assembler;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.restlet.Restlet;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ExceptionHandlerService;
import se.streamsource.streamflow.client.infrastructure.ui.JavaHelp;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.ApplicationInitializationService;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.CreateProxyUserDialog;
import se.streamsource.streamflow.client.ui.CreateUserDialog;
import se.streamsource.streamflow.client.ui.DebugWindow;
import se.streamsource.streamflow.client.ui.DummyDataService;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.ResetPasswordDialog;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.administration.AccountAdministrationNode;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.administration.AccountView;
import se.streamsource.streamflow.client.ui.administration.AdministrationModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationNode;
import se.streamsource.streamflow.client.ui.administration.AdministrationOutlineView;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.ui.administration.AdministrationWindow;
import se.streamsource.streamflow.client.ui.administration.ChangePasswordDialog;
import se.streamsource.streamflow.client.ui.administration.LinksQueryListModel;
import se.streamsource.streamflow.client.ui.administration.OrganizationAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.OrganizationAdministrationNode;
import se.streamsource.streamflow.client.ui.administration.OrganizationAdministrationView;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationNode;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationView;
import se.streamsource.streamflow.client.ui.administration.ProfileDialog;
import se.streamsource.streamflow.client.ui.administration.SelectLinksDialog;
import se.streamsource.streamflow.client.ui.administration.SelectOrganizationOrOrganizationalUnitDialog;
import se.streamsource.streamflow.client.ui.administration.SelectOrganizationalUnitDialog;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypeModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypeView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesAdminView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesView;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectCaseTypesDialog;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldCreationDialog;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldValueCommentEditView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldValueDateEditView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldValueEditModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldValueNumberEditView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldValueObserver;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldValueSelectionEditView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldValueTextEditView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FieldsView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormEditAdminView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.PageEditModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.PageEditView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.SelectionElementsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.SelectionElementsView;
import se.streamsource.streamflow.client.ui.administration.form.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.form.SelectedFormsView;
import se.streamsource.streamflow.client.ui.administration.groups.GroupAdminView;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsView;
import se.streamsource.streamflow.client.ui.administration.groups.ParticipantsModel;
import se.streamsource.streamflow.client.ui.administration.groups.ParticipantsView;
import se.streamsource.streamflow.client.ui.administration.label.GroupedSelectionDialog;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.LabelsView;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.label.SelectionDialog;
import se.streamsource.streamflow.client.ui.administration.organization.LinksListModel;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationUsersView;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsAdminView;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsModel;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsTabbedView;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsView;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectAdminView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectMembersModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectMembersView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsView;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesView;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointModel;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointView;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsAdminView;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsModel;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsView;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersModel;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersView;
import se.streamsource.streamflow.client.ui.administration.surface.SurfaceAdministrationView;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationView;
import se.streamsource.streamflow.client.ui.caze.AddCommentDialog;
import se.streamsource.streamflow.client.ui.caze.CaseActionsModel;
import se.streamsource.streamflow.client.ui.caze.CaseActionsView;
import se.streamsource.streamflow.client.ui.caze.CaseDetailView;
import se.streamsource.streamflow.client.ui.caze.CaseEffectiveFieldsValueModel;
import se.streamsource.streamflow.client.ui.caze.CaseEffectiveFieldsValueView;
import se.streamsource.streamflow.client.ui.caze.CaseFormsModel;
import se.streamsource.streamflow.client.ui.caze.CaseGeneralModel;
import se.streamsource.streamflow.client.ui.caze.CaseGeneralView;
import se.streamsource.streamflow.client.ui.caze.CaseInfoModel;
import se.streamsource.streamflow.client.ui.caze.CaseInfoView;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsDialog;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsModel;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsView;
import se.streamsource.streamflow.client.ui.caze.CaseModel;
import se.streamsource.streamflow.client.ui.caze.CaseSubmittedFormModel;
import se.streamsource.streamflow.client.ui.caze.CaseSubmittedFormView;
import se.streamsource.streamflow.client.ui.caze.CaseSubmittedFormsModel;
import se.streamsource.streamflow.client.ui.caze.CaseSubmittedFormsView;
import se.streamsource.streamflow.client.ui.caze.CaseTableView;
import se.streamsource.streamflow.client.ui.caze.CaseTypesDialog;
import se.streamsource.streamflow.client.ui.caze.CasesDetailView2;
import se.streamsource.streamflow.client.ui.caze.CasesModel;
import se.streamsource.streamflow.client.ui.caze.CasesTableModel;
import se.streamsource.streamflow.client.ui.caze.CasesView;
import se.streamsource.streamflow.client.ui.caze.ContactModel;
import se.streamsource.streamflow.client.ui.caze.ContactView;
import se.streamsource.streamflow.client.ui.caze.ContactsAdminView;
import se.streamsource.streamflow.client.ui.caze.ContactsModel;
import se.streamsource.streamflow.client.ui.caze.ContactsView;
import se.streamsource.streamflow.client.ui.caze.FormSubmissionModel;
import se.streamsource.streamflow.client.ui.caze.FormSubmissionWizardPage;
import se.streamsource.streamflow.client.ui.caze.FormsAdminView;
import se.streamsource.streamflow.client.ui.caze.PossibleCaseTypesModel;
import se.streamsource.streamflow.client.ui.caze.PossibleFormsModel;
import se.streamsource.streamflow.client.ui.caze.PossibleFormsView;
import se.streamsource.streamflow.client.ui.caze.SubmittedFormsAdminView;
import se.streamsource.streamflow.client.ui.caze.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.ui.caze.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationParticipantsModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationParticipantsView;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationView;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationsModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.caze.conversations.MessagesModel;
import se.streamsource.streamflow.client.ui.caze.conversations.MessagesView;
import se.streamsource.streamflow.client.ui.menu.AccountMenu;
import se.streamsource.streamflow.client.ui.menu.AccountsDialog;
import se.streamsource.streamflow.client.ui.menu.AccountsModel;
import se.streamsource.streamflow.client.ui.menu.AdministrationMenuBar;
import se.streamsource.streamflow.client.ui.menu.CreateAccountDialog;
import se.streamsource.streamflow.client.ui.menu.EditMenu;
import se.streamsource.streamflow.client.ui.menu.FileMenu;
import se.streamsource.streamflow.client.ui.menu.HelpMenu;
import se.streamsource.streamflow.client.ui.menu.OverviewMenuBar;
import se.streamsource.streamflow.client.ui.menu.ViewMenu;
import se.streamsource.streamflow.client.ui.menu.WindowMenu;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.overview.OverviewNode;
import se.streamsource.streamflow.client.ui.overview.OverviewProjectAssignmentsNode;
import se.streamsource.streamflow.client.ui.overview.OverviewProjectNode;
import se.streamsource.streamflow.client.ui.overview.OverviewProjectsNode;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryModel;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryView;
import se.streamsource.streamflow.client.ui.overview.OverviewView;
import se.streamsource.streamflow.client.ui.overview.OverviewWindow;
import se.streamsource.streamflow.client.ui.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.AccountSelectionView;
import se.streamsource.streamflow.client.ui.workspace.FilterListDialog;
import se.streamsource.streamflow.client.ui.workspace.GroupedFilterListDialog;
import se.streamsource.streamflow.client.ui.workspace.RefreshCaseCountTask;
import se.streamsource.streamflow.client.ui.workspace.SavedSearchesModel;
import se.streamsource.streamflow.client.ui.workspace.SavedSearchesView;
import se.streamsource.streamflow.client.ui.workspace.SelectLinkDialog;
import se.streamsource.streamflow.client.ui.workspace.TestConnectionTask;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceProjectAssignmentsNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceProjectInboxNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceProjectNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceProjectsNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceUserDraftsNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceUserNode;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceView;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceWindow;

import static org.qi4j.api.common.Visibility.*;
import static se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers.*;
import static se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers.addDialogs;

/**
 * JAVADOC
 */
public class UIAssembler
{
   public void assemble( LayerAssembly layer ) throws AssemblyException
   {
      search( layer.moduleAssembly( "Search" ) );
      administration( layer.moduleAssembly( "Administration" ) );
      workspace(layer.moduleAssembly( "Workspace" ));
      caze(layer.moduleAssembly( "Case" ));
      menu(layer.moduleAssembly("Menu"));
      overview(layer.moduleAssembly( "Overview" ));
      streamflow(layer.moduleAssembly( "Streamflow" ));
      restlet(layer.moduleAssembly( "Restlet client" ));

      // More specific administration modules
      labels(layer.moduleAssembly( "Labels" ));
      userAdministration( layer.moduleAssembly( "Users" ) );
      organizationAdministration(layer.moduleAssembly( "Organizations" ) );
      groupAdministration(layer.moduleAssembly( "Groups" ) );
      projectAdministration(layer.moduleAssembly( "Projects" ) );
      caseTypeAdministration(layer.moduleAssembly( "Case types" ) );
      resolutions(layer.moduleAssembly( "Resolutions" ) );
      roleAdministration(layer.moduleAssembly( "Roles" ) );
      forms(layer.moduleAssembly( "Forms" ) );
      administratorAdministrator(layer.moduleAssembly( "Administrators" ) );
      surfaceAdministration(layer.moduleAssembly( "Surface" ) );
   }

   private void surfaceAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, SurfaceAdministrationView.class,
            AccessPointsAdminView.class);

      addMV( module,
            AccessPointsModel.class,
            AccessPointsView.class);

      addMV( module,
            AccessPointModel.class,
            AccessPointView.class);

      addMV( module,
            ProxyUsersModel.class,
            ProxyUsersView.class);
   }

   private void administratorAdministrator( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, AdministratorsModel.class,
            AdministratorsView.class );
   }

   private void forms( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, SelectedFormsModel.class, SelectedFormsView.class );
   }

   private void roleAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, RolesModel.class,
            RolesView.class );
   }

   private void resolutions( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, ResolutionsModel.class, ResolutionsView.class );
      addMV( module, SelectedResolutionsModel.class, SelectedResolutionsView.class );
   }

   private void caseTypeAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, CaseTypesAdminView.class );

      addDialogs( module, SelectCaseTypesDialog.class );

      addMV( module, SelectedCaseTypesModel.class, SelectedCaseTypesView.class );

      addMV( module, FormsModel.class, FormsView.class );

      addMV( module, FormModel.class, FormView.class );

      addMV( module, CaseTypesModel.class,
            CaseTypesView.class );

      addMV( module, CaseTypeModel.class, CaseTypeView.class );

   }

   private void projectAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, ProjectAdminView.class );


      addMV( module, ProjectsModel.class,
            ProjectsView.class );

      addModels( module, ProjectModel.class );

      addViews( module, ProjectView.class, se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsAdminView.class, FormEditAdminView.class );

      addMV( module, ProjectMembersModel.class,
            ProjectMembersView.class );

      addMV( module,
            FieldsModel.class, FieldsView.class );

      addMV( module,
            FieldValueEditModel.class, FieldValueTextEditView.class);

      addMV( module,
            SelectionElementsModel.class, SelectionElementsView.class);

      addMV( module,
            PageEditModel.class, PageEditView.class);

      addViews( module,
            FieldValueDateEditView.class,
            FieldValueNumberEditView.class,
            FieldValueSelectionEditView.class,
            FieldValueCommentEditView.class);

      addDialogs( module, FieldCreationDialog.class );

      module.addObjects( FieldValueObserver.class );

   }

   private void groupAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, GroupAdminView.class );

      addMV( module, GroupsModel.class,
            GroupsView.class );

      addMV( module, ParticipantsModel.class,
            ParticipantsView.class );

   }

   private void organizationAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, OrganizationsTabbedView.class,
            OrganizationsAdminView.class );

      addMV( module, OrganizationsModel.class,
            OrganizationsView.class );
      addMV( module, LinksListModel.class,
            OrganizationUsersView.class );
   }

   private void userAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, UsersAdministrationModel.class,
            UsersAdministrationView.class );
   }

   private void labels( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, LabelsModel.class, LabelsView.class );

      addMV( module, SelectedLabelsModel.class, SelectedLabelsView.class );
   }

   private void restlet( ModuleAssembly module ) throws AssemblyException
   {
      module.importServices( Restlet.class ).visibleIn( application );
   }

   private void streamflow( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects(
            StreamflowApplication.class,
            AccountSelector.class
      );

      // SAF objects
      module.importServices( StreamflowApplication.class, ApplicationContext.class, AccountSelector.class ).visibleIn( layer );


      module.addServices( DummyDataService.class ).instantiateOnStartup();
      module.addServices( ApplicationInitializationService.class ).instantiateOnStartup();

      addDialogs( module, NameDialog.class,
            SelectUsersAndGroupsDialog.class,
            CreateUserDialog.class,
            CreateProxyUserDialog.class,
            ConfirmationDialog.class,
            ResetPasswordDialog.class );

      addModels( module,
            UsersAndGroupsModel.class );

      module.addObjects( DebugWindow.class );

      module.addObjects(
            DialogService.class,
            UncaughtExceptionHandler.class,
            JavaHelp.class
      ).visibleIn( layer );

      module.importServices( UncaughtExceptionHandler.class,
            JavaHelp.class ).importedBy( NewObjectImporter.class ).visibleIn( application );
      module.addServices(
            ExceptionHandlerService.class ).instantiateOnStartup();
      module.importServices( DialogService.class ).importedBy( NewObjectImporter.class ).visibleIn( application );
   }

   private void overview( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( OverviewWindow.class ).visibleIn( layer );

      module.addObjects(
            OverviewNode.class,
            OverviewProjectsNode.class,
            OverviewProjectNode.class,
            OverviewProjectAssignmentsNode.class).visibleIn( layer );

      addMV( module,
            OverviewModel.class,
            OverviewView.class );

      addMV( module,
            OverviewSummaryModel.class,
            OverviewSummaryView.class );
   }

   private void caze( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, CasesView.class, CasesDetailView2.class, ContactsAdminView.class,
            FormsAdminView.class, SubmittedFormsAdminView.class );

      addDialogs( module, AddCommentDialog.class, CaseLabelsDialog.class );

      addMV( module, CasesTableModel.class, CaseTableView.class );

      addMV( module, CaseInfoModel.class, CaseInfoView.class );

      addModels( module, CasesModel.class, CaseFormsModel.class );

      addMV( module,
            CaseModel.class,
            CaseDetailView.class );

      addMV( module,
            ContactsModel.class,
            ContactsView.class );

      addMV( module,
            ContactModel.class,
            ContactView.class );

      addMV( module,
            CaseGeneralModel.class,
            CaseGeneralView.class );

      addMV( module,
            CaseLabelsModel.class,
            CaseLabelsView.class );

      addMV( module,
            PossibleCaseTypesModel.class,
            CaseTypesDialog.class );

      addMV( module,
            CaseEffectiveFieldsValueModel.class,
            CaseEffectiveFieldsValueView.class );

      addMV( module,
            CaseSubmittedFormsModel.class,
            CaseSubmittedFormsView.class );

      addMV( module,
            CaseSubmittedFormModel.class,
            CaseSubmittedFormView.class );

      addMV( module,
            FormSubmissionModel.class,
            FormSubmissionWizardPage.class );

      addMV( module,
              PossibleFormsModel.class,
              PossibleFormsView.class );

      addMV( module,
    		  CaseActionsModel.class,
    		  CaseActionsView.class );

      // conversations
      addMV( module,
            MessagesModel.class,
            MessagesView.class );

      addMV( module,
            ConversationModel.class,
            ConversationView.class );

      addMV( module,
            ConversationsModel.class,
            ConversationsView.class );

      addMV( module,
            ConversationParticipantsModel.class,
            ConversationParticipantsView.class );

      // Attachments
      addMV( module,
            AttachmentsModel.class,
            AttachmentsView.class );
   }

   private void workspace( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, AccountSelectionView.class );
      module.addObjects( WorkspaceWindow.class ).visibleIn( layer );

      module.addObjects(
            WorkspaceNode.class,
            WorkspaceUserNode.class,
            WorkspaceUserDraftsNode.class,
            WorkspaceProjectsNode.class,
            WorkspaceProjectNode.class,
            WorkspaceProjectInboxNode.class,
            WorkspaceProjectAssignmentsNode.class,
            RefreshCaseCountTask.class).visibleIn( layer );

      addMV( module,
            WorkspaceModel.class,
            WorkspaceView.class );

      addMV( module, SavedSearchesModel.class, SavedSearchesView.class );

      addDialogs( module, SelectLinkDialog.class, FilterListDialog.class, GroupedFilterListDialog.class);
   }

   private void menu( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module,
            WorkspaceMenuBar.class,
            OverviewMenuBar.class,
            AdministrationMenuBar.class );
      addViews( module,
            FileMenu.class,
            EditMenu.class,
            ViewMenu.class,
            AccountMenu.class,
            WindowMenu.class,
            HelpMenu.class
      );

      addDialogs( module, CreateAccountDialog.class, AccountsDialog.class );

      addModels( module, AccountsModel.class );
   }

   private void administration( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( AdministrationWindow.class ).visibleIn( layer );

      module.addObjects( AdministrationNode.class,
            AccountAdministrationNode.class,
            OrganizationAdministrationNode.class,
            OrganizationalUnitAdministrationNode.class,
            OrganizationAdministrationModel.class );

      addViews( module,
            AdministrationView.class );
      addMV( module,
            OrganizationalUnitAdministrationModel.class,
            OrganizationalUnitAdministrationView.class );
      addMV( module,
            OrganizationAdministrationModel.class,
            OrganizationAdministrationView.class );
      addMV( module,
            AdministrationModel.class,
            AdministrationOutlineView.class );

      addViews( module, ProfileDialog.class );

      addMV( module,
            AccountModel.class,
            AccountView.class );
      addDialogs( module,
            ChangePasswordDialog.class,
            SelectOrganizationalUnitDialog.class,
            SelectOrganizationOrOrganizationalUnitDialog.class,
            SelectLinksDialog.class );
      addTasks( module, TestConnectionTask.class );

      addModels( module, LinksQueryListModel.class );

      addViews( module, GroupedSelectionDialog.class, SelectionDialog.class );
   }

   private void search( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( SearchResultTableModel.class ).visibleIn( layer );
   }
}