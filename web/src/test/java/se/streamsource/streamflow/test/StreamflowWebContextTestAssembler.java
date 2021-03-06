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
package se.streamsource.streamflow.test;

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchConfiguration;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionVisitor;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsConfiguration;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseConfiguration;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.assembler.StreamflowWebAssembler;
import se.streamsource.streamflow.web.configuration.ServiceConfiguration;

import java.util.Properties;

import static org.qi4j.api.common.Visibility.*;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class StreamflowWebContextTestAssembler
      extends StreamflowWebAssembler
{
   private TransactionVisitor transactionVisitor;


   public StreamflowWebContextTestAssembler( TransactionVisitor transactionVisitor )
   {
      this.transactionVisitor = transactionVisitor;
   }

   @Override
   protected void assembleApplicationLayer( LayerAssembly appLayer ) throws AssemblyException
   {
      appLayer.application().setMode( Application.Mode.test );
      appLayer.module( "Pdf" ).services( PdfGeneratorService.class ).visibleIn( application );
      new BootstrapAssembler().assemble( appLayer.module( "Bootstrap" ) );

      ApplicationAssembly applicationAssembly = appLayer.application();
      LayerAssembly layer1 = applicationAssembly.layer( "Layer 1" );
      layer1.uses(
            applicationAssembly.layer( "Context" ),
            applicationAssembly.layer( "Configuration" ),
            applicationAssembly.layer( "Domain" ) );
      ModuleAssembly module = layer1.module( "Module 1" );
      module.values( EntityValue.class );
      applicationAssembly.layer( "Domain" ).module( "Events" ).importedServices( TransactionVisitor.class ).visibleIn( Visibility.application ).setMetaInfo( transactionVisitor );

      ModuleAssembly knowledgebase = appLayer.module("Knowledgebase");
      Properties props = new Properties();
      try
      {
         props.load(getClass().getResourceAsStream("/velocity.properties"));

         VelocityEngine velocity = new VelocityEngine(props);

         knowledgebase.importedServices(VelocityEngine.class)
                 .importedBy(INSTANCE).setMetaInfo(velocity);

      } catch (Exception e)
      {
         throw new AssemblyException("Could not load velocity properties", e);
      }

      knowledgebase.services(KnowledgebaseService.class).identifiedBy("knowledgebase").visibleIn(Visibility.application);
      ModuleAssembly system = appLayer.module( "System" );
      system.services( SystemDefaultsService.class ).identifiedBy( "system" ).visibleIn( Visibility.application);
      ModuleAssembly configurationModule = module.layer().application().layer("Configuration").module("Configuration");
      configurationModule.entities( KnowledgebaseConfiguration.class, SystemDefaultsConfiguration.class).visibleIn( Visibility.application );
      configurationModule.forMixin( SystemDefaultsConfiguration.class ).declareDefaults().enabled().set( Boolean.TRUE );
      configurationModule.forMixin( SystemDefaultsConfiguration.class ).declareDefaults().sortOrderAscending().set( false );
      configurationModule.forMixin( SystemDefaultsConfiguration.class ).declareDefaults().webFormsProxyUrl().set( "https://localhost:8443/surface" );
      configurationModule.forMixin( SystemDefaultsConfiguration.class ).declareDefaults().defaultMarkReadTimeout().set( 15L );
   }

   @Override
   protected void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {
   }

   @Override
   protected void assembleManagementLayer( LayerAssembly managementLayer ) throws AssemblyException
   {
   }
}