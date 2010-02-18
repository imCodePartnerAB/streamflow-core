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

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class WorkspaceNode
      extends DefaultMutableTreeNode
      implements EventListener
{
   private WorkspaceUserNode userNode;
   private WorkspaceProjectsNode projectsNode;

   public WorkspaceNode( @Uses WorkspaceUserNode userNode,
                         @Uses WorkspaceProjectsNode projectsNode,
                         @Uses AccountModel accountModel )
   {
      super( accountModel );
      this.userNode = userNode;
      this.projectsNode = projectsNode;

      add( userNode );
      add( projectsNode );
   }

   @Override
   public AccountModel getUserObject()
   {
      return (AccountModel) super.getUserObject();
   }

   public WorkspaceUserNode getUserNode()
   {
      return userNode;
   }

   public WorkspaceProjectsNode getProjectsNode()
   {
      return projectsNode;
   }

   public void notifyEvent( DomainEvent event )
   {
      userNode.notifyEvent( event );
      projectsNode.notifyEvent( event );
   }

   @Override
   public String toString()
   {
      return getUserObject().settings().name().get();
   }
}
