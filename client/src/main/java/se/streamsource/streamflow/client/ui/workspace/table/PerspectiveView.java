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
package se.streamsource.streamflow.client.ui.workspace.table;

import ca.odell.glazedlists.SortedList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.Links;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.search.SearchView;
import se.streamsource.streamflow.client.util.BottomBorder;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.StreamflowToggleButton;
import se.streamsource.streamflow.client.util.dialog.DialogService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

public class PerspectiveView extends JPanel implements Observer
{

   private static final long serialVersionUID = -149885124005347187L;

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private CasesTableModel model;

   private JDialog popup;
   private JTextField searchField;
   private JPanel optionsPanel;
   private ApplicationContext context;
   private JPanel filterPanel;
   private JPanel viewPanel;
   private JList groupByList;
   private JList sortByList;
   private JList statusList;

   private enum FilterActions
   {
      filterClear,
      filterStatus,
      filterCaseType,
      filterLabel,
      filterAssignee,
      filterProject,
      filterCreatedBy,
      filterCreatedOn,
      filterDueOn,
      viewSorting,
      viewGrouping
   }

   public void initView(final @Service ApplicationContext context,
         final @Uses CasesTableModel model, @Optional @Uses JTextField searchField)
   {
      this.context = context;

      this.model = model;
      model.addObserver( this );
      this.searchField = searchField;
      setActionMap( context.getActionMap( this ) );
      
      setFocusable(true);
      setLayout(new BorderLayout());

      filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      javax.swing.Action filterClearAction = getActionMap().get( FilterActions.filterClear.name() );
      StreamflowButton filterClearButton = new StreamflowButton( filterClearAction );
      filterPanel.add(filterClearButton);

      addPopupButton( filterPanel, FilterActions.filterCreatedOn.name() );
      List<LinkValue> linkValues = model.possibleFilterLinks();

      if( Iterables.matchesAny( Links.withRel( "possibleprojects" ), linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterProject.name() );

      if( Iterables.matchesAny( Links.withRel( "possibleassignees" ), linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterAssignee.name() );

      addPopupButton(filterPanel, FilterActions.filterCaseType.name() );
      addPopupButton(filterPanel, FilterActions.filterLabel.name() );

      if( Iterables.matchesAny( Links.withRel( "possiblecreatedby" ), linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterCreatedBy.name() );

      addPopupButton(filterPanel, FilterActions.filterDueOn.name() );

      if( Iterables.matchesAny( Links.withRel( "possiblestatus" ) , linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterStatus.name() );

      add( filterPanel, BorderLayout.WEST );

      statusList = new StatusList();

      viewPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      addPopupButton(viewPanel, FilterActions.viewSorting.name() );
      addPopupButton(viewPanel, FilterActions.viewGrouping.name() );
      add(viewPanel, BorderLayout.EAST);
      
      sortByList = new SortByList();
      groupByList = new GroupByList();

      addHierarchyListener( new HierarchyListener()
      {
         public void hierarchyChanged( HierarchyEvent e )
         {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0)
            {
               if (!PerspectiveView.this.isShowing())
               {
                  for (Component component : Iterables.flatten( Iterables.iterable( filterPanel.getComponents() ), Iterables.iterable( viewPanel.getComponents() ) ))
                  {
                     if( !(component instanceof StreamflowToggleButton) )
                        continue;
                     
                     ((StreamflowToggleButton) component).setSelected( false );
                  }
               } 
            }
         }
      } );
   }

   private void addPopupButton(JPanel panel, String action)
   {
      javax.swing.Action filterAction = getActionMap().get(action);
      StreamflowToggleButton button = new StreamflowToggleButton(filterAction);
      button.addItemListener( new ItemListener()
      {
         public void itemStateChanged(ItemEvent itemEvent)
         {
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED) 
            {

               for (Component component : Iterables.flatten(Iterables.iterable(filterPanel.getComponents()), Iterables.iterable(viewPanel.getComponents())))
               {
                  if( !(component instanceof StreamflowToggleButton) )
                     continue;
                  if (component != itemEvent.getSource() )
                  {
                     ((StreamflowToggleButton)component).setSelected(false);
                  }
               }
               optionsPanel = new JPanel();
               StreamflowToggleButton button = (StreamflowToggleButton) itemEvent.getSource();
               showPopup(button);
           } else if (state == ItemEvent.DESELECTED)
           {
              killPopup();
           }
         }
       });
      panel.add(button);
   }
   
   @Action
   public void filterClear()
   {
      model.clearFilter();
      if( searchField != null )
         searchField.setText( "" );
      killPopup();
   }

   @Action
   public void filterStatus()
   {
      JPanel statusPanel = new JPanel( new BorderLayout() );
      statusPanel.setPreferredSize( new Dimension( 100, 60 )  );
      statusPanel.setMaximumSize( new Dimension( 100, 60 ) );
      statusPanel.setMaximumSize( new Dimension( 100, 60 ) );
      statusPanel.add( statusList, BorderLayout.CENTER );
      optionsPanel.add( statusPanel );
   }
   
   @Action
   public void filterCaseType()
   {
      SortedList<LinkValue> sortedCaseTypes = new SortedList<LinkValue>( model.getPossibleCaseTypes(),
            new SelectedLinkValueComparator(model.getSelectedCaseTypes()));
      
      PerspectiveOptions panel = new PerspectiveOptions(context, sortedCaseTypes, model.getSelectedCaseTypeIds(), true, text( WorkspaceResources.selected_case_types ));
      optionsPanel.add( panel );
   }

   @Action
   public void filterLabel()
   {
      SortedList<LinkValue> sortedLabels = new SortedList<LinkValue>( model.getPossibleLabels(),
            new SelectedLinkValueComparator( model.getSelectedLabels() ) );
      
      PerspectiveOptions panel = new PerspectiveOptions( context, sortedLabels, model.getSelectedLabelIds(), true, text( WorkspaceResources.selected_labels ));
      optionsPanel.add(panel);
   }

   @Action
   public void filterAssignee()
   {
      SortedList<LinkValue> sortedAssignees = new SortedList<LinkValue>( model.getPossibleAssignees(),
            new SelectedLinkValueComparator( model.getSelectedAssignees() ) );

      PerspectiveOptions panel = new PerspectiveOptions( context, sortedAssignees, model.getSelectedAssigneeIds(), false, text( WorkspaceResources.selected_users ) );
      optionsPanel.add(panel);
   }

   @Action
   public void filterProject()
   {
      SortedList<LinkValue> sortedProjects = new SortedList<LinkValue>( model.getPossibleProjects(),
            new SelectedLinkValueComparator( model.getSelectedProjects() ) );

      PerspectiveOptions panel = new PerspectiveOptions( context, sortedProjects, model.getSelectedProjectIds(), true, text( WorkspaceResources.selected_projects ));
      optionsPanel.add( panel );
   }
   
   @Action
   public void filterCreatedOn(ActionEvent event)
   {
      PerspectivePeriodView period = module.objectBuilderFactory().newObjectBuilder( PerspectivePeriodView.class ).use( model.getCreatedOnModel() ).newInstance();
      optionsPanel.add(period);
   }

   @Action
   public void filterDueOn(ActionEvent event)
   {
      PerspectivePeriodView period = module.objectBuilderFactory().newObjectBuilder( PerspectivePeriodView.class ).use( model.getDueOnModel() ).newInstance();
      optionsPanel.add(period);
   }

   @Action
   public void filterCreatedBy()
   {
      SortedList<LinkValue> sortedCreatedBy = new SortedList<LinkValue>( model.getPossibleCreatedBy(),
            new SelectedLinkValueComparator( model.getSelectedCreatedBy() ) );
      
      PerspectiveOptions panel = new PerspectiveOptions( context, sortedCreatedBy, model.getSelectedCreatedByIds(), false, text( WorkspaceResources.selected_users ));
      optionsPanel.add(panel);
   }

   @Action
   public void viewSorting()
   {
      optionsPanel.add(sortByList);
   }
   
   @Action
   public void viewGrouping()
   {
      optionsPanel.add(groupByList);
   }
   
   private void showPopup(final Component button)
   {
      SwingUtilities.invokeLater( new Runnable()
      {

         public void run()
         {
            // Make it impossible to have several popups open at the same time
            if( popup != null )
            {
               popup.dispose();
               popup = null;
            }
            final JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass( JFrame.class, PerspectiveView.this );
            popup = new JDialog( frame );
            popup.getRootPane().registerKeyboardAction( new ActionListener()
            {
               public void actionPerformed( ActionEvent e )
               {
                  killPopup();
                  cleanToggleButtonSelection();
               }
            }, KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), JComponent.WHEN_IN_FOCUSED_WINDOW);
            popup.setUndecorated( true );
            popup.setModal( false );
            popup.setLayout( new BorderLayout() );

            popup.add( optionsPanel, BorderLayout.CENTER );
            Point location = button.getLocationOnScreen();
            popup.setBounds( (int) location.getX(), (int) location.getY() + button.getHeight(), optionsPanel.getWidth(),
                  optionsPanel.getHeight() );
            popup.pack();
            popup.setVisible( true );
            frame.addComponentListener( new ComponentAdapter()
            {
               @Override
               public void componentMoved( ComponentEvent e )
               {
                  if (popup != null)
                  {
                     killPopup();
                     frame.removeComponentListener( this );
                  }
               }
            } );
         }
      } );
   }

   public void killPopup()
   {
      if (popup != null)
      {
         popup.setVisible(false);
         popup.dispose();
         popup = null;
      }

       // only do active update if we are not connected to SearchView
       if (!windowContainsSearchView(WindowUtils.findWindow( this )))
       {
            model.refresh();
       } else
       {
           this.update( null, null );
       }
   }

   private boolean windowContainsSearchView( Container container )
   {
       boolean result = false;
       for (Component c : container.getComponents())
       {
           if (c instanceof Container)
           {
               if (c instanceof SearchView)
               {

                   result = c.isShowing();
               } else
               {
                   result |= windowContainsSearchView((Container) c);
               }
           }
       }
       return result;

   }

   public JDialog getCurrentPopup()
   {
      return popup;
   }

   public void setCurrentPopup( JDialog dialog )
   {
      popup = dialog;
   }

   public void cleanToggleButtonSelection()
   {
      for (Component component : Iterables.flatten( Iterables.iterable( filterPanel.getComponents() ), Iterables.iterable( viewPanel.getComponents() ) ))
      {
         if (!(component instanceof StreamflowToggleButton))
            continue;
         if (((StreamflowToggleButton) component).isSelected())
         {
            ((StreamflowToggleButton) component).setSelected( false );
         }
      }
   }

   public void update( Observable o, Object arg )
   {
      for( Component comp : Iterables.flatten( Iterables.iterable(filterPanel.getComponents()), Iterables.iterable(viewPanel.getComponents()) ) )
      {
         if( comp instanceof StreamflowToggleButton )
         {
            StreamflowToggleButton button = (StreamflowToggleButton)comp;
            boolean selectedIsEmpty = true;
            switch( FilterActions.valueOf( ((ApplicationAction)button.getAction()).getName()))
            {
               case filterStatus:
                  selectedIsEmpty = model.getSelectedStatuses().isEmpty();
                  break;

               case filterAssignee:
                  selectedIsEmpty = model.getSelectedAssigneeIds().isEmpty();
                  break;

               case filterLabel:
                  selectedIsEmpty = model.getSelectedLabelIds().isEmpty();
                  break;

               case filterProject:
                  selectedIsEmpty = model.getSelectedProjectIds().isEmpty();
                  break;

               case filterCaseType:
                  selectedIsEmpty = model.getSelectedCaseTypeIds().isEmpty();
                  break;

               case filterCreatedBy:
                  selectedIsEmpty = model.getSelectedCreatedByIds().isEmpty();
                  break;

               case filterCreatedOn:
                  selectedIsEmpty = Period.none.equals( model.getCreatedOnModel().getPeriod() );
                  break;

               case filterDueOn:
                  selectedIsEmpty = Period.none.equals( model.getDueOnModel().getPeriod() );
                  break;

               case viewSorting:
                  selectedIsEmpty = SortBy.none.equals( model.getSortBy() );
                  break;

               case viewGrouping:
                  selectedIsEmpty = GroupBy.none.equals( model.getGroupBy() );
                  break;
               
               default:

            }
            button.setIcon( selectedIsEmpty ? icon( Icons.down_no_selection, ICON_16 ) : icon( Icons.down_with_selection, ICON_16 ) );
         }
      }
      SwingUtilities.invokeLater( new Runnable(){
         public void run()
         {
            PerspectiveView.this.invalidate();
         }
      });
   }

   class SelectedLinkValueComparator implements Comparator<LinkValue>
   {
     private List<String> selected;
     public SelectedLinkValueComparator(List<String> selectedValues )
     {
         selected = selectedValues;
     }
      public int compare(LinkValue o1, LinkValue o2)
      {
         int val1 = selected.contains( o1.text().get() ) ? 1:0;
         int val2 = selected.contains( o2.text().get() ) ? 1:0;
         int selectedCompare = val2 - val1;
         if (selectedCompare == 0)
            return o1.text().get().compareToIgnoreCase(o2.text().get());
         else
            return selectedCompare;
      }
   }
   
   class StatusList extends JList 
   {
      public StatusList()
      {
         super(new Object[]
         { OPEN.name(), ON_HOLD.name(), CLOSED.name() });
         setCellRenderer(new DefaultListCellRenderer()
         {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (model.getSelectedStatuses().contains(value))
               {
                  setIcon( icon( Icons.check, 12 ));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
               } else
               {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
               }
               setText(text( valueOf( value.toString() )));
               return this;
            }
         });

         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  String selectedValue = (String) statusList.getSelectedValue();
                  if (selectedValue != null)
                  {
                     if (model.getSelectedStatuses().contains(selectedValue))
                     {
                        model.getSelectedStatuses().remove(selectedValue);
                     } else
                     {
                        model.getSelectedStatuses().add(selectedValue);
                     }
                     statusList.clearSelection();
                  }
               }
            }
         });
      }
   }
   
   class SortByList extends JList
   {

      public SortByList()
      {
         List<Enum> allValues = new ArrayList<Enum>();
         allValues.addAll( Arrays.asList( SortBy.values() ));
         allValues.addAll(Arrays.asList(SortOrder.values()));
         setListData(allValues.toArray());
         
         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (value.equals(model.getSortBy()) || value.equals(model.getSortOrder()))
               {
                  setIcon( icon( Icons.check, 12 ));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0 ));
               } else {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0 ));
               }
               setText(text((Enum) value));
               if (index == SortBy.values().length-1)
                  setBorder(BorderFactory.createCompoundBorder(new BottomBorder(Color.LIGHT_GRAY, 1, 3), getBorder()));
               return this;
            }});
         
         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  Enum selectedValue = (Enum) getSelectedValue();
                  if (selectedValue != null)
                  {
                     if (selectedValue instanceof SortBy)
                     {
                        model.setSortBy((SortBy)selectedValue);
                     } else
                     {
                        model.setSortOrder((SortOrder) selectedValue);
                     }
                     clearSelection();
                     repaint();
                  }
               }
            }
         });
      }
   }

   class GroupByList extends JList
   {

      public GroupByList()
      {
         super(GroupBy.values());
         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer()
         {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (value.equals(model.getGroupBy()))
               {
                  setIcon( icon( Icons.check, 12 ));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0 ));
               } else {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0 ));
               }
               setText(text((GroupBy)value));
               return this;
            }});
         
         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  model.setGroupBy( (GroupBy) getSelectedValue());
               }
            }
         });
      }
   }
}