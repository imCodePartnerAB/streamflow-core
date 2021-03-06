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
package se.streamsource.dci.api;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JAVADOC
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(Requires.RequiresRoleConstraint.class)
public @interface Requires
{
   Class<?>[] value();

   class RequiresRoleConstraint
      implements Constraint<Requires, RoleMap>
   {
      public boolean isValid( Requires requires, RoleMap roleMap )
      {
         for (Class<?> roleClass : requires.value())
         {
            try
            {
               roleMap.get( roleClass );
            } catch (IllegalArgumentException ex)
            {
               return false;
            }
         }
         return true;
      }
   }
}
