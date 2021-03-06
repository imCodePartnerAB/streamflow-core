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
package se.streamsource.streamflow.client.ui.administration;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.swingx.JXFrame;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.ui.account.AccountModel;
import se.streamsource.streamflow.client.ui.account.AccountSelector;
import se.streamsource.streamflow.client.ui.menu.AdministrationMenuBar;
import se.streamsource.streamflow.client.util.JavaHelp;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Administration window
 */
public class AdministrationWindow
      extends FrameView
{
   public AdministrationWindow(
         @Service Application application,
         @Service JavaHelp javaHelp,
         @Uses AdministrationMenuBar menu,
         @Uses final AccountSelector accountSelector,
         @Structure final Module module )
   {
      super( application );

      final JXFrame frame = new JXFrame( i18n.text( AdministrationResources.window_name ) );
      frame.setLocationByPlatform( true );

      setFrame( frame );
      setMenuBar( menu );

      frame.setPreferredSize( new Dimension( 1300, 800 ) );
      frame.pack();

      accountSelector.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               if (accountSelector.isSelectionEmpty())
               {
                  frame.getContentPane().removeAll();
               } else
               {
                  frame.getContentPane().removeAll();

                  AccountModel selectedAccount = accountSelector.getSelectedAccount();
                  AdministrationView administrationView = module.objectBuilderFactory().newObjectBuilder(AdministrationView.class).use( selectedAccount.newAdministrationModel()).newInstance();

                  frame.getContentPane().add( administrationView );
               }
               frame.pack();
            }
         }
      } );
      // Turn off java help for 1.0 release
      //javaHelp.enableHelp( this.getRootPane(), "admin" );
   }

}