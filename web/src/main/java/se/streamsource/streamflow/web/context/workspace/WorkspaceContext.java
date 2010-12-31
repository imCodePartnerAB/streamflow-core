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

package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.web.domain.entity.user.SearchCaseQueries;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

/**
 * JAVADOC
 */
public class WorkspaceContext
{
   public Query<Case> search( TableQuery tableQuery)
   {
      SearchCaseQueries caseQueries = RoleMap.role( SearchCaseQueries.class );
      Query<Case> caseQuery = caseQueries.search( tableQuery.where() );
      caseQuery.orderBy( QueryExpressions.orderBy( QueryExpressions.templateFor( Describable.Data.class ).description() ) );

      return caseQuery;
   }
}
