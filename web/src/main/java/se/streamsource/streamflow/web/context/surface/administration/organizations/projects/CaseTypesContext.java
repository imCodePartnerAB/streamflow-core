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

package se.streamsource.streamflow.web.context.surface.administration.organizations.projects;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;

/**
 * JAVADOC
 */
@Mixins(CaseTypesContext.Mixin.class)
public interface CaseTypesContext
   extends SubContexts<LabelsContext>, IndexInteraction<LinksValue>, Interactions
{
   abstract class Mixin
      extends InteractionsMixin
      implements CaseTypesContext
   {
      public LinksValue index()
      {
         SelectedCaseTypes.Data data = context.get( SelectedCaseTypes.Data.class );
         Describable describable = context.get( Describable.class );

         TitledLinksBuilder builder = new TitledLinksBuilder( module.valueBuilderFactory() );

         builder.addDescribables( data.selectedCaseTypes() );
         builder.addTitle( describable.getDescription() );

         return builder.newLinks();
      }

      public LabelsContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get( CaseType.class, id ) );

         return subContext( LabelsContext.class);
      }
   }
}