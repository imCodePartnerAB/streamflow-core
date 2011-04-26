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

import org.jdesktop.swingx.*;
import org.qi4j.api.injection.scope.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.util.dialog.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * This view shows a date picker combined with a period list.
 * If a date is picked in the date picker the period is picked date plus period and if no date is picked the time period
 * will be today minus period.
 */
public class PerspectivePeriodView
        extends JPanel
{
   @Service
   DialogService dialogs;

   boolean isCreationDate;
   PerspectivePeriodModel model;

   public void initView(@Uses final PerspectivePeriodModel model)
   {
      setLayout(new BorderLayout());
      this.model = model;

      final JXTextField dateField = new JXTextField();
      dateField.setBorder(BorderFactory.createTitledBorder(text(WorkspaceResources.search_period)));
      dateField.setEditable(false);
      dateField.setText(model.getSearchValue(text(WorkspaceResources.date_format),
              " " + text(WorkspaceResources.date_separator) + " "));

      final JXMonthView monthView = new JXMonthView();
      monthView.setTraversable(true);

      if (model.getDate() != null)
      {
         monthView.setSelectionDate(model.getDate());
         monthView.ensureDateVisible(model.getDate());
      }

      final JList list = new JList(Period.values());

      list.setSelectedValue(model.getPeriod(), true);
      list.setCellRenderer(new DefaultListCellRenderer()
      {

         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                       boolean cellHasFocus)
         {
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            if (value.equals(model.getPeriod()))
            {
               setIcon(icon(Icons.check, 12));
               setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            } else
            {

               setIcon(null);
               setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
            }
            setText(text((Period) value));
            return this;
         }
      });

      list.addListSelectionListener(new ListSelectionListener()
      {

         public void valueChanged(ListSelectionEvent event)
         {
            if (!event.getValueIsAdjusting())
            {
               model.setPeriod((Period) list.getSelectedValue());
               if (model.getPeriod().equals(Period.none))
               {
                  monthView.clearSelection();
                  model.setDate(null);
               } else
               {
                  model.setPeriod((Period) list.getSelectedValue());
               }
               dateField.setText(model.getSearchValue(text(WorkspaceResources.date_format),
                       " " + text(WorkspaceResources.date_separator) + " "));

            }
         }
      });
      add(list, BorderLayout.WEST);

      JPanel datePicker = new JPanel(new BorderLayout());
      datePicker.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

      monthView.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            model.setDate(monthView.getSelectionDate());
            dateField.setText(model.getSearchValue(text(WorkspaceResources.date_format),
                    " " + text(WorkspaceResources.date_separator) + " "));
         }
      });

      add(dateField, BorderLayout.NORTH);

      datePicker.add(monthView, BorderLayout.CENTER);

      add(datePicker, BorderLayout.EAST);
   }
}