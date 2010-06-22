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

package se.streamsource.streamflow.client.ui.caze.attachments;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.attachment.AttachmentValue;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * JAVADOC
 */
public class AttachmentsTableFormatter
      implements AdvancedTableFormat<AttachmentValue>
{
   protected String[] columnNames = new String[]{
         text( attachment_name ),
         text( attachment_size ),
         text( created_column_header ),
         };
   protected Class[] columnClasses;

   public AttachmentsTableFormatter()
   {
      columnClasses = new Class[] {
            String.class,
            String.class,
            Date.class
            };
   }

   public int getColumnCount()
   {
      return columnNames.length;
   }

   public String getColumnName( int i )
   {
      return columnNames[i];
   }

   public Class getColumnClass( int i )
   {
      return columnClasses[i];
   }

   public Comparator getColumnComparator( int i )
   {
      return null;
   }

   public Object getColumnValue( AttachmentValue attachmentValue, int i )
   {
      switch (i)
      {
         case 0:
         {
            return attachmentValue.text().get();
         }

         case 1:
            long size = attachmentValue.size().get();

            if (size > 1024)
            {
               return size/1024+" KB";
            } else if (size > 1000*1024)
            {
               return size/(1000*1024)+" MB";
            } else
               return size+"";

         case 2:
            return attachmentValue.modificationDate().get() == null ? "" : DateFormat.getDateInstance( DateFormat.MEDIUM ).format( attachmentValue.modificationDate().get());
      }

      return null;
   }
}