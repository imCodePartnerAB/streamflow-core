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

package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.common.*;
import org.qi4j.api.concern.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.dci.api.*;
import se.streamsource.streamflow.web.domain.entity.gtd.*;
import se.streamsource.streamflow.web.infrastructure.caching.*;

/**
 * JAVADOC
 */
public abstract class UpdateCaseCountDraftsConcern
        extends ConcernOf<DraftsContext>
        implements DraftsContext
{
   @Optional
   @Service
   CachingService caching;

   @Structure
   UnitOfWorkFactory uowf;

   public void createcase()
   {
      Drafts drafts = RoleMap.role(Drafts.class);

      // Update drafts for user
      new Caching(caching, Caches.CASECOUNTS).addToCache(drafts.toString(), 1);

      next.createcase();
   }
}