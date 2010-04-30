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

package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.FilteredList;
import se.streamsource.streamflow.client.infrastructure.ui.GroupedFilteredList;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * JAVADOC
 */
public class SelectLinkDialog
      extends JPanel
{
   public LinkValue selected;
   public JList list;

   public SelectLinkDialog( final @Uses EventList<TitledLinkValue> links,
                                      @Service ApplicationContext context,
                                      @Structure ObjectBuilderFactory obf )
   {
      super( );

      setName( i18n.text( WorkspaceResources.search_projects_users ) );
      setActionMap( context.getActionMap( this ) );

      GroupedFilteredList list = new GroupedFilteredList();
      list.getList().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      list.setEventList( links );
      this.list = list.getList();

      add( new JScrollPane( list ));
   }

   public SelectLinkDialog( final @Service ApplicationContext context,
                                     @Uses EventList<LinkValue> links,
                                      @Structure ObjectBuilderFactory obf )
   {
      super( );

      setName( i18n.text( WorkspaceResources.search_projects_users ) );
      setActionMap( context.getActionMap( this ) );

      FilteredList list = new FilteredList();
      list.getList().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      list.setEventList( links );
      this.list = list.getList();

      add( new JScrollPane( list ));
   }

   public LinkValue getSelected()
   {
      return selected;
   }

   @Action
   public void execute()
   {
      selected = (LinkValue) list.getSelectedValue();

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}