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

package se.streamsource.dci.test.interactions.jmx;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;

import javax.management.*;
import javax.management.openmbean.*;
import java.util.*;

/**
 * JAVADOC
 */
public class MBeanAttributeContext
      implements IndexContext<Value>
{
   @Structure
   Module module;

   public Value index()
   {
      try
      {
         Object attribute = RoleMap.role( MBeanServer.class ).getAttribute( RoleMap.role( ObjectName.class ), RoleMap.role( MBeanAttributeInfo.class ).getName() );

         if (attribute instanceof TabularDataSupport)
         {
            TabularDataSupport table = (TabularDataSupport) attribute;
            ValueBuilder<TabularDataValue> builder = module.valueBuilderFactory().newValueBuilder( TabularDataValue.class );
            Set<Map.Entry<Object, Object>> entries = table.entrySet();
            List<List<String>> cells = builder.prototype().cells().get();
            for (Map.Entry<Object, Object> entry : entries)
            {
               CompositeDataSupport cds = (CompositeDataSupport) entry.getValue();
               String key = cds.get( "key" ).toString();
               String value = cds.get( "value" ).toString();

               List<String> row = new ArrayList<String>();
               row.add( key );
               row.add( value );
               cells.add( row );
            }
            return builder.newInstance();
         } else
         {
            ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
            builder.prototype().string().set( attribute.toString() );
            return builder.newInstance();
         }
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public void update( StringValue newValue ) throws InstanceNotFoundException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException
   {
      Attribute attribute = new Attribute( RoleMap.role( MBeanAttributeInfo.class ).getName(), newValue.string().get() );
      RoleMap.role( MBeanServer.class ).setAttribute( RoleMap.role( ObjectName.class ), attribute );
   }
}