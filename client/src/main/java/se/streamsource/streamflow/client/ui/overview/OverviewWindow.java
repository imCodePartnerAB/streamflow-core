/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.overview;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.swingx.JXFrame;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.JavaHelp;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.menu.OverviewMenuBar;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;

/**
 * Overview window
 */
public class OverviewWindow
      extends FrameView
{
   public OverviewWindow(
         @Service Application application,
         @Service JavaHelp javaHelp,
         @Uses OverviewMenuBar menu,
         @Structure final ObjectBuilderFactory obf,
         @Uses final AccountSelector accountSelector )
   {
      super( application );

      final JXFrame frame = new JXFrame( i18n.text( OverviewResources.window_name ) );
      frame.setLocationByPlatform( true );

      setFrame( frame );
      setMenuBar( menu );

      frame.setPreferredSize( new Dimension( 1000, 600 ) );
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
                  OverviewView overviewView = obf.newObjectBuilder( OverviewView.class ).newInstance();
                  overviewView.setModel( selectedAccount.overview() );

                  frame.getContentPane().add( overviewView );
                  overviewView.refreshTree();
               }
            }
         }
      } );
      // Turn off java help for 1.0 release
      //javaHelp.enableHelp( this.getRootPane(), "overview" );
   }

}