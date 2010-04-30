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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolutions;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedResolutions;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * JAVADOC
 */
@Mixins(ResolutionContext.Mixin.class)
public interface ResolutionContext
      extends DescribableContext,
      DeleteInteraction,
      Interactions
{
   LinksValue usages();

   abstract class Mixin
         extends InteractionsMixin
         implements ResolutionContext
   {
      public LinksValue usages()
      {
         Query<SelectedResolutions> usageQuery = context.get( Resolutions.class).usages( context.get( Resolution.class) );
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()); // TODO What to use for path here?
         for (SelectedResolutions selectedResolutions : usageQuery)
         {
            builder.addDescribable( (Describable) selectedResolutions );
         }

         return builder.newLinks();
      }

      public void delete()
      {
         Resolutions resolutions = context.get( Resolutions.class );
         Resolution resolution = context.get( Resolution.class );

         resolutions.removeResolution( resolution );
      }
   }
}