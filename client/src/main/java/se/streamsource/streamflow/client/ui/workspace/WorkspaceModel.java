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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;

/**
 * JAVADOC
 */
public class WorkspaceModel
        extends DefaultTreeTableModel
{
    @Structure
    ObjectBuilderFactory obf;

    Account account;

    public WorkspaceModel(@Service IndividualRepository individualRepository,
                       final @Structure ObjectBuilderFactory obf)
    {
        super();

        individualRepository.individual().visitAccounts(new AccountVisitor()
        {
            public void visitAccount(Account acc)
            {
                account = acc;
            }
        });

        if (account != null)
            setRoot(obf.newObjectBuilder(WorkspaceNode.class).use(account).newInstance());
    }

    @Override
    public int getColumnCount()
    {
        return 1;
    }

    @Override
    public Class<?> getColumnClass(int column)
    {
        switch (column)
        {
            case 0:
                return String.class;
        }
        return super.getColumnClass(column);
    }

    @Override
    public boolean isCellEditable(Object o, int i)
    {
        return false;
    }


    public void refresh()
    {
        setRoot(obf.newObjectBuilder(WorkspaceNode.class).use(account).newInstance());
    }

    
}
