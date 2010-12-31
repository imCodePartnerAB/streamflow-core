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

package se.streamsource.streamflow.web.context.workspace.table;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountDraftsConcern.class)
@Mixins(DraftsContext.Mixin.class)
public interface DraftsContext
      extends Context
{
   void createcase();

   abstract class Mixin
         implements DraftsContext
   {
      @Structure
      Module module;

      public Query<Case> cases(TableQuery tableQuery)
      {
         DraftsQueries inbox = role( DraftsQueries.class );

         QueryBuilder<Case> builder = inbox.drafts();
         Query<Case> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );

         return query;
      }

      public void createcase()
      {
         Drafts drafts = role( Drafts.class );
         drafts.createDraft();
      }
   }
}
