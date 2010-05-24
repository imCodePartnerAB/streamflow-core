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

package se.streamsource.streamflow.web.infrastructure.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.Qi4j;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.restlet.Restlet;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.server.plugin.ContactLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * JAVADOC
 */
public class OSGiServiceExporterAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( OSGiExporterService.class ).instantiateOnStartup();
   }

   @Mixins(OSGiExporterService.Mixin.class)
   interface OSGiExporterService
         extends ServiceComposite, Activatable
   {
      class Mixin
            implements Activatable
      {
         @Service
         Iterable<ServiceReference<Object>> services;

         List<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

         public void activate() throws Exception
         {
            BundleContext context = BundleReference.class.cast( OSGiServiceExporterAssembler.class.getClassLoader() ).getBundle().getBundleContext();

            // Export services
            for (ServiceReference service : services)
            {
               Object serviceInstance = service.get();
               Set<Class> classes = Classes.classesOf( serviceInstance.getClass() );
               String[] clazzes = new String[classes.size()];
               int idx = 0;
               for (Class aClass : classes)
               {
                  clazzes[idx++] = aClass.getName();
               }

               Properties properties = new Properties();
               properties.setProperty( Constants.SERVICE_ID, service.identity() );

               registrations.add( context.registerService( clazzes, serviceInstance, properties ) );
               LoggerFactory.getLogger( getClass() ).info( "Exported service:" + service.identity() );
            }
         }

         public void passivate() throws Exception
         {
            for (ServiceRegistration registration : registrations)
            {
               registration.unregister();
            }
            registrations.clear();
         }
      }
   }

}