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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.renderer.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.ui.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.ui.administration.users.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.resource.user.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class ProxyUsersView
      extends JPanel
   implements TransactionListener
{
   private ProxyUsersModel model;

   @Uses
   Iterable<CreateProxyUserDialog> userDialogs;

   @Uses
   Iterable<ResetPasswordDialog> resetPwdDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Service
   DialogService dialogs;

   JXTable proxyUsersTable;
   private EventJXTableModel<ProxyUserDTO> tableModel;

   public ProxyUsersView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      ApplicationActionMap am = context.getActionMap( this );
      setActionMap( am );

      this.model = obf.newObjectBuilder( ProxyUsersModel.class ).use( client ).newInstance();
      TableFormat<ProxyUserDTO> tableFormat = new ProxyUsersTableFormat();

      tableModel = new EventJXTableModel<ProxyUserDTO>( model.getEventList(), tableFormat );

      proxyUsersTable = new JXTable( tableModel );
      proxyUsersTable.getColumn( 0 ).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
      proxyUsersTable.getColumn( 0 ).setMaxWidth(60);
      proxyUsersTable.getColumn( 0 ).setResizable(false);
      proxyUsersTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("resetPassword")));

      JScrollPane scroll = new JScrollPane();
      scroll.setViewportView( proxyUsersTable );

      super.setLayout(new BorderLayout());
      super.add( scroll, BorderLayout.CENTER );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "resetPassword" ) );
      options.add( am.get( "remove" ) );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( new OptionsAction( options ) ) );
      super.add( toolbar, BorderLayout.SOUTH );

      proxyUsersTable.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "resetPassword" ), am.get( "remove") ) );
      new RefreshWhenShowing(this, model);
   }


   @org.jdesktop.application.Action
   public Task add()
   {
      final CreateProxyUserDialog dialog = userDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_user_title ) );

      if ( dialog.userCommand() != null )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.createProxyUser( dialog.userCommand() );
            }
         };
      } else
         return null;
   }

   @org.jdesktop.application.Action
   public Task resetPassword()
   {
      final ResetPasswordDialog dialog = resetPwdDialogs.iterator().next();

      final ProxyUserDTO proxyUser = tableModel.getElementAt( proxyUsersTable.convertRowIndexToModel( proxyUsersTable.getSelectedRow()) );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.reset_password_title ) + ": " + proxyUser.description().get() );

      if (dialog.password() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.resetPassword( proxyUser, dialog.password() );
            }
         };
      } else
         return null;
   }

      @Action
   public Task remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      final ProxyUserDTO proxyUser = tableModel.getElementAt( proxyUsersTable.convertRowIndexToView( proxyUsersTable.getSelectedRow()) );
      dialog.setRemovalMessage( proxyUser.description().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.remove_proxyuser_title ) );

      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove( proxyUser );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames("createdProxyUser", "changedEnabled" ), transactions ))
      {
         model.refresh();
      }
   }

   private class ProxyUsersTableFormat
      implements WritableTableFormat<ProxyUserDTO>, AdvancedTableFormat<ProxyUserDTO>
   {
      public boolean isEditable( ProxyUserDTO proxyUserDTO, int i )
      {
         return i == 0;
      }

      public ProxyUserDTO setColumnValue( final ProxyUserDTO proxyUserDTO, final Object o, int i )
      {
         new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.changeEnabled( proxyUserDTO, (Boolean)o );
            }
         }.execute();
         return null;
      }

      public int getColumnCount()
      {
         return 3;
      }

      public String getColumnName( int i )
      {
         return new String[]{
         text( AdministrationResources.user_enabled_label ),
         text( AdministrationResources.username_label ),
         text(AdministrationResources.description_label)}[i];
      }

      public Class getColumnClass(int i)
      {
         return new Class[]{Boolean.class, String.class, String.class}[i];
      }

      public Comparator getColumnComparator(int i)
      {
         return null;
      }

      public Object getColumnValue( ProxyUserDTO proxyUserDTO, int i )
      {
         switch (i)
         {
            case 0:
               return !proxyUserDTO.disabled().get();
            case 1:
               return proxyUserDTO.username().get();
            case 2:
               return proxyUserDTO.description().get();
         }
         return null;
      }
   }
}