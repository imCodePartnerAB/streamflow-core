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

package se.streamsource.streamflow.client.ui.administration.users;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.util.*;
import org.qi4j.api.injection.scope.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.client.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * A basic dialog for resetting user passwords.
 */
public class ResetPasswordDialog
      extends JPanel
{
   public JPasswordField passwordField;
   public JPasswordField confirmPasswordField;

   String password;

   @Uses
   DialogService dialogs;

   public ResetPasswordDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );

      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

      FormLayout layout = new FormLayout( "70dlu, 5dlu, 120dlu:grow", "pref, pref" );

      JPanel form = new JPanel( layout );
      form.setFocusable( false );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            form );

      passwordField = new JPasswordField();
      confirmPasswordField = new JPasswordField();

      builder.add(new JLabel( i18n.text( AdministrationResources.new_password ) ));
      builder.nextColumn(2);
      builder.add(passwordField);
      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.confirm_password ) ));
      builder.nextColumn(2);
      builder.add(confirmPasswordField);
      
      add(form, BorderLayout.CENTER);
   }

   public String password()
   {
      return password;
   }

   @org.jdesktop.application.Action
   public void execute()
   {
      if (new String( passwordField.getPassword() ).equals( new String( confirmPasswordField.getPassword() ) ))
      {
         password = new String( passwordField.getPassword() );
      } else
      {
         passwordField.setText( "" );
         confirmPasswordField.setText( "" );
         dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( AdministrationResources.passwords_dont_match ) ) );
         return;
      }
      WindowUtils.findWindow( this ).dispose();
   }

   @org.jdesktop.application.Action
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}
