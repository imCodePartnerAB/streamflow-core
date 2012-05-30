/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.api.administration.form;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * JAVADOC
 */
@Mixins( TextAreaFieldValue.Mixin.class )
public interface TextAreaFieldValue
      extends FieldValue
{
   Property<Integer> cols();

   Property<Integer> rows();


   abstract class Mixin
         implements FieldValue
   {
      public Boolean validate( String value )
      {
         return value != null;
      }
   }
}