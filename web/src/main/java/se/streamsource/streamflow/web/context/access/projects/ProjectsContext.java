/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.projects;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.web.context.access.organizations.AccessPointsContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(ProjectsContext.Mixin.class)
public interface ProjectsContext
   extends Context
{
   @SubContext
   ProjectContext projects();

   @SubContext
   AccessPointsContext accesspoints();

   abstract class Mixin
      extends ContextMixin
      implements ProjectsContext
   {

      public AccessPointsContext accesspoints()
      {
         return subContext( AccessPointsContext.class );
      }

      public ProjectContext projects( )
      {
         return subContext( ProjectContext.class);
      }
   }
}