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

package se.streamsource.streamflow.web.context.administration.labels;

import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.qi4j.library.constraints.annotation.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.web.domain.structure.label.*;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
@Mixins(LabelsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface LabelsContext
      extends Context, IndexContext<Iterable<Label>>
{
   void createlabel( @MaxLength(50) StringValue name );

   abstract class Mixin
         implements LabelsContext
   {
      @Structure
      Module module;

      public Iterable<Label> index()
      {
         return role( Labels.Data.class ).labels();
      }

      public void createlabel( StringValue name )
      {
         Labels labels = role( Labels.class );

         labels.createLabel( name.string().get() );
      }
   }
}
