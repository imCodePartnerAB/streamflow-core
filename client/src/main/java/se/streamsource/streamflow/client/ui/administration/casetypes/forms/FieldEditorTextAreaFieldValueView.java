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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

/**
 * JAVADOC
 */
public class FieldEditorTextAreaFieldValueView
      extends JScrollPane
{

   private StateBinder fieldDefinitionBinder;
   private StateBinder fieldValueBinder;

   public FieldEditorTextAreaFieldValueView( @Service ApplicationContext context,
                                             @Uses FieldValueEditModel model,
                                             @Structure ObjectBuilderFactory obf)
   {
      JPanel panel = new JPanel( new BorderLayout() );

      JPanel fieldPanel = new JPanel();
      FormLayout formLayout = new FormLayout(
            "45dlu, 5dlu, 150dlu:grow",
            "pref, pref, pref, pref, pref, 5dlu, top:70dlu:grow" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, fieldPanel );
      formBuilder.setBorder(Borders.createEmptyBorder("4dlu, 4dlu, 4dlu, 4dlu"));

      fieldDefinitionBinder = new StateBinder();
      fieldDefinitionBinder.setResourceMap( context.getResourceMap( getClass() ) );
      FieldDefinitionValue fieldDefinitionTemplate = fieldDefinitionBinder.bindingTemplate( FieldDefinitionValue.class );

      fieldValueBinder = new StateBinder();
      fieldValueBinder.setResourceMap( context.getResourceMap( getClass() ) );
      TextAreaFieldValue fieldValueTemplate = fieldValueBinder.bindingTemplate( TextAreaFieldValue.class );

      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.text_field_type ) ) );
      formBuilder.nextLine();

      formBuilder.add(new JLabel(i18n.text(AdministrationResources.mandatory)));
      formBuilder.nextColumn(2);
      formBuilder.add(fieldDefinitionBinder.bind( CHECKBOX.newField(), fieldDefinitionTemplate.mandatory() ) );
      formBuilder.nextLine();

      formBuilder.add(new JLabel(i18n.text(AdministrationResources.cols_label)));
      formBuilder.nextColumn(2);
      formBuilder.add(fieldValueBinder.bind( TEXTFIELD.newField(), fieldValueTemplate.cols() ) );
      formBuilder.nextLine();

      formBuilder.add(new JLabel(i18n.text(AdministrationResources.rows_label)));
      formBuilder.nextColumn(2);
      formBuilder.add(fieldValueBinder.bind( TEXTFIELD.newField(), fieldValueTemplate.rows() ) );
      formBuilder.nextLine();

      formBuilder.add(new JLabel(i18n.text(AdministrationResources.name_label)));
      formBuilder.nextColumn(2);
      formBuilder.add(fieldDefinitionBinder.bind( TEXTFIELD.newField(), fieldDefinitionTemplate.description() ) );
      formBuilder.nextLine(2);

      formBuilder.add(new JLabel(i18n.text(AdministrationResources.description_label)));
      formBuilder.nextColumn(2);
      formBuilder.add(fieldDefinitionBinder.bind( TEXTAREA.newField(), fieldDefinitionTemplate.note() ) );
      formBuilder.nextLine();

      FieldValueObserver observer = obf.newObjectBuilder( FieldValueObserver.class ).use( model ).newInstance();
      fieldValueBinder.addObserver( observer );
      fieldDefinitionBinder.addObserver( observer );

      fieldValueBinder.updateWith( model.getFieldDefinition().fieldValue().get() );
      fieldDefinitionBinder.updateWith( model.getFieldDefinition() );

      panel.add( fieldPanel, BorderLayout.CENTER );

      setViewportView( panel );
   }
}