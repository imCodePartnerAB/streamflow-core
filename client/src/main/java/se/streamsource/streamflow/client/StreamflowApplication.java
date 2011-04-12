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

package se.streamsource.streamflow.client;

import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.error.*;
import org.jdesktop.swingx.util.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.io.*;
import org.qi4j.api.object.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.property.*;
import org.qi4j.spi.structure.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.routing.*;
import org.slf4j.*;
import se.streamsource.streamflow.client.assembler.*;
import se.streamsource.streamflow.client.ui.*;
import se.streamsource.streamflow.client.ui.account.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.ui.overview.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.application.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.resource.caze.*;

import javax.jnlp.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Component;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * Controller for the application
 */
@ProxyActions({"cut", "copy", "paste",
        "createDraft", "complete", "assign", "drop", "forward", // Case related proxy actions
        "find", "selectTree", "selectTable", "selectDetails"})
public class StreamflowApplication
        extends SingleFrameApplication
        implements TransactionListener, SingleInstanceListener
{
   public static ValueType DOMAIN_EVENT_TYPE;

   final Logger logger = LoggerFactory.getLogger(getClass().getName());
   final Logger streamflowLogger = LoggerFactory.getLogger(LoggerCategories.STREAMFLOW);

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ModuleSPI module;

   @Service
   DialogService dialogs;

   @Service
   EventStream stream;

   @Service
   JavaHelp javaHelp;

   AccountsModel accountsModel;

   private AccountSelector accountSelector;

   WorkspaceWindow workspaceWindow;
   OverviewWindow overviewWindow;
   AdministrationWindow administrationWindow;
   DebugWindow debugWindow;

   public ApplicationSPI app;
   private String openCaseJson;

   public StreamflowApplication()
   {
      super();

      // We have to ensure that calls to the server are done in the order they were executed,
      // so make it single threaded
      getContext().removeTaskService(getContext().getTaskService());
      getContext().addTaskService(new TaskService("default", Executors.newSingleThreadExecutor()));

      getContext().getResourceManager().setApplicationBundleNames(Arrays.asList("se.streamsource.streamflow.client.resources.StreamflowApplication"));
   }

   @Override
   protected void initialize(String[] args)
   {
      // Check if we are supposed to open a particular case
      final File[] openFile = new File[1];
      if (args.length > 0)
      {
         if (args[0].equals("-open"))
         {
            openFile(new File(args[1]));
         }
      }

      try
      {
         SingleInstanceService singleInstanceService = (SingleInstanceService) ServiceManager.lookup(SingleInstanceService.class.getName());
         singleInstanceService.addSingleInstanceListener(this);
      } catch (UnavailableServiceException e)
      {
         // Ignore
      }
   }

   public void newActivation(String[] args)
   {
      System.out.println("New args:" + Arrays.asList(args));

      if (args.length > 0)
      {
         initialize(args);
         CaseDTO caseDTO = module.valueBuilderFactory().newValueFromJSON(CaseDTO.class, openCaseJson);
         openCaseJson = null;
         workspaceWindow.getCurrentWorkspace().openCase(caseDTO.caseId().get());
      }
   }

   public void openFile(File file)
   {
      System.out.println("Opening: " + file);
      try
      {
         final StringBuffer buf = new StringBuffer();
         Inputs.text(file.getAbsoluteFile()).transferTo(Outputs.withReceiver(new Receiver<String, RuntimeException>()
         {
            public void receive(String item) throws RuntimeException
            {
               buf.append(item);
            }
         }));
         openCaseJson = buf.toString();
         System.out.println(buf);
      } catch (IOException e)
      {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
   }

   public void init(@Uses final AccountsModel accountsModel,
                    @Structure final ObjectBuilderFactory obf,
                    @Uses final AccountSelector accountSelector,
                    @Service EventStream stream
   ) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException
   {
      DOMAIN_EVENT_TYPE = module.valueDescriptor(DomainEvent.class.getName()).valueType();

      this.stream = stream;

//      NotificationGlassPane.install();

      try
      {
         //Check for Mac OS - and load if we are on Mac
         getClass().getClassLoader().loadClass("com.apple.eawt.Application");
         MacOsUIExtension osUIExtension = new MacOsUIExtension(this);
         osUIExtension.attachMacUIExtension();
         osUIExtension.attachMacOpenFileExtension();
         osUIExtension.convertAccelerators();
      } catch (Throwable e)
      {
         //Do nothing
      }


      // General UI settings
      String toolTipDismissDelay = i18n.text(StreamflowResources.tooltip_delay_dismiss);
      String toolTipInitialDelay = i18n.text(StreamflowResources.tooltip_delay_initial);
      String toolTipReshowDelay = i18n.text(StreamflowResources.tooltip_delay_reshow);
      if (toolTipInitialDelay != null && !toolTipInitialDelay.trim().equals(""))
      {
         ToolTipManager.sharedInstance().setInitialDelay(Integer.parseInt(toolTipInitialDelay));
      }
      if (toolTipDismissDelay != null && !toolTipDismissDelay.trim().equals(""))
      {
         ToolTipManager.sharedInstance().setDismissDelay(Integer.parseInt(toolTipDismissDelay));
      }
      if (toolTipReshowDelay != null && !toolTipReshowDelay.trim().equals(""))
      {
         ToolTipManager.sharedInstance().setReshowDelay(Integer.parseInt(toolTipReshowDelay));
      }

      getContext().getActionMap().get("myProfile").setEnabled(false);


      this.accountSelector = accountSelector;
      this.workspaceWindow = obf.newObjectBuilder(WorkspaceWindow.class).use(accountSelector).newInstance();
      this.overviewWindow = obf.newObjectBuilder(OverviewWindow.class).use(accountSelector).newInstance();
      this.administrationWindow = obf.newObjectBuilder(AdministrationWindow.class).use(accountSelector).newInstance();
      this.debugWindow = obf.newObjectBuilder(DebugWindow.class).newInstance();
      setMainFrame(workspaceWindow.getFrame());

      this.accountsModel = accountsModel;

      showWorkspaceWindow();

      // Auto-select first account if only one available
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            if (accountsModel.getAccounts().size() == 1)
            {
               accountSelector.setSelectedIndex(0);

               if (openCaseJson != null)
               {
                  CaseDTO caseDTO = module.valueBuilderFactory().newValueFromJSON(CaseDTO.class, openCaseJson);
                  openCaseJson = null;
                  workspaceWindow.getCurrentWorkspace().openCase(caseDTO.caseId().get());
               }
            }
         }
      });

      accountSelector.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            StreamflowApplication.this.getContext().getActionMap().get("myProfile").setEnabled(!accountSelector.getSelectionModel().isSelectionEmpty());
         }
      });

      getContext().getActionMap().get("savePerspective").setEnabled(false);
      getContext().getActionMap().get("managePerspectives").setEnabled(false);
   }

   @Override
   protected void startup()
   {
      try
      {
         Client client = new Client(Protocol.HTTP);
         client.start();
         // Make it slower to get it more realistic
         Restlet restlet = new Filter(client.getContext(), client)
         {
            @Override
            protected int beforeHandle(Request request, Response response)
            {
               workspaceWindow.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

               return super.beforeHandle(request, response);
            }

            @Override
            protected void afterHandle(Request request, Response response)
            {
               workspaceWindow.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

               super.afterHandle(request, response);
            }
         };

         Energy4Java is = new Energy4Java();
         app = is.newApplication(new StreamflowClientAssembler(this,
                 org.jdesktop.application.Application.getInstance().getContext(),
                 restlet));

         logger.info("Starting in " + app.mode() + " mode");

         app.activate();
      } catch (Throwable e)
      {
         JXErrorPane.showDialog(getMainFrame(), new ErrorInfo(i18n.text(StreamflowResources.startup_error), e.getMessage(), null, "#error", e, java.util.logging.Level.SEVERE, Collections.<String, String>emptyMap()));
         shutdown();
      }

      streamflowLogger.info("Startup done");

   }

   // Menu actions

   @Uses
   private ObjectBuilder<AccountsDialog> accountsDialog;

   @Action
   public void manageAccounts()
   {
      ListItemValue selectedValue = (ListItemValue) accountSelector.getSelectedValue();
      AccountsDialog dialog = accountsDialog.use(accountsModel).newInstance();
      dialog.setSelectedAccount(selectedValue);
      dialogs.showOkDialog(getMainFrame(), dialog, text(AccountResources.account_title));
   }

   @Action
   public void selectAccount()
   {
      accountSelector.clearSelection();
      if (administrationWindow.getFrame().isVisible())
      {
         administrationWindow.getFrame().setVisible(false);
         overviewWindow.getFrame().setVisible(false);
      }
   }

   @Action
   public void myProfile()
   {
      ProfileView profile = obf.newObjectBuilder(ProfileView.class).use(accountSelector.getSelectedAccount().serverResource().getSubClient("account").getSubClient("profile")).newInstance();
      dialogs.showOkDialog(getMainFrame(), profile, text(AccountResources.profile_title));
   }

   @Action
   public void savePerspective(ActionEvent e)
   {
      ((ApplicationAction) getContext().getActionMap().get("savePerspective").getValue("proxy")).actionPerformed(e);
   }

   @Action
   public void managePerspectives(ActionEvent e)
   {
      ((ApplicationAction) getContext().getActionMap().get("managePerspectives").getValue("proxy")).actionPerformed(e);
   }

   public EventStream getSource()
   {
      return stream;
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      for (Window window : Frame.getWindows())
      {
         dispatchTransactions(window, transactions);
      }
   }

   private void dispatchTransactions(Component component, Iterable<TransactionDomainEvents> transactionEventsIterable)
   {
      if (!component.isShowing())
         return;

      if (component instanceof TransactionListener)
         ((TransactionListener) component).notifyTransactions(transactionEventsIterable);

      if (component instanceof Container)
      {
         Container container = (Container) component;
         for (Component childComponent : container.getComponents())
         {
            // Only dispatch to visible components - they will refresh once visible anyway
            dispatchTransactions(childComponent, transactionEventsIterable);
         }
      }
   }

   // Controller actions -------------------------------------------

   // Menu actions
   // Account menu

   @Action
   public void showWorkspaceWindow()
   {
      if (!workspaceWindow.getFrame().isVisible())
      {
         show(workspaceWindow);
      }
      workspaceWindow.getFrame().toFront();
   }

   @Action
   public void showOverviewWindow() throws Exception
   {
      if (!overviewWindow.getFrame().isVisible())
      {
         show(overviewWindow);
      }
      overviewWindow.getFrame().toFront();
   }

   @Action
   public void showAdministrationWindow() throws Exception
   {
      if (!administrationWindow.getFrame().isVisible())
         show(administrationWindow);
      administrationWindow.getFrame().toFront();
   }

   @Action
   public void showDebugWindow() throws Exception
   {
      if (!debugWindow.getFrame().isVisible())
         show(debugWindow);
      debugWindow.getFrame().toFront();
   }

   @Action
   public void close(ActionEvent e)
   {
      WindowUtils.findWindow((Component) e.getSource()).dispose();
   }

   @Action
   public void cancel(ActionEvent e)
   {
      WindowUtils.findWindow((Component) e.getSource()).dispose();
   }

   @Action
   public void showAbout()
   {
      dialogs.showOkDialog(getMainFrame(), new AboutDialog(getContext()));
   }

   @Action
   public void showHelp(ActionEvent event)
   {
      // Turn off java help for 1.0 release
      // javaHelp.init();
   }

   @Override
   public void exit(EventObject eventObject)
   {
      super.exit(eventObject);
   }

   @Override
   protected void shutdown()
   {
      try
      {
         SingleInstanceService singleInstanceService = (SingleInstanceService) ServiceManager.lookup(SingleInstanceService.class.getName());
         singleInstanceService.removeSingleInstanceListener(this);
      } catch (UnavailableServiceException e)
      {
         // Ignore
      }

      try
      {
         if (app != null)
            app.passivate();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @Override
   protected void show(JComponent jComponent)
   {
      super.show(jComponent);
   }
}