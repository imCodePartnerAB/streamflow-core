/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import ca.odell.glazedlists.*;
import org.jdesktop.application.*;
import org.qi4j.api.object.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.util.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.List;

import static se.streamsource.streamflow.client.util.i18n.*;

public class PerspectiveOptionsView extends JPanel
{
   private JList itemList;
   private JTextField filterField;


   public PerspectiveOptionsView(final ApplicationContext context, List<LinkValue> values,
         final List selectedValues, final boolean isGrouped,  ObjectBuilderFactory obf)
   {

      super(new BorderLayout());
      final int currentSelectedLabelCount = selectedValues.size();
      setPreferredSize( new Dimension( 250, 200 ) );
      setMaximumSize( new Dimension( 250, 200 ) );
      setMinimumSize( new Dimension( 250, 200 ) );

      JPanel list;

      if( isGrouped )
      {
         list = new GroupedFilteredList();
         ((GroupedFilteredList)list).getList().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         EventList<TitledLinkValue> titledLinks = new BasicEventList<TitledLinkValue>( );
         int count = 0;
         for( LinkValue link : values )
         {
            TitledLinkValue linkValue = (TitledLinkValue) link;
            if( count < selectedValues.size())
            {
               ValueBuilder<TitledLinkValue> builder = linkValue.buildWith();
               builder.prototype().title().set( text( WorkspaceResources.selected_projects ) );
               titledLinks.add( builder.newInstance() );
            }
            else
            {
               titledLinks.add( linkValue );
            }
            count++;
         }
         ((GroupedFilteredList)list).setEventList( titledLinks, text( WorkspaceResources.selected_projects ) );

         this.itemList = ((GroupedFilteredList)list).getList();
         this.filterField = ((GroupedFilteredList)list).getFilterField();
      } else
      {
         list = new FilteredList();
         ((FilteredList)list).getList().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         ((FilteredList)list).setEventList( (EventList<LinkValue>) values, false );
         this.itemList = ((FilteredList)list).getList();
         this.filterField = ((FilteredList)list).getFilterField();
      }
      add(list);

      itemList.setCellRenderer(new DefaultListCellRenderer(){
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
               boolean cellHasFocus)
         {
            String text;
            if (value instanceof SeparatorList.Separator )
            {
               SeparatorList.Separator separator = (SeparatorList.Separator) value;

               if (separator.first() instanceof TitledLinkValue )
               {
                  text = ((TitledLinkValue) separator.first()).title().get();
               } else
               {
                  text = ((LinkValue) separator.first()).text().get();
               }
               Component component = super.getListCellRendererComponent( list, text, index, isSelected, cellHasFocus );
               setFont( getFont().deriveFont( Font.BOLD ) );
               return component;
            } else
            {
               LinkValue linkValue = (LinkValue) value;
               setFont( list.getFont() );
               setBackground( list.getBackground() );
               setForeground( list.getForeground() );
               if (selectedValues.contains( linkValue.id().get() ))
               {
                  setIcon( icon( Icons.check, 12 ) );
                  setBorder( BorderFactory.createEmptyBorder( 4, 0, 0, 0 ) );
               } else
               {

                  setIcon( null );
                  setBorder( BorderFactory.createEmptyBorder( 4, 16, 0, 0 ) );
               }
               setText( linkValue.text().get() );
               if ( isGrouped ? index == currentSelectedLabelCount : index == currentSelectedLabelCount - 1 )
                  setBorder( BorderFactory.createCompoundBorder( new BottomBorder( Color.LIGHT_GRAY, 1, 3 ), getBorder() ) );
               return this;
            }
         }
      });

      itemList.addListSelectionListener(new ListSelectionListener()
      {

         public void valueChanged(ListSelectionEvent event)
         {
            if (!event.getValueIsAdjusting()
                  && ! (itemList.getSelectedValue() instanceof SeparatorList.Separator) )
            {
               LinkValue linkValue = (LinkValue) itemList.getSelectedValue();
               if (linkValue != null)
               {
                  if (selectedValues.contains(linkValue.id().get()))
                  {
                     selectedValues.remove(linkValue.id().get());
                  } else
                  {
                     selectedValues.add(linkValue.id().get());
                  }
                  itemList.clearSelection();
               }
            }
         }
      });
      //
      // Skip filtering if short list
      if (values.size() < 10)
         filterField.setVisible(false);
   }

   public void setSelectionMode(int selectionMode)
   {
      itemList.setSelectionMode(selectionMode);
   }

   public JTextField getFilterField()
   {
      return filterField;
   }

}