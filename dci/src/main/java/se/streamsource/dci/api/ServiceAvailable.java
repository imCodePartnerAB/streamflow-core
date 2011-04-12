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

package se.streamsource.dci.api;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.service.*;

import java.lang.annotation.*;

/**
 * Annotate interaction methods with ServiceAvailable. They will only be valid
 * if a service with the given type is available.
 */
@InteractionConstraintDeclaration(ServiceAvailable.ServiceAvailableConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceAvailable
{
   Class value();

   public class ServiceAvailableConstraint
         implements InteractionConstraint<ServiceAvailable>
   {
      @Structure
      ServiceFinder finder;

      public boolean isValid( ServiceAvailable serviceAvailable, RoleMap roleMap )
      {
         ServiceReference ref = finder.findService( serviceAvailable.value() );
         return ref != null && ref.isAvailable();
      }
   }
}