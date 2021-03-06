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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.OpenAttachmentTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.WrapLayout;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 *
 */
public class MessageDraftAttachmentsView
   extends JPanel
   implements Refreshable, TransactionListener
{
   @Structure
   Module module;

   @Service
   DialogService dialogs;

   AttachmentsModel model;

   JPanel attachmentsPanel;

   Map<StreamflowButton,AttachmentDTO> openFileMap = new HashMap<StreamflowButton,AttachmentDTO>();

   ActionMap am;

   public MessageDraftAttachmentsView( @Service ApplicationContext context, @Uses AttachmentsModel model )
   {
      this.model = model;
      am = context.getActionMap( this );
      setLayout( new BorderLayout(  ) );

      FormLayout formLayout = new FormLayout( "pref,4dlu,pref:grow", "40dlu" );
      PanelBuilder builder = new PanelBuilder( formLayout );

      StreamflowButton addButton = new StreamflowButton(am.get("add"));
      builder.add( addButton );
      builder.nextColumn(2);

      attachmentsPanel = new JPanel(new WrapLayout( FlowLayout.LEFT ) );

      JScrollPane scroll = new JScrollPane(  );
      scroll.setBorder( BorderFactory.createEmptyBorder() );
      scroll.setViewportView( attachmentsPanel );

      builder.add( scroll );

      add( builder.getPanel(), BorderLayout.CENTER );
      new RefreshWhenShowing( this, this );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if( matches(onEntityTypes( "se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity" ), transactions ))
      {
         // on usecase delete no update necessary
         if( matches( withUsecases( "delete" ), transactions ))
         {
            if( matches(  withNames( "removedAttachment" ), transactions ))
               refresh();
            else
               return;
         }

         else if ( matches( withNames( "addedAttachment" ), transactions ))
            refresh();
      }
   }

   public void refresh()
   {
      attachmentsPanel.removeAll();
      openFileMap.clear();

      model.refresh();
      for( AttachmentDTO attachmentIn : model.getEventList() )
      {
         final AttachmentDTO attachment = attachmentIn;

         JPanel attachmentPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
         StreamflowButton openButton = new StreamflowButton( attachment.text().get(), i18n.icon( Icons.attachments, 14 ) );
         openButton.setBorder( BorderFactory.createEmptyBorder() );

         openFileMap.put( openButton, attachment );

         openButton.addActionListener( am.get( "open" ) );
         attachmentPanel.add( openButton );

         StreamflowButton removeButton = new StreamflowButton( i18n.icon( Icons.drop, 14 ) );
         removeButton.setBorder( BorderFactory.createEmptyBorder() );
         removeButton.addActionListener( new ActionListener()
         {
            public void actionPerformed( ActionEvent e )
            {
               ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
               dialog.setRemovalMessage(i18n.text( WorkspaceResources.attachment ) );
               dialogs.showOkCancelHelpDialog(MessageDraftAttachmentsView.this, dialog, i18n.text( StreamflowResources.confirmation));

               if (dialog.isConfirmed())
               {
                  new CommandTask()
                  {
                     @Override
                     public void command()
                           throws Exception
                     {
                        try
                        {
                           model.removeAttachment( attachment );
                        } catch (Throwable e)
                        {
                           e.printStackTrace();
                        }
                     }
                  }.execute();
               }
            }
         } );

         attachmentPanel.add( removeButton );

         attachmentsPanel.add( attachmentPanel );
      }

      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            MessageDraftAttachmentsView.this.revalidate();
            MessageDraftAttachmentsView.this.repaint();
         }
      } );

   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task open( ActionEvent e ) throws IOException
   {
      AttachmentDTO attachment = openFileMap.get( e.getSource() );
      return new OpenAttachmentTask( attachment.text().get(), attachment.href().get(), MessageDraftAttachmentsView.this, model, dialogs );
   }


   @Action(block = Task.BlockingScope.APPLICATION)
   public Task add() throws IOException
   {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setMultiSelectionEnabled(true);

      if (fileChooser.showDialog(this, i18n.text(WorkspaceResources.create_attachment)) == JFileChooser.APPROVE_OPTION)
      {
         final File[] selectedFiles = fileChooser.getSelectedFiles();

         return new AddAttachmentTask(selectedFiles);
      } else
         return null;
   }

   private class AddAttachmentTask
         extends CommandTask
   {
      File[] selectedFiles;

      private AddAttachmentTask(File[] selectedFiles)
      {
         this.selectedFiles = selectedFiles;
      }

      @Override
      public void command()
            throws Exception
      {
         setTitle( getResourceMap().getString( "title" ) );
         String message = getResourceMap().getString("message");

         for (File file : selectedFiles)
         {
            setMessage( message + " " + file.getName() );
            FileInputStream fin = new FileInputStream(file);
            model.createAttachment(file, fin);
         }
      }
   }

}
