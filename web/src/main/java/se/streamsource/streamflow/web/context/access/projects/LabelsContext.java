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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(LabelsContext.Mixin.class)
public interface LabelsContext
   extends SubContexts<LabelsContext>, IndexContext<LinksValue>, Context
{
   void createaccesspoint( StringDTO name );

   abstract class Mixin
      extends ContextMixin
      implements LabelsContext
   {
      public LinksValue index()
      {
         ProjectLabelsQueries labelsQueries = context.role( ProjectLabelsQueries.class );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         Map<Label,SelectedLabels> map = labelsQueries.possibleLabels( context.role( TaskType.class ) );

         try
         {
            List<Label> labels = context.role( List.class );
            for (Label label : map.keySet())
            {
               if ( !labels.contains( label ) )
               {
                  linksBuilder.addDescribable( label );
               }
            }
         } catch (IllegalArgumentException e)
         {
            linksBuilder.addDescribables( map.keySet() );
         }

         return linksBuilder.newLinks();
      }

      public void createaccesspoint( StringDTO name )
      {
         Project project = context.role( Project.class );
         TaskType taskType = context.role( TaskType.class );
         List<Label> labels = null;
         try
         {
            labels = context.role( List.class );
         } catch (IllegalArgumentException e)
         {

         }

         AccessPoints accessPoints = context.role( AccessPoints.class );
         accessPoints.createAccessPoint( name.string().get(), project, taskType, labels );
      }

      public LabelsContext context( String id )
      {
         List<Label> labels;
         try
         {
            labels = context.role( List.class );
         } catch (IllegalArgumentException e)
         {
            labels = new ArrayList<Label>();
         }

         labels.add( module.unitOfWorkFactory().currentUnitOfWork().get( Label.class, id ) );

         context.playRoles( labels );

         return subContext( LabelsContext.class);
      }
   }
}