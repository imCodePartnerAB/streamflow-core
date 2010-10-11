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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.util.Specification;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypesQueries;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.dci.api.SubContexts;

/**
 * JAVADOC
 */
@Mixins(SelectedCaseTypesContext.Mixin.class)
public interface SelectedCaseTypesContext
   extends SubContexts<SelectedCaseTypeContext>, IndexContext<LinksValue>, Context
{
   public LinksValue possiblecasetypes();

   public void addcasetype( EntityReferenceDTO caseTypeDTO );

   abstract class Mixin
      extends ContextMixin
      implements SelectedCaseTypesContext
   {
      public LinksValue index()
      {
         SelectedCaseTypes.Data caseTypes = roleMap.get( SelectedCaseTypes.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel("casetype").addDescribables( caseTypes.selectedCaseTypes() ).newLinks();
      }

      public LinksValue possiblecasetypes()
      {
         final SelectedCaseTypes.Data selectedLabels = roleMap.get( SelectedCaseTypes.Data.class);
         CaseTypesQueries caseTypes = roleMap.get( CaseTypesQueries.class);
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "addcasetype" );
         caseTypes.caseTypes( builder, new Specification<CaseType>()
         {
            public boolean valid( CaseType instance )
            {
               return !selectedLabels.selectedCaseTypes().contains( instance );
            }
         });
         return builder.newLinks();
      }

      public void addcasetype( EntityReferenceDTO caseTypeDTO )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         SelectedCaseTypes caseTypes = roleMap.get( SelectedCaseTypes.class);
         CaseType caseType = uow.get( CaseType.class, caseTypeDTO.entity().get().identity() );

         caseTypes.addSelectedCaseType( caseType );
      }

      public SelectedCaseTypeContext context( String id )
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( CaseType.class, id ));
         return subContext( SelectedCaseTypeContext.class );
      }
   }
}
