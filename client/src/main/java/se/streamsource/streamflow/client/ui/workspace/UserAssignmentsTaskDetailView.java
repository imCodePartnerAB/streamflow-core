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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.*;

/**
 * JAVADOC
 */
public class UserAssignmentsTaskDetailView
        extends JTabbedPane
{
    public UserAssignmentsTaskDetailView(@Service ApplicationContext appContext,
                                     @Service TaskCommentsModel commentsModel,
                                     @Service TaskGeneralModel generalModel,
                                     @Structure ObjectBuilderFactory obf)
    {
        TaskCommentsView commentsView = obf.newObjectBuilder(TaskCommentsView.class).use(commentsModel).newInstance();
        TaskGeneralView generalView = obf.newObjectBuilder(TaskGeneralView.class).use(generalModel).newInstance();

        addTab(i18n.text(WorkspaceResources.general_tab), generalView);
        addTab("Metadata", new JLabel("TODO"));
        addTab(i18n.text(WorkspaceResources.comments_tab), commentsView);
        addTab("Attachments", new JLabel("TODO"));
    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);
        getSelectedComponent().setVisible(aFlag);
    }
}