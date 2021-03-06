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

import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import ca.odell.glazedlists.swing.EventTableModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.table.SeparatorTable;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TimeZone;

import static java.awt.RenderingHints.*;
import static java.lang.Integer.*;
import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Base class for all views of case lists.
 */
public class CasesTableView
      extends JPanel
      implements TransactionListener
{
   @Structure
   Module module;

   public static final int MILLIS_IN_DAY = (1000 * 60 * 60 * 24);
   public static final WorkspaceResources[] dueGroups = {WorkspaceResources.overdue, WorkspaceResources.duetoday, WorkspaceResources.duetomorrow, WorkspaceResources.duenextweek, WorkspaceResources.duenextmonth, WorkspaceResources.later, WorkspaceResources.noduedate};

   private Comparator<CaseTableValue> groupingComparator = new Comparator<CaseTableValue>()
   {
      public int compare( CaseTableValue o1, CaseTableValue o2 )
      {
         GroupBy groupBy = model.getGroupBy();
         switch (groupBy)
         {
            case caseType:
               return o1.caseType().get().compareTo( o2.caseType().get() );
            case dueOn:
               return dueOnGroup( o1.dueOn().get() ).compareTo( dueOnGroup( o2.dueOn().get() ) );
            case assignee:
               return o1.assignedTo().get().compareTo( o2.assignedTo().get() );
            case project:
               return o1.owner().get().compareTo( o2.owner().get() );
            case priority:
               Integer prio1 = o1.priority().get() != null ? o1.priority().get().priority().get() : new Integer( 9999 );
               Integer prio2 = o2.priority().get() != null ? o2.priority().get().priority().get() : new Integer( 9999 );
               return prio1.compareTo( prio2 );

            default:
               return 0;
         }
      }
   };

   protected JXTable caseTable;
   protected CasesTableModel model;
   private TableFormat tableFormat;
   private ApplicationContext context;

   private PerspectiveView filter;


   public void init( final @Service ApplicationContext context,
                     @Uses CasesTableModel casesTableModel,
                     final @Uses TableFormat tableFormat,
                     @Optional @Uses JTextField searchField )
   {
      setLayout( new BorderLayout() );

      this.context = context;
      this.model = casesTableModel;
      this.tableFormat = tableFormat;

      ActionMap am = context.getActionMap( CasesTableView.class, this );
      setActionMap( am );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CasesTableView.class, this ) );

      // Filter
      filter = module.objectBuilderFactory().newObjectBuilder(PerspectiveView.class).use( model, searchField ).newInstance();
      add( filter, BorderLayout.NORTH );

      // Table
      // Trigger creation of filters and table model
      caseTable = new SeparatorTable( null )
      {
         public Component prepareRenderer(
               TableCellRenderer renderer, int row, int column)
         {
            Component c = super.prepareRenderer(renderer, row, column);

            //  add custom rendering here
            EventTableModel model = (EventTableModel) getModel();
            if( model.getElementAt( row ) instanceof CaseTableValue )
            {

               Map attributes = c.getFont().getAttributes();
               if( ((CaseTableValue) model.getElementAt( row )).removed().get() )
               {
                  attributes.put( TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
               } else if ( ((CaseTableValue) model.getElementAt( row )).unread().get() )
               {
                  attributes.put( TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD );
               }
               c.setFont( new Font(attributes) );
            }
            
            return c;
         }
      };
      caseTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      caseTable.getActionMap().getParent().setParent( am );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      caseTable.getActionMap().remove( "column.horizontalScroll" );
      caseTable.getActionMap().remove( "column.packAll" );
      caseTable.getActionMap().remove( "column.packSelected" );
      caseTable.setColumnControlVisible( true );

      caseTable.setModel( new EventJXTableModel<CaseTableValue>( model.getEventList(), tableFormat ) );

      model.addObserver( new Observer()
      {
         public void update( Observable o, Object arg )
         {
            if (model.getGroupBy() == GroupBy.none)
            {
               caseTable.setModel( new EventJXTableModel<CaseTableValue>( model.getEventList(), tableFormat ) );
            }
            else
            {
               SeparatorList<CaseTableValue> groupingList = new SeparatorList<CaseTableValue>( model.getEventList(),
                     groupingComparator, 1, 10000 );
               caseTable.setModel( new EventJXTableModel<CaseTableValue>( groupingList, tableFormat ) );
            }

            if( !model.containsCaseWithPriority()) {
               model.addInvisibleColumn( 8 );
            }
            
            for (Integer invisibleCol : model.getInvisibleColumns())
            {
               TableColumnModelExt tm = (TableColumnModelExt) caseTable.getColumnModel();
               if (tm.getColumnExt( invisibleCol ).isVisible())
                  caseTable.getColumnExt( invisibleCol ).setVisible( false );
            }

         }
      } );

      caseTable.getColumn( 0 ).setPreferredWidth( 500 );
      caseTable.getColumn( 1 ).setPreferredWidth( 70 );
      caseTable.getColumn( 1 ).setMaxWidth( 70 );
      caseTable.getColumn( 1 ).setResizable( false );
      caseTable.getColumn( 2 ).setPreferredWidth( 300 );
      caseTable.getColumn( 2 ).setMaxWidth( 300 );
      caseTable.getColumn( 3 ).setPreferredWidth( 150 );
      caseTable.getColumn( 3 ).setMaxWidth( 150 );
      caseTable.getColumn( 4 ).setPreferredWidth( 90 );
      caseTable.getColumn( 4 ).setMaxWidth( 90 );
      caseTable.getColumn( 5 ).setPreferredWidth( 150 );
      caseTable.getColumn( 5 ).setMaxWidth( 150 );
      caseTable.getColumn( 6 ).setPreferredWidth( 90 );
      caseTable.getColumn( 6 ).setMaxWidth( 90 );
      caseTable.getColumn( 7 ).setPreferredWidth( 150 );
      caseTable.getColumn( 7 ).setMaxWidth( 150 );
      caseTable.getColumn( 7 ).setResizable( false );
      caseTable.getColumn( 8 ).setPreferredWidth( 100 );
      caseTable.getColumn( 8 ).setMaxWidth( 100 );
      caseTable.getColumn( 9 ).setMaxWidth( 50 );
      caseTable.getColumn( 9 ).setResizable( false );

      caseTable.setAutoCreateColumnsFromModel( false );

      int count = 0;
      for (TableColumn c : caseTable.getColumns())
      {
         c.setIdentifier( (Integer)count );
         count++;
         
         c.addPropertyChangeListener( new PropertyChangeListener()
         {
            public void propertyChange( PropertyChangeEvent evt )
            {
               if ("visible".equals( evt.getPropertyName() ))
               {
                  TableColumnExt columnExt = (TableColumnExt) evt.getSource();

                  if (columnExt.isVisible())
                  {
                     model.removeInvisibleColumn( columnExt.getModelIndex() );
                  } else
                  {
                     model.addInvisibleColumn( columnExt.getModelIndex() );
                  }
               }
            }
         } );
      }

      JScrollPane caseScrollPane = new JScrollPane( caseTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

      add( caseScrollPane, BorderLayout.CENTER );

      caseTable.setDefaultRenderer( Date.class, new DefaultTableRenderer( new StringValue()
      {
         private static final long serialVersionUID = 4782416330896582518L;

         public String getString( Object value )
         {
            return value != null ? DateFormats.getProgressiveDateTimeValue( (Date) value, Locale.getDefault() ) : "";
         }
      } ) );
      caseTable.setDefaultRenderer( ArrayList.class, new DefaultTableCellRenderer()
      {

         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {
            if (value == null) return this;
            if (value instanceof SeparatorList.Separator)
               return caseTable.getDefaultRenderer( SeparatorList.Separator.class )
                     .getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

            JPanel renderer = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

            ArrayList<String> icons = (ArrayList<String>) value;
            for (String icon : icons)
            {
               ImageIcon image = i18n.icon( Icons.valueOf( icon ), 11 );
               JLabel iconLabel = image != null ? new JLabel( image, SwingConstants.LEADING ) : new JLabel( "   " );
               renderer.add( iconLabel );
            }
            if (isSelected)
               renderer.setBackground( table.getSelectionBackground() );
            return renderer;
         }
      } );
      caseTable.setDefaultRenderer( CaseStates.class, new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus, int row, int column )
         {
            if( value == null ) return this;

            EventTableModel model = (EventTableModel) table.getModel();
            boolean hasResolution = !Strings.empty( ((CaseTableValue) model.getElementAt( row )).resolution().get() );
            boolean removed = ((CaseTableValue)model.getElementAt( row )).removed().get();

            String iconName = hasResolution ? "case_status_withresolution_" + value.toString().toLowerCase() + "_icon"
                  : "case_status_" + value.toString().toLowerCase() + "_icon";

            iconName = removed ? "case_status_draft_icon" : iconName;

            JLabel renderedComponent = (JLabel) super.getTableCellRendererComponent( table, value, isSelected, hasFocus,
                  row, column );
            renderedComponent.setHorizontalAlignment( SwingConstants.CENTER );
            setText( null );

            setIcon( i18n.icon( CaseResources.valueOf( iconName ),
                  i18n.ICON_16 ) );
            setName( i18n.text( CaseResources.valueOf( "case_status_" + value.toString().toLowerCase() + "_text" ) ) );
            setToolTipText( i18n.text( CaseResources.valueOf( "case_status_" + value.toString().toLowerCase() + "_text" ) ) );

            return this;
         }
      } );

      caseTable.setDefaultRenderer( PriorityValue.class, new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {
            final PriorityValue priority = (PriorityValue) value;
            String val = priority == null ? "" : priority.text().get();

            JPanel panel = new JPanel( );
            FormLayout layout = new FormLayout( "10dlu, 50dlu:grow", "pref" );
            DefaultFormBuilder formBuilder = new DefaultFormBuilder( layout, panel );

            panel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
            JLabel label = new JLabel( ){
               @Override
               protected void paintComponent(Graphics g) {
                  Color color = getBackground();
                  if( priority != null )
                  {
                     if( !Strings.empty( priority.color().get() ) )
                        color = new Color( parseInt( priority.color().get() ) );
                     else
                        color = Color.BLACK;
                  }
                  final Color FILL_COLOR = ColorUtil.removeAlpha( color );

                  Graphics2D g2 = (Graphics2D) g.create();

                  try {
                     g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                     g2.setColor(Color.LIGHT_GRAY);
                     final int DIAM = Math.min(getWidth(), getHeight());
                     final int inset = 3;
                     g2.fill(new Ellipse2D.Float(inset, inset, DIAM-2*inset, DIAM-2*inset));
                     g2.setColor(FILL_COLOR);
                     final int border = 1;
                     g2.fill(new Ellipse2D.Float(inset+border, inset+border, DIAM-2*inset-2*border, DIAM-2*inset-2*border));
                  } finally {
                     g2.dispose();
                  }
               }
            };
            label.setPreferredSize( new Dimension( 10, 10 ) );
            //label.setBorder( BorderFactory.createLineBorder( Color.RED ) );
            formBuilder.add( ( Strings.empty(val) || "-".equals( val ) ) ? new JLabel( ) : label,
                  new CellConstraints(1 , 1, 1, 1, CellConstraints.FILL, CellConstraints.FILL,
                        new Insets( 0, 0, 0, 0 ) ) );
            JLabel text = new JLabel( val );
            //text.setBorder( BorderFactory.createLineBorder( Color.RED ) );
            formBuilder.add( text, new CellConstraints(2, 1, 1, 1, CellConstraints.LEFT, CellConstraints.FILL,
                  new Insets( 0, 0, 0, 0 ) ) );

            if (isSelected)
            {
               panel.setBackground( table.getSelectionBackground() );
               text.setForeground( table.getSelectionForeground() );
            }
            return panel;
         }
      });
      caseTable.setDefaultRenderer( SeparatorList.Separator.class, new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object separator, boolean isSelected, boolean hasFocus, int row, int column )
         {
            String value = "";
            boolean emptyDescription = false;
            switch (model.getGroupBy())
            {
               case caseType:
                  emptyDescription = Strings.empty( ((CaseTableValue) ((SeparatorList.Separator) separator).first()).caseType().get() );
                  value = !emptyDescription ? ((CaseTableValue) ((SeparatorList.Separator) separator).first()).caseType().get() : text( WorkspaceResources.no_casetype );
                  break;
               case assignee:
                  emptyDescription = Strings.empty( ((CaseTableValue) ((SeparatorList.Separator) separator).first()).assignedTo().get() );
                  value = !emptyDescription ? ((CaseTableValue) ((SeparatorList.Separator) separator).first()).assignedTo().get() : text( WorkspaceResources.no_assignee );
                  break;
               case project:
                  emptyDescription = Strings.empty( ((CaseTableValue) ((SeparatorList.Separator) separator).first()).owner().get() );
                  value = !emptyDescription ? ((CaseTableValue) ((SeparatorList.Separator) separator).first()).owner().get() : text( WorkspaceResources.no_project );
                  break;
               case dueOn:
                  value = text( dueGroups[dueOnGroup( ((CaseTableValue) ((SeparatorList.Separator) separator).first()).dueOn().get() )] );
                  break;
               case priority:
                  emptyDescription =  ((CaseTableValue) ((SeparatorList.Separator) separator).first()).priority().get() == null
                        || Strings.empty( ((CaseTableValue) ((SeparatorList.Separator) separator).first()).priority().get().color().get() );
                  value = !emptyDescription ? ((CaseTableValue) ((SeparatorList.Separator) separator).first()).priority().get().text().get() : text( WorkspaceResources.no_priority);
                  break;
            }

            Component component = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
            component.setFont( component.getFont().deriveFont( Font.BOLD + Font.ITALIC ) );
            component.setBackground( Color.lightGray );
            return component;
         }
      } );

     AbstractHighlighter separatorHighlighter = (AbstractHighlighter) HighlighterFactory.createSimpleStriping(HighlighterFactory.QUICKSILVER);
     separatorHighlighter.setHighlightPredicate(new HighlightPredicate.TypeHighlightPredicate(SeparatorList.Separator.class));
     caseTable.addHighlighter(HighlighterFactory.createAlternateStriping());
     caseTable.addHighlighter(separatorHighlighter);

      addFocusListener( new FocusAdapter()
      {
         public void focusGained( FocusEvent e )
         {
            caseTable.requestFocusInWindow();
         }
      } );

      model.getEventList().addListEventListener( new ListEventListener<CaseTableValue>()
      {
         public void listChanged( ListEvent<CaseTableValue> listChanges )
         {
            // Synchronize lists
            Set<String> labels = new HashSet<String>();
            Set<String> assignees = new HashSet<String>();
            Set<String> projects = new HashSet<String>();
            for (CaseTableValue caseTableValue : listChanges.getSourceList())
            {
               for (LinkValue linkValue : caseTableValue.labels().get().links().get())
               {
                  labels.add( linkValue.text().get() );
               }

               assignees.add( caseTableValue.assignedTo().get() );
               projects.add( caseTableValue.owner().get() );
            }
            List<String> sortedLabels = new ArrayList<String>( labels );
            List<String> sortedAssignees = new ArrayList<String>( assignees );
            List<String> sortedProjects = new ArrayList<String>( projects );
            Collections.sort( sortedLabels );
            Collections.sort( sortedAssignees );
            Collections.sort( sortedProjects );
            sortedLabels.add( 0, text( WorkspaceResources.all ) );
            sortedAssignees.add( 0, text( WorkspaceResources.all ) );
            sortedProjects.add( 0, text( WorkspaceResources.all ) );
         }
      } );
      
      addHierarchyListener(new HierarchyListener()
      {
         public void hierarchyChanged(HierarchyEvent e)
         {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0)
            {
               if (CasesTableView.this.isShowing())
               {
                  context.getActionMap().get("savePerspective").setEnabled(true);
               }
            }
         }
      });
      new RefreshWhenShowing( this, model );
   }

   public JXTable getCaseTable()
   {
      return caseTable;
   }

   public CasesTableModel getModel()
   {
      return model;
   }

   private Integer dueOnGroup( Date date )
   {
      /**
       * 0 = Overdue
       * 1 = Today
       * 2 = Tomorrow
       * 3 = Within next week
       * 4 = Within next month
       * 5 = Later
       * 6 = No due date
       */

      long currentTime = System.currentTimeMillis();
      currentTime = currentTime / MILLIS_IN_DAY;
      currentTime *= MILLIS_IN_DAY;
      Date today = new Date( currentTime );
      Date lateToday = new Date( currentTime + MILLIS_IN_DAY - 1 );

      Calendar month = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
      month.setTime( today );
      month.add( Calendar.MONTH, 1 );

      Calendar week = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
      week.setTime( today );
      week.add( Calendar.WEEK_OF_YEAR, 1 );

      Calendar tomorrow = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
      tomorrow.setTime( lateToday );
      tomorrow.add( Calendar.DATE, 1 );

      int group;
      if (date == null)
         group = 6;
      else if (date.after( month.getTime() ))
         group = 5; // Later
      else if (date.after( week.getTime() ))
         group = 4; // Within next month
      else if (date.after( tomorrow.getTime() ))
         group = 3; // Within next week
      else if (date.after( lateToday ))
         group = 2; // Tomorrow
      else if (date.after( today ))
         group = 1;
      else
         group = 0;

      return group;
   }

   public void notifyTransactions( final Iterable<TransactionDomainEvents> transactions )
   {

      if (Events.matches( withNames( "createdCase" ), transactions ))
      {
         final DomainEvent event = Iterables.first( Iterables.filter( withNames( "createdCase" ), Events.events( transactions ) ) );;

         context.getTaskService().execute( new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.refresh();
            }

            @Override
            protected void succeeded( Iterable<TransactionDomainEvents> transactionEventsIterable )
            {
               super.succeeded( transactionEventsIterable );

               TableModel model = caseTable.getModel();
               boolean rowFound = false;

              for( int i=0, n=model.getRowCount(); i < n; i++ )
               {
                  if( model.getValueAt( i, model.getColumnCount() ).toString().endsWith( EventParameters.getParameter( event, "param1" ) + "/") )
                  {
                     caseTable.getSelectionModel().setSelectionInterval( caseTable.convertRowIndexToView( i ), caseTable.convertRowIndexToView( i )  );
                     caseTable.scrollRectToVisible( caseTable.getCellRect( i, 0, true ) );
                     rowFound = true;
                     break;
                  }
               }
            }
         } );
      } else if (Events.matches( withNames( "addedLabel", "removedLabel",
            "changedDescription", "changedCaseType", "changedStatus",
            "changedOwner", "assignedTo", "unassigned", "changedRemoved","deletedEntity",
            "updatedContact", "addedContact", "deletedContact",
            "createdConversation", "changedDueOn", "submittedForm", "createdAttachment",
            "removedAttachment", "changedPriority", "setUnread", "createdMessageFromDraft" ), transactions ))
      {
         context.getTaskService().execute( new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {

               model.refresh();

               if (Events.matches( withNames( "changedStatus",
                     "changedOwner", "assignedTo", "unassigned", "deletedEntity"  ), transactions ))
               {
                  caseTable.getSelectionModel().clearSelection();
               }
            }
         } );
      }
   }
}