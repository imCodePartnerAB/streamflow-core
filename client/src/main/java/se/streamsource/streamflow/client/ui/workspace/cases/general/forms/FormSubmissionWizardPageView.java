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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.first;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.events;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.BindingFormBuilder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.util.ValidationUtils;
import com.jgoodies.validation.view.ValidationResultViewFactory;

/**
 * JAVADOC
 */
public class FormSubmissionWizardPageView
      extends WizardPage
      implements Observer, TransactionListener
{
   private java.util.Map<String, AbstractFieldPanel> componentFieldMap;
   private java.util.Map<StateBinder, EntityReference> fieldBinders;
   private ValidationResultModel validationResultModel;
   private FormSubmissionWizardPageModel model;
   private static final Map<Class<? extends FieldValue>, Class<? extends AbstractFieldPanel>> fields = new HashMap<Class<? extends FieldValue>, Class<? extends AbstractFieldPanel>>();

   @Structure
   Module module;

   static
   {
      // Remember to add editors here when creating new types
      fields.put( CheckboxesFieldValue.class, CheckboxesPanel.class );
      fields.put( ComboBoxFieldValue.class, ComboBoxPanel.class );
      fields.put( DateFieldValue.class, DatePanel.class );
      fields.put( ListBoxFieldValue.class, ListBoxPanel.class );
      fields.put( NumberFieldValue.class, NumberPanel.class );
      fields.put( OptionButtonsFieldValue.class, OptionButtonsPanel.class );
      fields.put( OpenSelectionFieldValue.class, OpenSelectionPanel.class );
      fields.put( TextAreaFieldValue.class, TextAreaFieldPanel.class );
      fields.put( TextFieldValue.class, TextFieldPanel.class );
      fields.put( AttachmentFieldValue.class, AttachmentFieldPanel.class );
   }


   public FormSubmissionWizardPageView( @Structure Module module,
                                        @Uses PageSubmissionDTO page,
                                        @Uses FormDraftModel model)
   {
      super( page.title().get() );
      this.module = module;
      this.model = module.objectBuilderFactory().newObjectBuilder(FormSubmissionWizardPageModel.class).use( model ).newInstance();
      componentFieldMap = new HashMap<String, AbstractFieldPanel>();
      validationResultModel = new DefaultValidationResultModel();
      setLayout( new BorderLayout() );
      final JXPanel panel = new JXPanel( new FormLayout() );
      panel.setScrollableHeightHint( ScrollableSizeHint.VERTICAL_STRETCH );

      fieldBinders = new HashMap<StateBinder, EntityReference>( page.fields().get().size() );
      FormLayout formLayout = new FormLayout( "200dlu, 70dlu:grow", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );
      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, null );

      for (FieldSubmissionDTO DTO : page.fields().get())
      {
         AbstractFieldPanel component;
         FieldValue fieldValue = DTO.field().get().fieldValue().get();
         if ( fieldValue instanceof FieldGroupFieldValue )
         {
            bb.append( new JLabel( "<html><b>"+DTO.field().get().description().get()+"</b>:</html>" ) );
         } else if (!(fieldValue instanceof CommentFieldValue))
         {
            component = getComponent(DTO);
            componentFieldMap.put( DTO.field().get().field().get().identity(), component );
            StateBinder stateBinder = component.bindComponent( bb, DTO);
            stateBinder.addObserver( this );
            fieldBinders.put( stateBinder, DTO.field().get().field().get() );

         } else
         {
            // comment field does not have any input component
            String comment = DTO.field().get().note().get();
            comment = comment.replaceAll( "\n", "<br/>" );

            JEditorPane commentPane = new JEditorPane( "text/html", "<html>" + comment + "</html>" );
            Font font = UIManager.getFont( "Label.font" );
            String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                  "font-size: " + font.getSize() + "pt; }";
            ((HTMLDocument) commentPane.getDocument()).getStyleSheet().addRule( bodyRule );

            commentPane.setOpaque( false );
            commentPane.setBorder( null );
            commentPane.setEditable( false );
            commentPane.setFocusable( true );
            commentPane.addHyperlinkListener( new HyperlinkListener()
            {
               public void hyperlinkUpdate( HyperlinkEvent e )
               {
                  if (e.getEventType().equals( HyperlinkEvent.EventType.ACTIVATED ))
                  {
                     // Open in browser
                     try
                     {
                        Desktop.getDesktop().browse( e.getURL().toURI() );
                     } catch (IOException e1)
                     {
                        e1.printStackTrace();
                     } catch (URISyntaxException e1)
                     {
                        e1.printStackTrace();
                     }
                  }
               }
            } );

            bb.append( commentPane );
         }
      }

      JComponent validationResultsComponent = ValidationResultViewFactory.createReportList( validationResultModel );
      formBuilder.appendRow( "top:30dlu:g" );

      CellConstraints cc = new CellConstraints();
      formBuilder.add( validationResultsComponent, cc.xywh( 1, formBuilder.getRow() + 1, 1, 1, "fill, bottom" ) );

      final JScrollPane scroll = new JScrollPane( panel );
      add( scroll, BorderLayout.CENTER );

      createFocusListenerForComponents( panel, panel.getComponents() );
   }

   private void createFocusListenerForComponents( final JXPanel main, Component[] components )
   {
      for (final Component component : components)
      {
         if (component instanceof AbstractFieldPanel)
         {
            Component firstFocusable = ((AbstractFieldPanel) component).firstFocusableComponent( (AbstractFieldPanel) component );
            if (firstFocusable != null)
            {
               firstFocusable.addFocusListener( new FocusAdapter()
               {
                  @Override
                  public void focusGained( FocusEvent e )
                  {
                     // increase visible rectangle to cover title label too
                     Rectangle rectangle = component.getBounds();
                     rectangle.add( rectangle.getX(), rectangle.getY()-21 );
                     main.scrollRectToVisible( rectangle );
                  }
               } );
            }
         } else
         {

            if (component.isFocusable())
            {
               component.addFocusListener( new FocusAdapter()
               {

                  @Override
                  public void focusGained( FocusEvent e )
                  {
                     main.scrollRectToVisible( component.getBounds() );
                  }
               } );
            }
         }
      }
   }

   @Override
   public WizardPanelNavResult allowNext( String s, Map map, Wizard wizard )
   {
      ValidationResult validation = validatePage();
      validationResultModel.setResult( validation );

      if (!validation.hasErrors())
      {
         // last page check needed ???
         return WizardPanelNavResult.PROCEED;
      }
      return WizardPanelNavResult.REMAIN_ON_PAGE;
   }

   @Override
   public WizardPanelNavResult allowBack( String stepName, Map settings, Wizard wizard )
   {
      // first page check needed ???
      return super.allowBack( stepName, settings, wizard );
   }

   @Override
   public WizardPanelNavResult allowFinish( String s, Map map, Wizard wizard )
   {
      ValidationResult validationResult = validatePage();
      validationResultModel.setResult( validationResult );
      if (validationResult.hasErrors())
      {
         return WizardPanelNavResult.REMAIN_ON_PAGE;
      }
      return WizardPanelNavResult.PROCEED;
   }

   private ValidationResult validatePage()
   {
      ValidationResult validationResult = new ValidationResult();

      for (AbstractFieldPanel component : componentFieldMap.values())
      {

         String value = component.getValue();

         if (component.mandatory())
         {
            if (ValidationUtils.isEmpty( value ))
            {
               validationResult.addError( i18n.text( WorkspaceResources.mandatory_field_missing ) + ": " + component.title() );
            }
         }
      }
      return validationResult;
   }

   public void update( final Observable observable, Object arg )
   {
      final Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "value" ))
      {
         try
         {
            if (property.get() instanceof Date)
            {
               new CommandTask()
               {
                  @Override
                  public void command()
                        throws Exception
                  {
                     model.updateField( fieldBinders.get( observable ), DateFunctions.toUtcString( (Date) property.get() ) );
                  }
               }.execute();
            } else if (property.get() instanceof File)
            {
               try
               {
                  final FileInputStream fin = new FileInputStream( (File) property.get() );

                  new CommandTask()
                  {
                     @Override
                     protected void command() throws Exception
                     {
                        model.createAttachment( fieldBinders.get( observable ), (File) property.get(), fin );
                     }
                  }.execute();
               } catch (Exception e)
               {
                  throw new OperationException( CaseResources.could_not_upload_file, e );
               }
            } else
            {
               new CommandTask()
               {
                  @Override
                  protected void command() throws Exception
                  {
                     model.updateField( fieldBinders.get( observable ), property.get().toString() );
                  }
               }.execute();
            }
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_update_field, e );
         }
      }
   }

   public void updateFieldPanel( String fieldId, String fieldValue )
   {
      AbstractFieldPanel panel = componentFieldMap.get( fieldId );

      if (panel != null && fieldValue != null && !fieldValue.equals( panel.getValue() ))
      {
         panel.setValue( fieldValue );
      }
   }

   private AbstractFieldPanel getComponent( FieldSubmissionDTO field )
   {
      FieldValue fieldValue = field.field().get().fieldValue().get();
      Class<? extends FieldValue> fieldValueType = (Class<FieldValue>) fieldValue.getClass().getInterfaces()[0];
      return module.objectBuilderFactory().newObjectBuilder(fields.get(fieldValueType)).use( field, fieldValue, model ).newInstance();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "changedFieldAttachmentValue" ), transactions ))
      {
         String value = EventParameters.getParameter( first( filter( Events.withNames( "changedFieldAttachmentValue" ), events( transactions ) ) ), "param1" );
         AttachmentFieldDTO dto = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldDTO.class, value);

         ValueBuilder<AttachmentFieldSubmission> builder = module.valueBuilderFactory().newValueBuilder(AttachmentFieldSubmission.class);
         builder.prototype().attachment().set( dto.attachment().get() );
         builder.prototype().name().set( dto.name().get() );

         updateFieldPanel( dto.field().get().identity(), builder.newInstance().toJSON() );
      } else if (Events.matches( Events.withNames( "changedFieldValue" ), transactions ))
      {
         DomainEvent event = first( filter( Events.withNames( "changedFieldValue" ), events( transactions ) ) );
         String fieldId = EventParameters.getParameter( event, "param1" );
         String value = EventParameters.getParameter( event, "param2" );

         updateFieldPanel( fieldId, value );
      }
   }
   
}