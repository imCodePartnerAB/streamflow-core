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
package se.streamsource.streamflow.web.context.administration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.FormValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresPriorityVisibility;
import se.streamsource.streamflow.web.domain.structure.casetype.PriorityOnCase;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.organization.Priorities;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;
import se.streamsource.streamflow.web.domain.structure.organization.PrioritySettings;

/**
 * Context for case priority settings.
 */
@Mixins(PriorityOnCaseContext.Mixin.class)
public interface PriorityOnCaseContext
      extends Context, IndexContext<FormValue>
{

   @RequiresPriorityVisibility
   void updatemandatory( @Name("mandatory") Boolean mandatory );

   void updatevisibility( @Name("visible") Boolean visible );

   void defaultpriority( @Optional @Name("id") String id );

   LinksValue priorities();

   abstract class Mixin implements PriorityOnCaseContext, IndexContext<FormValue>
   {
      @Structure
      Module module;

      @Uses
      PriorityOnCase priorityOnCase;

      @Uses
      PriorityOnCase.Data priorityOnCaseData;

      public FormValue index()
      {
         ValueBuilder<FormValue> builder = module.valueBuilderFactory().newValueBuilder( FormValue.class );
         builder.prototype().form().get().put( "visible", priorityOnCaseData.visible().get().toString() );
         builder.prototype().form().get().put( "mandatory", priorityOnCaseData.mandatory().get().toString() );
         builder.prototype().form().get().put( "prioritydefault", priorityOnCaseData.defaultPriority().get() != null
               ? EntityReference.getEntityReference( priorityOnCaseData.defaultPriority().get() ).identity() : "-1" );
         return builder.newInstance();
      }

      public void updatevisibility( Boolean visible )
      {
         priorityOnCase.changeVisibility( visible );
      }

      public void updatemandatory( Boolean mandatory )
      {
         priorityOnCase.changeMandatory( mandatory );
      }

      public void defaultpriority( String id )
      {
         if( id != null )
         {
            priorityOnCase.changeDefaultPriority( module.unitOfWorkFactory().currentUnitOfWork().get( Priority.class, id ) );
         } else
            priorityOnCase.changeDefaultPriority( null );
      }

      public LinksValue priorities()
      {
         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         Organization org = orgs.organization().get();
         RoleMap.current().set( org );

         Priorities.Data priorities = RoleMap.role( Priorities.Data.class );
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         builder.command( "prioritydefault" );

         ValueBuilder<PriorityValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( PriorityValue.class );

         linkBuilder.prototype().text().set( "-" );
         linkBuilder.prototype().id().set( "-1" );
         linkBuilder.prototype().href().set( "" );
         linkBuilder.prototype().priority().set( -1 );
         builder.addLink( linkBuilder.newInstance() );

         List<Priority> sortedList =  priorities.prioritys().toList();
         Collections.sort( sortedList, new Comparator<Priority>()
         {
            public int compare( Priority o1, Priority o2 )
            {
               return ((PrioritySettings.Data) o1).priority().get().compareTo( ((PrioritySettings.Data) o2).priority().get() );
            }
         } );

         for(Priority priority : sortedList )
         {
            String id = EntityReference.getEntityReference( priority ).identity();
            linkBuilder.prototype().priority().set( ((PrioritySettings.Data)priority).priority().get() );
            linkBuilder.prototype().color().set( ((PrioritySettings.Data)priority).color().get() );
            linkBuilder.prototype().id().set( id );
            linkBuilder.prototype().text().set( priority.getDescription() );
            linkBuilder.prototype().href().set( "prioritydefault" );

            builder.addLink( linkBuilder.newInstance() );
         }
         return builder.newLinks();
       }

   }
}
