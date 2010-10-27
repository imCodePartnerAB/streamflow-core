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

package se.streamsource.streamflow.client.util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXDatePicker;
import org.qi4j.api.property.Property;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.Component;
import java.text.DateFormat;
import java.util.Locale;

/**
 * JAVADOC
 */
public class BindingFormBuilder
{
   // Field types

   public enum Fields
   {
      LABEL
            {
               public Component newField()
               {
                  JLabel jLabel = new JLabel();
                  jLabel.setFocusable( false );
                  return jLabel;
               }
            },
      TEXTFIELD
            {
               public Component newField()
               {
                  return new JTextField( 30 );
               }
            },
      FORMATTEDTEXTFIELD
            {
               public Component newField()
               {
                  return new JFormattedTextField( 30 );
               }
            },
      PASSWORD
            {
               public Component newField()
               {
                  return new JPasswordField( 15 );
               }
            },
      TEXTAREA
            {
               public Component newField()
               {
                  JTextArea text = new JTextArea( 10, 30 );
                  text.setLineWrap( true );
                  text.setWrapStyleWord( true );
                  return new JScrollPane( text );
               }
            },
      CHECKBOX
            {
               public Component newField()
               {
                  return new JCheckBox();
               }
            },
      RADIOBUTTON
            {
               public Component newField()
               {
                  return new JRadioButton();
               }
            },
      COMBOBOX
            {
               public Component newField()
               {
                  return new JComboBox();
               }
            },
      DATEPICKER
            {
               public Component newField()
               {
                  return new JXDatePicker( Locale.getDefault());
               }
            };

      public abstract Component newField();
   }

   private DefaultFormBuilder formBuilder;
   private ResourceMap resourceMap;
   private StateBinder stateBinder;

   public BindingFormBuilder( DefaultFormBuilder formBuilder, StateBinder stateBinder )
   {
      this( formBuilder, stateBinder, null );
   }

   public BindingFormBuilder( DefaultFormBuilder formBuilder, StateBinder stateBinder, ResourceMap resourceMap )
   {
      this.formBuilder = formBuilder;
      this.resourceMap = resourceMap;
      this.stateBinder = stateBinder;
   }

   public BindingFormBuilder appendLine( Enum resourceKey, Fields fieldType, Property property, Object... args )
   {
      return appendLine( resourceKey, fieldType.newField(), property, stateBinder, args );
   }

   public BindingFormBuilder appendLine( Enum resourceKey, Component component, Property property, Object... args )
   {
      return appendLine( resourceKey, component, property, stateBinder, args );
   }

   public BindingFormBuilder appendLine( Enum resourceKey, Fields fieldType, Property property, StateBinder stateBinderIn, Object... args )
   {
      return appendLine( resourceKey, fieldType.newField(), property, stateBinderIn, args );
   }

   public BindingFormBuilder appendLine( Enum resourceKey, Component component, Property property, StateBinder stateBinderIn, Object... args )
   {
      append( resourceKey, component, property, stateBinderIn, args );

      formBuilder.nextLine();

      return this;
   }

   public BindingFormBuilder append( String name, Component component,
                                     Property property, StateBinder stateBinderIn )
   {
      JLabel label = formBuilder.append( name );
      label.setFocusable( false );
      label.setLabelFor( component );
      formBuilder.nextLine();
      return append( component, property, stateBinderIn );
   }


   public BindingFormBuilder append( Enum resourceKey, Component component,
                                     Property property, StateBinder stateBinderIn, Object... args )
   {
      String resource = getResource( resourceKey, args );

      JLabel label = formBuilder.append( resource );
      label.setFocusable( false );
      label.setLabelFor( component );
      formBuilder.nextLine();
      return append( component, property, stateBinderIn );
   }

   public BindingFormBuilder append( Component component, Property property,
                                     StateBinder stateBinderIn )
   {
      stateBinderIn.bind( component, property );
      formBuilder.append( component );

      if (component instanceof JXDatePicker)
      {
         // Set date format
         ((JXDatePicker) component).setFormats( DateFormat.getDateInstance( DateFormat.MEDIUM, Locale.getDefault() ) );
      }
      return this;
   }


   public BindingFormBuilder appendButtonLine( Action buttonAction )
   {
      JButton button = new JButton( buttonAction );
      formBuilder.append( button );
      formBuilder.nextLine();
      return this;
   }

   public BindingFormBuilder appendToggleButtonLine( Action buttonAction )
   {
      JToggleButton button = new JToggleButton( buttonAction );
      formBuilder.append( button );
      formBuilder.nextLine();
      return this;
   }

   public BindingFormBuilder append( Component component )
   {
      formBuilder.append( component );
      return this;
   }

   public BindingFormBuilder appendLine( Component component )
   {
      formBuilder.append( component );
      formBuilder.nextLine();
      return this;
   }

   public BindingFormBuilder appendSeparator( Enum resourceKey )
   {
      formBuilder.appendSeparator( getResource( resourceKey ) );
      return this;
   }

   public BindingFormBuilder appendFormattedTextField( Enum resourceKey, Enum resourceKeyPattern, Property property, StateBinder stateBinderIn, Object... args )
   {
      String resource = getResource( resourceKey, args );
      String pattern = getResource( resourceKeyPattern, args );

      JLabel label = formBuilder.append( resource );
      formBuilder.nextLine();
      JFormattedTextField component = new JFormattedTextField( new RegexPatternFormatter( pattern ) );
      stateBinderIn.bind( component, property );
      formBuilder.append( component );
      label.setLabelFor( component );
      formBuilder.nextLine();

      return this;

   }

   public String getResource( Enum resourceKey, Object... args )
   {
      String key = resourceKey.toString();

      String resource = resourceMap == null ? null : resourceMap.getString( key, args );
      if (resource == null)
      {
         ResourceMap map = Application.getInstance().getContext().getResourceMap( resourceKey.getClass() );
         resource = map.getString( key, args );
      }

      if (resource == null)
      {
         resource = "#" + key;
      }
      return resource;
   }
}

