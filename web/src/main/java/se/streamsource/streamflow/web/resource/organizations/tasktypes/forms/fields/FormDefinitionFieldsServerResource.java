/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.fields;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.PageBreakFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.SingleSelectionFieldValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.form.FormEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms/{form}/fields
 */
public class FormDefinitionFieldsServerResource
      extends CommandQueryServerResource
{
   public ListValue fields()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );

      return new ListValueBuilder( vbf ).addDescribableItems( form.fields() ).newList();
   }

   public void add( CreateFieldDTO createFieldDTO )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );

      form.createField( createFieldDTO.name().get(), getFieldValue( createFieldDTO.fieldType().get() ) );
   }

   private FieldValue getFieldValue( FieldTypes fieldType )
   {
      FieldValue value = null;
      switch (fieldType)
      {
         case text:
            ValueBuilder<TextFieldValue> textBuilder = vbf.newValueBuilder( TextFieldValue.class );
            textBuilder.prototype().width().set( 30 );
            value = textBuilder.newInstance();
            break;
         case number:
            ValueBuilder<NumberFieldValue> numberBuilder = vbf.newValueBuilder( NumberFieldValue.class );
            numberBuilder.prototype().integer().set( true );
            value = numberBuilder.newInstance();
            break;
         case date:
            value = vbf.newValue( DateFieldValue.class );
            break;
         case single_selection:
            ValueBuilder<SingleSelectionFieldValue> single = vbf.newValueBuilder( SingleSelectionFieldValue.class );
            value = single.newInstance();
            break;
         case multi_selection:
            break;
         case comment:
            break;
         case page_break:
            value = vbf.newValue( PageBreakFieldValue.class);
            break;
      }
      return value;
   }
}