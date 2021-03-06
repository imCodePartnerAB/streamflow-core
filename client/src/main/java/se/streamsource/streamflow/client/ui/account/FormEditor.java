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
package se.streamsource.streamflow.client.ui.account;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * JAVADOC
 */
public class FormEditor
{
   Component[] components;
   boolean editing = false;

   public FormEditor(Component... components)
   {
      this.components = components;
      init(components);
      view();
   }

   private void init(Component... components)
   {
      for (Component component : components)
      {
         if (component instanceof JPanel)
         {
            init(((JPanel) component).getComponents());
         } else if (component instanceof JLabel)
         {
            final JLabel label = (JLabel) component;
            label.addPropertyChangeListener("text",
                  new PropertyChangeListener()
                  {
                     public void propertyChange(PropertyChangeEvent evt)
                     {
                        setVisible(label, !(evt.getNewValue() == null || evt
                              .getNewValue().equals("")));
                     }
                  });
         } else if (component instanceof JTextComponent)
         {
            final JTextComponent text = (JTextComponent) component;
            text.getDocument().addDocumentListener(new DocumentListener()
            {
               public void insertUpdate(DocumentEvent e)
               {
                  changedUpdate(e);
               }

               public void removeUpdate(DocumentEvent e)
               {
                  changedUpdate(e);
               }

               public void changedUpdate(DocumentEvent e)
               {
                  if (!isEditing())
                  {
                     if (text.getText().equals(""))
                     {
                        setVisible(text, false);
                     } else
                     {
                        if (!text.isVisible())
                        {
                           setVisible(text, true);
                        }
                     }
                  }
               }
            });
         }
      }
   }

   public boolean isEditing()
   {
      return editing;
   }

   public void view()
   {
      editing = false;
      view(components);
   }

   private void view(Component... components)
   {
      for (Component component : components)
      {
         if (component instanceof JPanel)
         {
            view(((JPanel) component).getComponents());
         } else if (component instanceof JLabel)
         {
            // Do nothing
         } else if (component instanceof JComponent)
         {
            final JComponent text = (JComponent) component;
            text.setEnabled(false);
         }
         component.doLayout();
      }
   }

   public void edit()
   {
      editing = true;
      edit(components);
   }

   private void edit(Component... components)
   {
      for (Component component : components)
      {
         if (component instanceof JPanel)
         {
            edit(((JPanel) component).getComponents());
         } else if (component instanceof JLabel)
         {
            // Do nothing
         } else if (component instanceof JComponent)
         {
            final JComponent text = (JComponent) component;
            text.setEnabled(true);
            setVisible(component, true);
         }
      }
   }

   private void setVisible(Component component, boolean b)
   {
      component.setVisible(b);
      JLabel label = (JLabel) ((JComponent) component)
            .getClientProperty("labeledBy");
      if (label != null)
         label.setVisible(b);
   }
}
