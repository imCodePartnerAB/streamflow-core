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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import static se.streamsource.dci.api.RoleMap.role;

import java.io.IOException;
import java.util.regex.Pattern;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.entity.form.DatatypeDefinitionEntity;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinition;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinitions;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeRegularExpression;

/**
 * JAVADOC
 */
public class DatatypeDefinitionContext implements IndexContext<DatatypeDefinitionEntity>, DeleteContext
{
   @Structure
   Module module;

   public DatatypeDefinitionEntity index()
   {
      return role( DatatypeDefinitionEntity.class );
   }

   public void delete() throws IOException
   {
      role( DatatypeDefinitions.class ).removeDatatypeDefinition( role( DatatypeDefinition.class ) );
   }

   public void changeregularexpression(@Optional @Name("regularexpression") String newExpression)
   {
      DatatypeRegularExpression regularexpression = RoleMap.role( DatatypeRegularExpression.class );
      if (newExpression != null)
      {
         Pattern.compile( newExpression );
      }
      regularexpression.changeRegularExpression( newExpression );
   }
}
