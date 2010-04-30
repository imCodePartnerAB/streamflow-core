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
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.context.structure.labels.LabelContext;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolutions;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;

/**
 * JAVADOC
 */
@Mixins(ResolutionsContext.Mixin.class)
public interface ResolutionsContext
   extends Interactions, IndexInteraction<LinksValue>,SubContexts<ResolutionContext>
{
   void createresolution( StringValue name );

   abstract class Mixin
      extends InteractionsMixin
      implements ResolutionsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         return new LinksBuilder(module.valueBuilderFactory()).rel( "resolution" ).addDescribables( context.get( Resolutions.class).getResolutions()).newLinks();
      }

      public void createresolution( StringValue name )
      {
         Resolutions resolutions = context.get(Resolutions.class);

         resolutions.createResolution( name.string().get() );
      }

      public ResolutionContext context( String id )
      {
         context.set(module.unitOfWorkFactory().currentUnitOfWork().get( Resolution.class, id ));

         return subContext( ResolutionContext.class );
      }
   }
}