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
package se.streamsource.streamflow.api.workspace.cases.general;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;

import java.util.Date;

/**
 * General information about a case
 */
public interface CaseGeneralDTO
      extends ValueComposite
{
   @Optional
   Property<String> caseId();

   @Optional
   Property<LinkValue> caseType();

   @Optional
   @MaxLength(50)
   Property<String> description();

   @Optional
   Property<String> note();

   Property<Date> creationDate();

   Property<CaseStates> status();

   @Optional
   Property<Date> dueOn();
   
   @Optional
   Property<LinkValue> priority();
}