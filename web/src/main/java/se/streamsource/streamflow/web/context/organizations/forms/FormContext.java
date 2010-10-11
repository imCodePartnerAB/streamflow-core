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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.PossibleFormMoveToQueries;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormId;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;

/**
 * JAVADOC
 */
@Mixins(FormContext.Mixin.class)
public interface FormContext
   extends DeleteContext, DescribableContext, NotableContext, Context
{
   FormValue form();

   void move( EntityValue to);

   public void changeformid( StringValue stringValue );

   @SubContext
   FormPagesContext pages();

   abstract class Mixin
      extends ContextMixin
      implements FormContext
   {
      @Structure
      Module module;

      public FormValue form()
      {
         FormEntity form = roleMap.get(FormEntity.class);

         ValueBuilder<FormValue> builder = module.valueBuilderFactory().newValueBuilder( FormValue.class );

         builder.prototype().note().set( form.note().get() );
         builder.prototype().description().set( form.description().get() );
         builder.prototype().form().set( EntityReference.parseEntityReference( form.identity().get() ) );
         builder.prototype().id().set( form.formId().get() );

         return builder.newInstance();
      }

      public void move(EntityValue to)
      {
         Forms toForms = module.unitOfWorkFactory().currentUnitOfWork().get( Forms.class, to.entity().get() );
         Form form = roleMap.get(Form.class);
         roleMap.get( Forms.class ).moveForm(form, toForms);
      }

      public void changeformid( StringValue stringValue )
      {
         FormId form = roleMap.get( FormId.class );
         form.changeFormId( stringValue.string().get() );
      }

      public void delete()
      {
         Form form = roleMap.get( Form.class);
         Forms forms = roleMap.get(Forms.class);
         forms.removeForm( form );
      }

      public FormPagesContext pages()
      {
         return subContext( FormPagesContext.class );
      }
   }
}
