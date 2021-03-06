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
package se.streamsource.streamflow.web.domain.interaction.security;

import static java.util.Arrays.binarySearch;
import static se.streamsource.streamflow.web.domain.interaction.security.CaseAccessType.values;

import java.util.Map;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Management of case access rules.
 */
@Mixins(CaseAccess.Mixin.class)
public interface CaseAccess
{
   void changeAccess(PermissionType permissionType, CaseAccessType accessType);

   void clearAccess();
   
   CaseAccessType getAccessType(PermissionType permissionType);

   interface Data
   {
      @UseDefaults
      Property<Map<PermissionType, CaseAccessType>> accessPermissions();

      void changedAccess(@Optional DomainEvent event, PermissionType permissionType, CaseAccessType accessType);
      
      void clearedAccess(@Optional DomainEvent event);
   }

   abstract class Mixin
      implements CaseAccess, Data
   {

      public void changeAccess( PermissionType permissionType, CaseAccessType accessType )
      {
         CaseAccessType current = getAccessType( permissionType );

         if (binarySearch( values(), accessType ) > binarySearch( values(), current ))
         {
            changedAccess( null, permissionType, accessType );
         }
      }

      public void changedAccess( @Optional DomainEvent event, PermissionType permissionType, CaseAccessType accessType )
      {
         Map<PermissionType, CaseAccessType> permissionAccess = accessPermissions().get();

         permissionAccess.put( permissionType, accessType );

         accessPermissions().set( permissionAccess );
      }

      public void clearAccess()
      {
         clearedAccess( null );
      }
      
      public void clearedAccess(@Optional DomainEvent event)
      {
         accessPermissions().get().clear();
      }
      
      public CaseAccessType getAccessType( PermissionType permissionType )
      {
         CaseAccessType current = accessPermissions().get().get( permissionType );

         if (current == null)
            current = CaseAccessType.all;

         return current;
      }
   }
}
