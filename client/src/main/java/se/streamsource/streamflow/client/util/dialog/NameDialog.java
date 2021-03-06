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
package se.streamsource.streamflow.client.util.dialog;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;

import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Select a name for something.
 */
public class NameDialog
      extends JPanel
{
   public JTextField nameField;

   String name;

   public NameDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

      FormLayout layout = new FormLayout( "40dlu, 5dlu, 120dlu:grow", "pref" );

      JPanel form = new JPanel( layout );
      form.setFocusable( false );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            form );

      nameField = new JTextField();

      builder.add(new JLabel( i18n.text( AdministrationResources.name_label ) ));
      builder.nextColumn(2);
      builder.add(nameField);

      add(form, BorderLayout.CENTER);
   }

   public String name()
   {
      return name;
   }

   @Action
   public void execute()
   {
      name = nameField.getText();

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}
