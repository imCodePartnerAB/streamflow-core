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
package se.streamsource.streamflow.web.domain.entity.casetype;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Notable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedResolutions;

/**
 * JAVADOC
 */
@Concerns(ResolutionEntity.RemovableConcern.class)
public interface ResolutionEntity
      extends DomainEntity,

      // Structure
      Resolution,
      Describable.Data,
      Notable.Data,
      Removable.Data
{
   abstract class RemovableConcern
      extends ConcernOf<Removable>
      implements Removable
   {
      @Structure
      Module module;

      @This
      Resolution resolution;

      public boolean removeEntity()
      {
         boolean removed = next.removeEntity();

         // Remove all usages of this case-type
         if (removed)
         {
            {
               SelectedResolutions.Data selectedResolutions = QueryExpressions.templateFor( SelectedResolutions.Data.class );
               Query<SelectedResolutions> resolutionUsages = module.queryBuilderFactory().newQueryBuilder(SelectedResolutions.class).
                     where(QueryExpressions.contains(selectedResolutions.selectedResolutions(), resolution)).
                     newQuery(module.unitOfWorkFactory().currentUnitOfWork());

               for (SelectedResolutions resolutionUsage : resolutionUsages)
               {
                  resolutionUsage.removeSelectedResolution( resolution );
               }
            }
         }

         return removed;
      }

      public void deleteEntity()
      {
         next.deleteEntity();
      }
   }
}