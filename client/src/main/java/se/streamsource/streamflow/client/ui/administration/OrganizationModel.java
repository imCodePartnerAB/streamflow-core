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
package se.streamsource.streamflow.client.ui.administration;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.external.IntegrationPointsModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.priorities.PrioritiesModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseAccessDefaultsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsModel;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointsModel;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersModel;
import se.streamsource.streamflow.client.ui.administration.templates.SelectedTemplatesModel;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationListModel;

/**
 * Represents an organization in the administration model.
 */
public class OrganizationModel
   extends ResourceModel
{
   public OrganizationModel()
   {
      relationModelMapping("administrators", AdministratorsModel.class);
      relationModelMapping("labels", LabelsModel.class);
      relationModelMapping("selectedlabels", SelectedLabelsModel.class);
      relationModelMapping("organizationusers", UsersAdministrationListModel.class);
      relationModelMapping("roles", RolesModel.class);
      relationModelMapping("forms", FormsModel.class);
      relationModelMapping("casetypes", CaseTypesModel.class);
      relationModelMapping("priorities", PrioritiesModel.class);
      relationModelMapping("accesspoints", AccessPointsModel.class);
      relationModelMapping("emailaccesspoints", EmailAccessPointsModel.class);
      relationModelMapping( "integrationpoints", IntegrationPointsModel.class );
      relationModelMapping("proxyusers", ProxyUsersModel.class);
      relationModelMapping("templates", SelectedTemplatesModel.class);
      relationModelMapping( "restrictions", CaseAccessDefaultsModel.class );
      relationModelMapping( "formondelete", FormOnRemoveModel.class );
      relationModelMapping( "groups", GroupsModel.class );
   }
}
