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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.form.PossibleFormMoveToQueries;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;

/**
 * JAVADOC
 */
@Mixins(FormsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface FormsContext
      extends SubContexts<FormContext>, IndexContext<LinksValue>, Context
{
   LinksValue possiblemoveto();

   void createform( @MaxLength(50) StringValue formName );

   abstract class Mixin
         extends ContextMixin
         implements FormsContext
   {
      public LinksValue index()
      {
         Forms.Data forms = roleMap.get( Forms.Data.class );

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "form" ).addDescribables( forms.forms() ).newLinks();
      }

      public LinksValue possiblemoveto()
      {
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
         builder.command( "move" );
         roleMap.get( PossibleFormMoveToQueries.class).possibleMoveFormTo( builder );
         return builder.newLinks();
      }

      public void createform( StringValue formName )
      {
         Forms forms = roleMap.get( Forms.class );
         Form form = forms.createForm();
         form.changeDescription( formName.string().get() );
      }

      public FormContext context( String id )
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, id ) );
         return subContext( FormContext.class );
      }
   }
}
