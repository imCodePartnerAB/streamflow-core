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

package se.streamsource.streamflow.client.ui.overview;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.ui.ContextItemGroupComparator;
import se.streamsource.streamflow.client.ui.ContextItemListRenderer;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableFormatter;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SeparatorContextItemListCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;

/**
 * JAVADOC
 */
public class OverviewView
        extends JPanel
{
   private JList overviewList;
   private JSplitPane pane;
   private CasesView casesView;
   private OverviewModel model;
   private final CommandQueryClient client;

   public OverviewView(final @Service ApplicationContext context,
                       final @Uses CommandQueryClient client,
                       final @Structure ObjectBuilderFactory obf)
   {
      super(new BorderLayout());
      this.client = client;

      overviewList = new JList();
      casesView = obf.newObjectBuilder(CasesView.class).use(client).newInstance();
      casesView.setBlankPanel(new JPanel());

      model = obf.newObjectBuilder(OverviewModel.class).use(client).newInstance();

      JTextField filterField = new JTextField();
      SortedList<ContextItem> sortedIssues = new SortedList<ContextItem>(model.getItems(), new Comparator<ContextItem>()
      {
         public int compare(ContextItem o1, ContextItem o2)
         {
            return o1.getGroup().compareTo(o2.getGroup());
         }
      });
      final FilterList<ContextItem> textFilteredIssues = new FilterList<ContextItem>(sortedIssues, new TextComponentMatcherEditor<ContextItem>(filterField, new TextFilterator<ContextItem>()
      {
         public void getFilterStrings(List<String> strings, ContextItem contextItem)
         {
            strings.add(contextItem.getGroup());
         }
      }));

      Comparator<ContextItem> comparator = new ContextItemGroupComparator();
      EventList<ContextItem> separatorList = new SeparatorList<ContextItem>(textFilteredIssues, comparator, 1, 10000);
      overviewList.setModel(new EventListModel<ContextItem>(separatorList));
      overviewList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      overviewList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select");
      overviewList.getActionMap().put("select", new AbstractAction()
      {
         public void actionPerformed(ActionEvent e)
         {
            pane.getRightComponent().requestFocus();
         }
      });

      overviewList.setCellRenderer(new SeparatorContextItemListCellRenderer(new ContextItemListRenderer()));

      JScrollPane workspaceScroll = new JScrollPane(overviewList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      pane.setDividerLocation(200);
      pane.setResizeWeight(0);

      JPanel overviewOutline = new JPanel(new BorderLayout());
      overviewOutline.add(workspaceScroll, BorderLayout.CENTER);
      overviewOutline.setMinimumSize(new Dimension(150, 300));

      pane.setLeftComponent(overviewOutline);
      pane.setRightComponent(casesView);

      add(pane, BorderLayout.CENTER);

      overviewList.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (!e.getValueIsAdjusting())
            {
               JList list = (JList) e.getSource();

               if (list.getSelectedValue() == null)
                  return;

               if (list.getSelectedValue() instanceof ContextItem)
               {
                  ContextItem contextItem = (ContextItem) list.getSelectedValue();
                  TableFormat tableFormat;
                  /*if (contextItem.getRelation().equals(Icons.assign.name()))
                  {
                     tableFormat = new OverviewAssignmentsCaseTableFormatter();
                  } else
                  {*/
                  tableFormat = new CasesTableFormatter();
                  //}
                  CasesTableView casesTable = obf.newObjectBuilder(CasesTableView.class).use(contextItem.getClient(), tableFormat).newInstance();

                  casesView.showTable(casesTable);
               } else
               {
                  // TODO Overview of all projects
/*
                  final OverviewSummaryModel overviewSummaryModel = model.summary();

                  view = obf.newObjectBuilder( OverviewSummaryView.class ).use( overviewSummaryModel ).newInstance();
                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        overviewSummaryModel.refresh();
                        return null;
                     }
                  } );
*/
               }
            }
         }
      });

      new RefreshWhenShowing(this, model);
   }

}