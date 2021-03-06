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
package se.streamsource.streamflow.web.domain.entity.caze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypesQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */

@Mixins(CaseTypeQueries.Mixin.class)
public interface CaseTypeQueries
{
   void possibleCaseTypes( LinksBuilder builder );

   List<Project> possibleProjects();

   class Mixin
         implements CaseTypeQueries
   {
      @Structure
      Module module;

      @This
      CreatedOn created;

      @This
      Ownable.Data ownable;

      @This
      TypedCase.Data typedCase;

      public void possibleCaseTypes( final LinksBuilder builder )
      {
         Owner owner = ownable.owner().get();
         if (owner == null)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) created.createdBy().get();

            for (final Organization organization : orgs.organizations())
            {
               OrganizationQueries orgQueries = (OrganizationQueries) organization;

               // Find out what case-types have been selected
               final Set<CaseType> selectedCaseTypes = new HashSet<CaseType>();
               orgQueries.visitOrganization( new OrganizationVisitor()
               {
                  @Override
                  public boolean visitSelectedCaseType( CaseType caseType )
                  {
                     selectedCaseTypes.add( caseType );

                     return super.visitSelectedCaseType( caseType );
                  }
               }, new OrganizationQueries.ClassSpecification(
                     OrganizationalUnits.class,
                     OrganizationalUnit.class,
                     Projects.class,
                     SelectedCaseTypes.class
               ) );

               orgQueries.visitOrganization( new OrganizationVisitor()
               {
                  Describable owner;
                  StringBuilder string = new StringBuilder();

                  @Override
                  public boolean visitOrganization( Organization org )
                  {
                     owner = org;
                     return super.visitOrganization( org );
                  }

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
                     if (selectedCaseTypes.contains( caseType ))
                     {
                        // Build up list of labels as classes
                        string.setLength( 0 );
                        SelectedLabels.Data selectedLabels = (SelectedLabels.Data) caseType;
                        for (Label label : selectedLabels.selectedLabels())
                        {
                           string.append( label.getDescription() ).append( ' ' );
                        }

                        builder.addDescribable( caseType, owner.getDescription(), string.toString() );
                     }

                     return super.visitCaseType( caseType );
                  }
               }, new OrganizationQueries.ClassSpecification(
                     Organization.class,
                     OrganizationalUnits.class,
                     OrganizationalUnit.class,
                     Projects.class,
                     Project.class,
                     CaseTypes.class,
                     CaseType.class
               ) );
            }
         } else
         {
            // Show only Case types from owning project
            SelectedCaseTypes.Data selectedCaseTypes = (SelectedCaseTypes.Data) owner;
            Describable describableOwner = (Describable) owner;
            StringBuilder string = new StringBuilder();
            for (CaseType caseType : selectedCaseTypes.selectedCaseTypes())
            {
               // Build up list of labels as classes
               string.setLength( 0 );
               SelectedLabels.Data selectedLabels = (SelectedLabels.Data) caseType;
               for (Label label : selectedLabels.selectedLabels())
               {
                  string.append( label.getDescription() ).append( ' ' );
               }

               builder.addDescribable( caseType, describableOwner.getDescription(), string.toString() );
            }
         }
      }

      public List<Project> possibleProjects()
      {
         Owner owner = ownable.owner().get();

         if (owner == null)
         {
            Creator creator = created.createdBy().get();

            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) creator;

            List<Project> projects = new ArrayList<Project>();

            for (Organization organization : orgs.organizations())
            {
               CaseTypesQueries caseTypesQueries = (CaseTypesQueries) organization;
               QueryBuilder<Project> builder = caseTypesQueries.possibleProjects( typedCase.caseType().get() );
               Query<Project> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
               for (Project project : query)
               {
                  // dont allow duplicates
                  if (!projects.contains( project ))
                     projects.add( project );
               }
            }

            return projects;
         } else if (owner instanceof OwningOrganizationalUnit.Data)
         {
            OwningOrganizationalUnit.Data ouOwner = (OwningOrganizationalUnit.Data) owner;
            OrganizationalUnit ou = ouOwner.organizationalUnit().get();
            CaseTypesQueries org = (CaseTypesQueries) ((OwningOrganization) ou).organization().get();

            List<Project> projects = new ArrayList<Project>();
            QueryBuilder<Project> builder = org.possibleProjects( typedCase.caseType().get() );
            Query<Project> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
            for (Project project : query)
            {
               projects.add( project );
            }

            return projects;
         } else
         {
            return Collections.emptyList();
         }
      }
   }
}
