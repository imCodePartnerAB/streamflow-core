/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MemberClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.roles.MemberRoleClientResource;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * JAVADOC
 */
public class MemberRolesModel
        extends AbstractTableModel
{
    @Uses
    OrganizationalUnitAdministrationModel organizationModel;
    @Uses
    MemberClientResource member;

    ListValue memberRoles;
    List<ListItemValue> allRoles;

    String[] columnNames = {" ", "Role Name"};
    Class[] columnClasses = {Boolean.class, String.class};
    boolean[] columnEditable = {true, false};


    @Override
    public void setValueAt(Object value, int row, int column)
    {
        ListItemValue role = allRoles.get(row);
        try
        {
            MemberRoleClientResource memberRole = member.roles().role(role.entity().get().identity());
            if ((Boolean) value)
            {
                memberRole.put(null);
                memberRoles.items().get().add(role);
            } else
            {
                memberRole.delete();
                memberRoles.items().get().remove(role);
            }
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }

    public Object getValueAt(int row, int column)
    {
        switch (column)
        {
            case 0:
                return memberRoles.items().get().contains(allRoles.get(row));
            case 1:
                return allRoles.get(row).description().get();
        }
        return null;
    }

    public int getRowCount()
    {
        return allRoles == null ? 0 : allRoles.size();
    }

    public int getColumnCount()
    {
        return 2;
    }

    public Class<?> getColumnClass(int column)
    {
        return columnClasses[column];
    }

    public boolean isCellEditable(int row, int column)
    {
        return columnEditable[column];
    }

    public String getColumnName(int column)
    {
        return columnNames[column];
    }

    public void refresh()
    {
        try
        {
            allRoles = organizationModel.getOrganization().roles().roles().<ListValue>buildWith().prototype().items().get();
            memberRoles = member.roles().roles().<ListValue>buildWith().prototype();
            fireTableDataChanged();
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }
}