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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.CreateFieldDTO;
import se.streamsource.streamflow.api.administration.form.CreateFieldGroupDTO;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldTypes;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.administration.form.PageDefinitionValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroup;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroupValue;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroups;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.domain.structure.form.Page;
import se.streamsource.streamflow.web.domain.structure.form.Pages;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JAVADOC
 */
public class FormPageContext
      implements IndexContext<PageDefinitionValue>
{
   @Structure
   Module module;

   private static final String field_group = "field_group";
   private static final String fieldgroup_group = "fieldgroup_group";
   
   public PageDefinitionValue index()
   {
      Describable describable = RoleMap.role( Describable.class );
      Identity identity = RoleMap.role( Identity.class );
      Page page = RoleMap.role(Page.class);

      ValueBuilder<PageDefinitionValue> builder = module.valueBuilderFactory().newValueBuilder( PageDefinitionValue.class );
      builder.prototype().description().set( describable.getDescription() );
      builder.prototype().page().set( EntityReference.parseEntityReference( identity.identity().get() ) );

      builder.prototype().rule().set( page.getRule() );

      return builder.newInstance();
   }

   public void move( @Name("direction") String direction )
   {
      Page page = RoleMap.role( Page.class );
      Pages.Data pagesData = RoleMap.role( Pages.Data.class );
      Pages pages = RoleMap.role( Pages.class );

      int index = pagesData.pages().toList().indexOf( page );
      if (direction.equalsIgnoreCase( "up" ))
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

   public void createfieldgroup(CreateFieldGroupDTO createFieldGroupDTO ) {
      Fields fields = RoleMap.role( Fields.class );

      ValueBuilder<FieldGroupFieldValue> builder = module.valueBuilderFactory().newValueBuilder( FieldGroupFieldValue.class );
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
      FieldGroup fieldGroup = uow.get( FieldGroup.class, createFieldGroupDTO.fieldGroup().get() );
      builder.prototype().fieldGroup().set( EntityReference.getEntityReference( fieldGroup) );
      
      fields.createField( createFieldGroupDTO.name().get(), builder.newInstance() );
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
         case geolocation:
            value = vbf.newValue( GeoLocationFieldValue.class );
            break;
      }
      return value;
   }
   

   public LinksValue possiblefields()
   {
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      
      ResourceBundle bundle = ResourceBundle.getBundle( FormPageContext.class.getName(), RoleMap.role( Locale.class ) );
      
      for (FieldTypes  fieldType : FieldTypes.values())
      {
         builder.addLink( bundle.getString( fieldType.toString()), fieldType.toString(), "createfield", "create", "field", bundle.getString( field_group ) );
      }
      List<FieldGroup> fieldGroups = RoleMap.role( FieldGroups.Data.class ).fieldGroups().toList();
      for (FieldGroup fieldGroup : fieldGroups)
      {
         builder.addLink( fieldGroup.getDescription(), EntityReference.getEntityReference( fieldGroup).identity(), "createfieldgroup", "createfieldgroup", "fieldgroup", bundle.getString( fieldgroup_group ) );
      }

      return builder.newLinks();
   }

   @FirstPage(false)
   public LinksValue possiblerulefields()
   {
      final Pages.Data pages = RoleMap.role( Pages.Data.class );
      Page page = RoleMap.role( Page.class );

      final int index = pages.pages().toList().indexOf( page );

      Iterable<Field> possiblefields = Iterables.filter( new Specification<Field>()
      {
         public boolean satisfiedBy( Field field )
         {
            FieldValue fieldValue = ((FieldValueDefinition.Data) field).fieldValue().get();
            boolean filter = fieldValue instanceof FieldGroupValue
                  || fieldValue instanceof CommentFieldValue
                  || fieldValue instanceof TextAreaFieldValue
                  || fieldValue instanceof AttachmentFieldValue;
            return !filter;
         }
      }, Iterables.flatten( Iterables.map( new Function<Page, Iterable<Field>>()
      {
         public Iterable<Field> map( Page page )
         {
            return ((Fields.Data) page).fields().toList();
         }
      }, Iterables.filter( new Specification<Page>()
      {
         public boolean satisfiedBy( Page page )
         {
            return pages.pages().toList().indexOf( page ) < index;
         }
      }, pages.pages() ) ) ) );

      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      builder.addLink( " ", "" );
      for( Field field : possiblefields )
      {
         builder.addLink( field.getDescription() + "-" + field.getFieldId(), ((Identity)field).identity().get() );
      }

      return builder.newLinks();
   }
}