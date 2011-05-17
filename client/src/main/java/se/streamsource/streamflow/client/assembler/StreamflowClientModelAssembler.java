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

package se.streamsource.streamflow.client.assembler;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.restlet.Client;
import org.restlet.data.Protocol;

/**
 * JAVADOC
 */
public class StreamflowClientModelAssembler
      implements ApplicationAssembler
{
   Object[] serviceObjects;

   public StreamflowClientModelAssembler(Object... serviceObjects)
   {
      this.serviceObjects = serviceObjects;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
      assembly.setName( "StreamflowClient" );
      assembly.setVersion( "0.1" );

      // Create layers
      LayerAssembly domainInfrastructureLayer = assembly.layer( "Client domain infrastructure" );
      LayerAssembly domainLayer = assembly.layer( "Domain" );
      LayerAssembly modelLayer = assembly.layer( "Model" );
      LayerAssembly restLayer = assembly.layer( "REST" );

      // Define layer usage
      domainLayer.uses( domainInfrastructureLayer, restLayer );
      modelLayer.uses(domainLayer, domainInfrastructureLayer, restLayer);
      restLayer.uses(domainInfrastructureLayer);

      // Assembler layers
      new ModelAssembler().assemble(modelLayer);
      new DomainAssembler().assemble( domainLayer );
      new InfrastructureAssembler().assemble( domainInfrastructureLayer );
      new RESTAssembler().assembler(restLayer);

      for (Object serviceObject : serviceObjects)
      {
         assembly.setMetaInfo( serviceObject );
      }

      return assembly;
   }
}