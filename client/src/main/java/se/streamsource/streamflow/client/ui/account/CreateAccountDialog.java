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

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.PASSWORD;
import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTFIELD;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class CreateAccountDialog extends JPanel
{
   final Logger logger = LoggerFactory.getLogger( getClass().getName() );

   @Structure
   Module module;

   private JTextField nameField;
   private JTextField serverField;
   private JTextField usernameField;
   private JPasswordField passwordField;
   private AccountSettingsValue settings;

   public CreateAccountDialog( @Service ApplicationContext context )
   {
      setActionMap( context.getActionMap( this ) );

      FormLayout layout = new FormLayout( "50dlu, 5dlu, 175dlu",
            "pref, pref, pref, pref" );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );

      nameField = (JTextField) TEXTFIELD.newField();
      serverField = (JTextField) TEXTFIELD.newField();
      String defaultServer = "";
      try
      {
         BasicService bs = (BasicService) ServiceManager.lookup( "javax.jnlp.BasicService" );
         String host = bs.getCodeBase().getHost();
         int portInt = bs.getCodeBase().getPort();
         String port = portInt != -1 ? ":" + bs.getCodeBase().getPort() : "";
         defaultServer = host + port;
      } catch (UnavailableServiceException e)
      {
         logger.info( "Streamflow Client not started via Java Web Start - cannot determine default host!" );
      }
      if (!"".equals( defaultServer ))
         serverField.setText( "http://" + defaultServer + "/streamflow" );
      usernameField = (JTextField) TEXTFIELD.newField();
      passwordField = (JPasswordField) PASSWORD.newField();

      builder.add( new JLabel( i18n.text( AdministrationResources.create_account_name ) ) );
      builder.nextColumn( 2 );
      builder.add( nameField );
      builder.nextLine();

      builder.add( new JLabel( i18n.text( AdministrationResources.create_account_server ) ) );
      builder.nextColumn( 2 );
      builder.add( serverField );
      builder.nextLine();

      builder.add( new JLabel( i18n.text( AdministrationResources.create_account_username ) ) );
      builder.nextColumn( 2 );
      builder.add( usernameField );
      builder.nextLine();

      builder.add( new JLabel( i18n.text( AdministrationResources.create_account_password ) ) );
      builder.nextColumn( 2 );
      builder.add( passwordField );
      builder.nextLine();
   }

   @Action
   public void execute()
   {
      try
      {
         ValueBuilder<AccountSettingsValue> accountBuilder = module.valueBuilderFactory().newValueBuilder(AccountSettingsValue.class);
         accountBuilder.prototype().name().set( nameField.getText() );
         accountBuilder.prototype().server().set( serverField.getText() );
         accountBuilder.prototype().userName().set( usernameField.getText() );
         accountBuilder.prototype().password().set( String.valueOf( passwordField.getPassword() ) );
         settings = accountBuilder.newInstance();
      } catch (ConstraintViolationException cve)
      {
         JOptionPane.showMessageDialog( new JFrame(), cve.getMessage(), "Dialog",
               JOptionPane.ERROR_MESSAGE );
         return;
      }

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public AccountSettingsValue settings()
   {
      return settings;
   }
}