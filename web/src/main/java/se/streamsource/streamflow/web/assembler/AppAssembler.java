/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.assembler;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceSelector;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.rest.MBeanServerImporter;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import org.restlet.security.Verifier;
import se.streamsource.streamflow.infrastructure.event.replay.DomainEventPlayerService;
import se.streamsource.streamflow.web.application.console.ConsoleResultValue;
import se.streamsource.streamflow.web.application.console.ConsoleScriptValue;
import se.streamsource.streamflow.web.application.console.ConsoleService;
import se.streamsource.streamflow.web.application.mail.MailService;
import se.streamsource.streamflow.web.application.management.CompositeMBean;
import se.streamsource.streamflow.web.application.management.ErrorLogService;
import se.streamsource.streamflow.web.application.management.EventManagerService;
import se.streamsource.streamflow.web.application.management.LoggingService;
import se.streamsource.streamflow.web.application.management.ManagerComposite;
import se.streamsource.streamflow.web.application.management.ManagerService;
import se.streamsource.streamflow.web.application.management.ReindexOnStartupService;
import se.streamsource.streamflow.web.application.management.jmxconnector.JmxConnectorService;
import se.streamsource.streamflow.web.application.migration.StartupMigrationService;
import se.streamsource.streamflow.web.application.notification.NotificationService;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;
import se.streamsource.streamflow.web.application.organization.BootstrapDataService;
import se.streamsource.streamflow.web.application.organization.TestDataService;
import se.streamsource.streamflow.web.application.security.PasswordVerifierService;
import se.streamsource.streamflow.web.application.statistics.StatisticsService;

import javax.management.MBeanServer;

import static org.qi4j.api.common.Visibility.application;

/**
 * JAVADOC
 */
public class AppAssembler
{
   public void assemble( LayerAssembly layer)
         throws AssemblyException
   {
      console(layer.moduleAssembly( "Console" ));
      migration(layer.moduleAssembly( "Migration" ));

      if (layer.applicationAssembly().mode().equals( Application.Mode.production ))
      {
         management(layer.moduleAssembly( "Management" ));
         notification(layer.moduleAssembly( "Notification" ));
         mail(layer.moduleAssembly( "Mail" ));
      }

      security(layer.moduleAssembly( "Security" ));

      new BootstrapAssembler().assemble( layer.moduleAssembly( "Bootstrap" ) );

      statistics(layer.moduleAssembly( "Statistics" ));
   }


   private void mail( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( MailService.class ).identifiedBy( "mail" ).instantiateOnStartup().visibleIn( Visibility.application );
   }

   private void notification( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( NotificationService.class )
            .identifiedBy( "notification" )
            .instantiateOnStartup()
            .visibleIn( Visibility.layer );
   }

   private void statistics( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( StatisticsService.class ).
            identifiedBy( "statistics" ).
            instantiateOnStartup().
            visibleIn( Visibility.layer );
   }

   private void security( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( PasswordVerifierService.class );
      module.importServices( Verifier.class ).importedBy( NewObjectImporter.class ).visibleIn( application );
   }

   private void management( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( CompositeMBean.class );
      module.addTransients( ManagerComposite.class );

      module.importServices( MBeanServer.class ).importedBy( MBeanServerImporter.class );
      module.addServices( ManagerService.class ).visibleIn( Visibility.application ).instantiateOnStartup();

      module.addServices( JmxConnectorService.class ).identifiedBy( "jmxconnector" ).instantiateOnStartup();

      module.addServices( ReindexerService.class ).identifiedBy( "reindexer" ).visibleIn( Visibility.layer );
      module.addServices( ReindexOnStartupService.class ).instantiateOnStartup();

      module.addServices( EventManagerService.class, DomainEventPlayerService.class ).instantiateOnStartup();
      module.addServices( ErrorLogService.class ).instantiateOnStartup();

      module.addServices( LoggingService.class ).instantiateOnStartup();
   }

   private void migration( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.production ))
      {
         // Migrate state
         module.addServices( StartupMigrationService.class ).
               visibleIn( Visibility.application ).
               identifiedBy( "startupmigration" ).
               instantiateOnStartup();
      }
   }

   private void console( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues( ConsoleScriptValue.class, ConsoleResultValue.class ).visibleIn( Visibility.application );

      module.addServices( ConsoleService.class ).visibleIn( Visibility.application );
   }
}