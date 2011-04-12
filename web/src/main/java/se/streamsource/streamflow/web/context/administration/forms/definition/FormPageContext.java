/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.administration.forms.definition;

import org.qi4j.api.constraint.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.domain.structure.form.*;

/**
 * JAVADOC
 */
public class FormPageContext
      implements IndexContext<PageDefinitionValue>
{
   @Structure
   Module module;

   public PageDefinitionValue index()
   {
      Describable describable = RoleMap.role( Describable.class );
      Identity identity = RoleMap.role( Identity.class );

      ValueBuilder<PageDefinitionValue> builder = module.valueBuilderFactory().newValueBuilder( PageDefinitionValue.class );
      builder.prototype().description().set( describable.getDescription() );
      builder.prototype().page().set( EntityReference.parseEntityReference( identity.identity().get() ) );
      return builder.newInstance();
   }

   public void move( StringValue direction )
   {
      Page page = RoleMap.role( Page.class );
      Pages.Data pagesData = RoleMap.role( Pages.Data.class );
      Pages pages = RoleMap.role( Pages.class );

      int index = pagesData.pages().toList().indexOf( page );
      if (direction.string().get().equalsIgnoreCase( "up" ))
      {
         try
         {
            pages.movePage( page, index - 1 );
         } catch (ConstraintViolationException e)
         {
         }
      } else
      {
         pages.movePage( page, index + 1 );
      }
   }

   public void delete()
   {
      Page pageEntity = RoleMap.role( Page.class );
      Pages form = RoleMap.role( Pages.class );

      form.removePage( pageEntity );
   }

   public void create( CreateFieldDTO createFieldDTO )
   {
      Fields fields = RoleMap.role( Fields.class );

      fields.createField( createFieldDTO.name().get(), getFieldValue( createFieldDTO.fieldType().get() ) );
   }

   private FieldValue getFieldValue( FieldTypes fieldType )
   {
      FieldValue value = null;
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      switch (fieldType)
      {
         case attachment:
            value = vbf.newValue( AttachmentFieldValue.class );
            break;
         case checkboxes:
            value = vbf.newValue( CheckboxesFieldValue.class );
            break;
         case combobox:
            value = vbf.newValue( ComboBoxFieldValue.class );
            break;
         case comment:
            value = vbf.newValue( CommentFieldValue.class );
            break;
         case date:
            value = vbf.newValue( DateFieldValue.class );
            break;
         case listbox:
            value = vbf.newValue( ListBoxFieldValue.class );
            break;
         case number:
            ValueBuilder<NumberFieldValue> numberBuilder = vbf.newValueBuilder( NumberFieldValue.class );
            numberBuilder.prototype().integer().set( true );
            value = numberBuilder.newInstance();
            break;
         case optionbuttons:
            value = vbf.newValue( OptionButtonsFieldValue.class );
            break;
         case openselection:
            ValueBuilder<OpenSelectionFieldValue> valueBuilder = vbf.newValueBuilder( OpenSelectionFieldValue.class );
            valueBuilder.prototype().openSelectionName().set( "" );
            value = valueBuilder.newInstance();
            break;
         case textarea:
            ValueBuilder<TextAreaFieldValue> builder = vbf.newValueBuilder( TextAreaFieldValue.class );
            builder.prototype().cols().set( 30 );
            builder.prototype().rows().set( 5 );
            value = builder.newInstance();
            break;
         case text:
            ValueBuilder<TextFieldValue> textBuilder = vbf.newValueBuilder( TextFieldValue.class );
            textBuilder.prototype().width().set( 30 );
            value = textBuilder.newInstance();
            break;
      }
      return value;
   }
}
