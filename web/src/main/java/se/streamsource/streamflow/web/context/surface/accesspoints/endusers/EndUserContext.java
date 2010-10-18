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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.CaseFormDTO;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

/**
 * JAVADOC
 */
@Mixins(EndUserContext.Mixin.class)
public interface EndUserContext
      extends SubContexts<CaseContext>, Context, IndexContext<LinksValue>
{
   // command
   void createcase( );

   void createcasewithform( );

   // query
   CaseFormDTO findcasewithform();

   abstract class Mixin
      extends ContextMixin
      implements EndUserContext
   {

      public LinksValue index()
      {
         DraftsQueries draftsQueries = roleMap.get( DraftsQueries.class );
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.addDescribables( draftsQueries.drafts().newQuery( module.unitOfWorkFactory().currentUnitOfWork() ));
         return linksBuilder.newLinks();
      }

      public void createcase( )
      {
         AnonymousEndUser endUser = roleMap.get( AnonymousEndUser.class );
         EndUserCases endUserCases = roleMap.get( EndUserCases.class );
         endUserCases.createCase( endUser );
      }

      public void createcasewithform()
      {
         AnonymousEndUser endUser = roleMap.get( AnonymousEndUser.class );
         EndUserCases endUserCases = roleMap.get( EndUserCases.class );
         endUserCases.createCaseWithForm( endUser );
      }

      public CaseContext context( String id)
      {
         CaseEntity caseEntity = module.unitOfWorkFactory().currentUnitOfWork().get( CaseEntity.class, id );
         roleMap.set( caseEntity );
         return subContext( CaseContext.class );
      }

      public CaseFormDTO findcasewithform()
      {
         ValueBuilder<CaseFormDTO> builder = module.valueBuilderFactory().newValueBuilder( CaseFormDTO.class );
         DraftsQueries queries = roleMap.get( DraftsQueries.class );
         Query<Case> query = queries.drafts().newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         for (Case aCase : query)
         {
            SelectedForms.Data data = roleMap.get( SelectedForms.Data.class );
            Form form = data.selectedForms().get( 0 );
            FormDraft formSubmission = aCase.getFormSubmission( form );
            if ( formSubmission != null )
            {
               builder.prototype().caze().set( EntityReference.getEntityReference( aCase ));
               builder.prototype().form().set( EntityReference.getEntityReference( formSubmission ));
               return builder.newInstance();
            }
         }

         return builder.newInstance();
      }
   }
}