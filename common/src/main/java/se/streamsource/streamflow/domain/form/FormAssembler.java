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

package se.streamsource.streamflow.domain.form;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * JAVADOC
 */
public class FormAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.addValues( CreateFieldDTO.class,
            EffectiveFieldValue.class,
            EffectiveFormFieldsValue.class,
            SubmittedFieldValue.class,
            FormValue.class,
            FormSubmissionValue.class,
            FieldValue.class,
            FieldValueDTO.class,
            CheckboxesFieldValue.class,
            ComboBoxFieldValue.class,
            CommentFieldValue.class,
            DateFieldValue.class,
            ListBoxFieldValue.class,
            NumberFieldValue.class,
            OptionButtonsFieldValue.class,
            TextAreaFieldValue.class,
            TextFieldValue.class,
            FieldDefinitionValue.class,
            FieldSubmissionValue.class,
            SubmittedFormValue.class,
            PageSubmissionValue.class,
            SubmitFormDTO.class,
            PageDefinitionValue.class).visibleIn( Visibility.application );
   }
}
