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

package se.streamsource.streamflow.web.context.structure.resolutions;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolutions;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedResolutions;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.Projects;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
@Mixins(SelectedResolutionsContext.Mixin.class)
public interface SelectedResolutionsContext
   extends SubContexts<SelectedResolutionContext>, IndexInteraction<LinksValue>, Interactions
{
   public LinksValue possibleresolutions();
   public void createresolution( StringValue name );
   public void addresolution( EntityReferenceDTO resolutionDTO );

   abstract class Mixin
         extends InteractionsMixin
         implements SelectedResolutionsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         SelectedResolutions.Data resolutions = context.get(SelectedResolutions.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "resolution" ).addDescribables( resolutions.selectedResolutions() ).newLinks();
      }

      public LinksValue possibleresolutions()
      {
         OrganizationQueries organizationQueries = context.get(OrganizationQueries.class);
         final SelectedResolutions.Data selectedResolutions = context.get(SelectedResolutions.Data.class);

         final LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command( "addresolution" );
         organizationQueries.visitOrganization( new OrganizationVisitor()
         {

            Describable owner;

            @Override
            public boolean visitOrganizationalUnit( OrganizationalUnit ou )
            {
               owner = ou;

               return super.visitOrganizationalUnit( ou );
            }

            @Override
            public boolean visitProject( Project project )
            {
               owner = project;

               return super.visitProject( project );
            }

            @Override
            public boolean visitCaseType( CaseType caseType )
            {
               owner = caseType;

               return super.visitCaseType( caseType );
            }

            @Override
            public boolean visitResolution( Resolution resolution )
            {
               if (!selectedResolutions.selectedResolutions().contains( resolution ))
                  builder.addDescribable( resolution, owner );

               return true;
            }
         }, new OrganizationQueries.ClassSpecification(
               OrganizationalUnits.class,
               OrganizationalUnit.class,
               Projects.class,
               CaseTypes.class,
               CaseType.class,
               Resolutions.class));
         return builder.newLinks();
      }

      public void createresolution( StringValue name )
      {
         Resolutions resolutions = context.get(Resolutions.class);
         SelectedResolutions selectedResolutions = context.get(SelectedResolutions.class);

         Resolution resolution = resolutions.createResolution( name.string().get() );
         selectedResolutions.addSelectedResolution( resolution );
      }

      public void addresolution( EntityReferenceDTO resolutionDTO )
      {
         SelectedResolutions resolutions = context.get( SelectedResolutions.class);
         Resolution resolution = module.unitOfWorkFactory().currentUnitOfWork().get( Resolution.class, resolutionDTO.entity().get().identity() );

         resolutions.addSelectedResolution( resolution );
      }

      public SelectedResolutionContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get(Resolution.class, id ));
         return subContext( SelectedResolutionContext.class );
      }
   }
}