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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import org.qi4j.api.injection.scope.*;
import se.streamsource.streamflow.client.ui.workspace.cases.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.*;

public class TextFieldPanel
      extends AbstractFieldPanel
{
   private JTextField textField;
   private TextFieldValue fieldValue;

   @Service
   DialogService dialogs;

   public TextFieldPanel( @Uses FieldSubmissionValue field, @Uses TextFieldValue fieldValue )
   {
      super( field );
      setLayout( new BorderLayout() );
      this.fieldValue = fieldValue;

      textField = new JTextField();
      textField.setColumns( fieldValue.width().get() );
      add( textField, BorderLayout.WEST );
   }

   @Override
   public String getValue()
   {
      return textField.getText();
   }

   @Override
   public void setValue( String newValue )
   {
      textField.setText( newValue );
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      final TextFieldPanel panel = this;
      textField.setInputVerifier( new InputVerifier()
      {
         @Override
         public boolean verify( JComponent input )
         {
            if (!Strings.empty( fieldValue.regularExpression().get() )
                  && !Strings.empty( ((JTextComponent) input).getText() ))
            {
               try
               {
                  new RegexPatternFormatter( fieldValue.regularExpression().get() ).stringToValue( ((JTextComponent) input).getText() );
               } catch (ParseException e)
               {
                  dialogs.showMessageDialog( panel,
                        i18n.text( CaseResources.regular_expression_does_not_validate ), "" );
                  return false;
               }
            }
            binding.updateProperty( ((JTextComponent) input).getText() );
            return true;
         }
      });
   }

   @Override
   protected String componentName()
   {
      StringBuilder componentName = new StringBuilder( "<html>" );
      componentName.append( title() );
      if (!Strings.empty( fieldValue.hint().get() ))
      {
         componentName.append( " <font color='#778899'>(" ).append( fieldValue.hint().get() ).append( ")</font>" );
      }

      if (mandatory())
      {
         componentName.append( " <font color='red'>*</font>" );
      }
      componentName.append( "</html>" );
      return componentName.toString();
   }

}