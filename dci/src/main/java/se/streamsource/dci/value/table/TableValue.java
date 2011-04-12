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

package se.streamsource.dci.value.table;

import org.qi4j.api.common.*;
import org.qi4j.api.property.*;
import org.qi4j.api.value.*;

import java.util.*;

/**
 * A table of rows. RowValue needs to be subtyped in order to add columns.
 */
public interface TableValue
      extends ValueComposite
{
   @UseDefaults
   Property<List<ColumnValue>> cols();

   @UseDefaults
   Property<List<RowValue>> rows();
}