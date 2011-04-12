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

package se.streamsource.streamflow.web.context.account;

import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.contact.*;
import se.streamsource.streamflow.server.plugin.contact.*;
import se.streamsource.streamflow.web.application.contact.*;

/**
 * JAVADOC
 */
public class ContactableContext
      implements IndexContext<ContactValue>
{
   @Optional
   @Service
   StreamflowContactLookupService contactLookup;

   @Structure
   ValueBuilderFactory vbf;

   public ContactValue index()
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();
      return contact;
   }

   public void changename( StringValue name )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();

      ValueBuilder<ContactValue> builder = contact.buildWith();
      builder.prototype().name().set( name.string().get().trim() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changenote( StringValue note )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();

      ValueBuilder<ContactValue> builder = contact.buildWith();
      builder.prototype().note().set( note.string().get() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changecontactid( StringValue contactId )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();

      ValueBuilder<ContactValue> builder = contact.buildWith();
      builder.prototype().contactId().set( contactId.string().get() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changecompany( StringValue company )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();

      ValueBuilder<ContactValue> builder = contact.buildWith();
      builder.prototype().company().set( company.string().get() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changephonenumber( ContactPhoneValue phoneValue )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();

      ValueBuilder<ContactValue> builder = contact.buildWith();

      // Create an empty phone value if it doesnt exist already
      if (contact.phoneNumbers().get().isEmpty())
      {
         ContactPhoneValue phone = vbf.newValue( ContactPhoneValue.class ).<ContactPhoneValue>buildWith().prototype();
         phone.phoneNumber().set( phoneValue.phoneNumber().get() );
         builder.prototype().phoneNumbers().get().add( phone );
      } else
      {
         builder.prototype().phoneNumbers().get().get( 0 ).phoneNumber().set( phoneValue.phoneNumber().get() );
      }

      contactable.updateContact( builder.newInstance() );
   }

   public void changeaddress( ContactAddressValue addressValue )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();

      ValueBuilder<ContactValue> builder = contact.buildWith();

      // Create an empty address value if it doesnt exist already
      if (contact.addresses().get().isEmpty())
      {
         ContactAddressValue address = vbf.newValue( ContactAddressValue.class ).<ContactAddressValue>buildWith().prototype();
         address.address().set( addressValue.address().get() );
         builder.prototype().addresses().get().add( address );
      } else
      {
         builder.prototype().addresses().get().get( 0 ).address().set( addressValue.address().get() );
      }

      contactable.updateContact( builder.newInstance() );
   }

   public void changeemailaddress( ContactEmailValue emailValue )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactValue contact = contactable.getContact();

      ValueBuilder<ContactValue> builder = contact.buildWith();

      // Create an empty email value if it doesnt exist already
      if (contact.emailAddresses().get().isEmpty())
      {
         ContactEmailValue email = vbf.newValue( ContactEmailValue.class ).<ContactEmailValue>buildWith().prototype();
         email.emailAddress().set( emailValue.emailAddress().get().trim() );
         builder.prototype().emailAddresses().get().add( email );
      } else
      {
         builder.prototype().emailAddresses().get().get( 0 ).emailAddress().set( emailValue.emailAddress().get().trim() );
      }

      contactable.updateContact( builder.newInstance() );
   }

   @ServiceAvailable(StreamflowContactLookupService.class)
   public ContactList contactlookup( se.streamsource.streamflow.server.plugin.contact.ContactValue template )
   {
      if (contactLookup != null)
         return contactLookup.lookup( template );
      else
         return vbf.newValue( ContactList.class );
   }
}