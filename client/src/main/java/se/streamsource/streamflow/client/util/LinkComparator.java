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
package se.streamsource.streamflow.client.util;

import se.streamsource.dci.value.link.LinkValue;

import java.text.Collator;
import java.util.Comparator;

/**
 * JAVADOC
 */
public class LinkComparator
      implements Comparator<LinkValue>
{
   public int compare( LinkValue o1, LinkValue o2 )
   {
      Collator collator = Collator.getInstance();
      return collator.compare( o1.text().get(), o2.text().get() );
   }
}