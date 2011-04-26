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

package se.streamsource.streamflow.api.workspace.cases.contact;

import org.qi4j.api.common.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import org.qi4j.api.value.*;
import org.qi4j.library.constraints.annotation.*;

import java.util.*;

/**
 * Contact information for either a person or a company
 */
@Mixins(ContactDTO.Mixin.class)
public interface ContactDTO
      extends ValueComposite
{
   @UseDefaults
   Property<String> name();

   @UseDefaults
   @Matches("([\\d]{12})?")
   Property<String> contactId();

   @UseDefaults
   Property<String> company();

   @UseDefaults
   Property<Boolean> isCompany();

   @UseDefaults
   Property<List<ContactPhoneDTO>> phoneNumbers();

   @UseDefaults
   Property<List<ContactEmailDTO>> emailAddresses();

   @UseDefaults
   Property<List<ContactAddressDTO>> addresses();

   @UseDefaults
   Property<String> picture();

   @UseDefaults
   Property<String> note();

   ContactEmailDTO defaultEmail();

   ContactPhoneDTO defaultPhone();

   abstract class Mixin
      implements ContactDTO
   {
      public ContactEmailDTO defaultEmail()
      {
         ListIterator<ContactEmailDTO> listIter = emailAddresses().get().listIterator();
         if (listIter.hasNext())
         {
            return listIter.next();
         } else
            return null;
      }

      public ContactPhoneDTO defaultPhone()
      {
         ListIterator<ContactPhoneDTO> listIter = phoneNumbers().get().listIterator();
         if (listIter.hasNext())
         {
            return listIter.next();
         } else
            return null;
      }
   }
}