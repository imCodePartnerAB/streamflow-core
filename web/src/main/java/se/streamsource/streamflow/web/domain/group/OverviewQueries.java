/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.group;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.task.Assignable;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Ownable;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.domain.task.TaskStatus;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Delegatable;

import java.util.Locale;
import java.util.ResourceBundle;

@Mixins(OverviewQueries.Mixin.class)
public interface OverviewQueries
{

	public void generateExcelProjectSummary(Locale locale, Workbook workbook);
	public ProjectSummaryListDTO getProjectsSummary();

	class Mixin implements OverviewQueries
	{
		@Structure
		QueryBuilderFactory qbf;

		@Structure
		ValueBuilderFactory vbf;

		@Structure
		UnitOfWorkFactory uowf;

		@This
		Identity id;

        @This
        Participation.Data participant;

        public ProjectSummaryListDTO getProjectsSummary()
        {
            UnitOfWork uow =  uowf.currentUnitOfWork();

            ValueBuilder<ProjectSummaryDTO> builder = vbf.newValueBuilder(ProjectSummaryDTO.class);
            ProjectSummaryDTO builderPrototype = builder.prototype();

            ValueBuilder<ProjectSummaryListDTO> listBuilder = vbf.newValueBuilder(ProjectSummaryListDTO.class);
            ProjectSummaryListDTO listBuilderPrototype = listBuilder.prototype();

            for (Project project : participant.allProjects())
            {
                Association<Assignee> assigneeAssociation = templateFor( Assignable.Data.class).assignedTo();
                Property<String> ownableId = templateFor( Ownable.Data.class).owner().get().identity();

                QueryBuilder<TaskEntity> ownerQueryBuilder = qbf.newQueryBuilder(TaskEntity.class).where(
                        eq(ownableId, ((ProjectEntity)project).identity().get()));

                Association<Assignee> assignee = templateFor( Assignable.Data.class).assignedTo();
                Association<Delegatee> delegatee = templateFor( Delegatable.Data.class).delegatedTo();

                QueryBuilder<TaskEntity> inboxQueryBuilder = ownerQueryBuilder.where(and(
                    isNull(assigneeAssociation),
                    isNull(delegatee),
                    eq(templateFor( TaskStatus.Data.class).status(), TaskStates.ACTIVE)));
                Query<TaskEntity> inboxQuery = inboxQueryBuilder.newQuery(uow);

                
                QueryBuilder<TaskEntity> assignedQueryBuilder = ownerQueryBuilder.where(and(
                        isNotNull(assigneeAssociation),
                        eq(templateFor( TaskStatus.Data.class).status(), TaskStates.ACTIVE)));
                Query<TaskEntity> assignedQuery = assignedQueryBuilder.newQuery(uow);

                builderPrototype.project().set(project.getDescription());
                builderPrototype.inboxCount().set(inboxQuery.count());
                builderPrototype.assignedCount().set(assignedQuery.count());

                listBuilderPrototype.projectOverviews().get().add(builder.newInstance());

            }
            return listBuilder.newInstance();
        }
        
		public void generateExcelProjectSummary(Locale locale, Workbook workbook)
		{
			ProjectSummaryListDTO summaryListDTO = getProjectsSummary();
			ResourceBundle bundle = ResourceBundle.getBundle(
					OverviewQueries.class.getName(), locale);
			Sheet sheet = createSheet(bundle.getString("projects_summary"),
					workbook);

			// Create header cells
			Row headerRow = sheet.createRow((short) 0);
			headerRow.setHeightInPoints(30);

			createHeaderCell(bundle.getString("project_column_header"), workbook,
					headerRow, (short) 0, HSSFCellStyle.ALIGN_CENTER,
					HSSFCellStyle.VERTICAL_CENTER);
			createHeaderCell(bundle.getString("inbox_column_header"), workbook,
					headerRow, (short) 1, HSSFCellStyle.ALIGN_CENTER,
					HSSFCellStyle.VERTICAL_CENTER);
			createHeaderCell(bundle.getString("assigned_column_header"), workbook,
					headerRow, (short) 2, HSSFCellStyle.ALIGN_CENTER,
					HSSFCellStyle.VERTICAL_CENTER);
			createHeaderCell(bundle.getString("total_column_header"), workbook,
					headerRow, (short) 3, HSSFCellStyle.ALIGN_CENTER,
					HSSFCellStyle.VERTICAL_CENTER);
			short rowCounter = 0;
			for (ProjectSummaryDTO summaryDTO : summaryListDTO
					.projectOverviews().get())
			{
				Row contentRow = sheet.createRow(++rowCounter);
				// contentRow.setHeightInPoints(30);

				// Project
				createCell(summaryDTO.project().get(), workbook, contentRow,
						(short) 0, HSSFCellStyle.ALIGN_LEFT,
						HSSFCellStyle.VERTICAL_TOP);
				// Inbox
				createCell(String.valueOf(summaryDTO.inboxCount().get()),
						workbook, contentRow, (short) 1,
						HSSFCellStyle.ALIGN_RIGHT, HSSFCellStyle.VERTICAL_TOP);
				// Assigned
				createCell(String.valueOf(summaryDTO.assignedCount().get()),
						workbook, contentRow, (short) 2,
						HSSFCellStyle.ALIGN_RIGHT, HSSFCellStyle.VERTICAL_TOP);
				// Total
				createCell(String.valueOf(summaryDTO.inboxCount().get()
						+ summaryDTO.assignedCount().get()), workbook,
						contentRow, (short) 3, HSSFCellStyle.ALIGN_RIGHT,
						HSSFCellStyle.VERTICAL_TOP);
			}
		}

		/**
		 * Creates a header cell and aligns it a certain way.
		 * 
		 * @param wb
		 *            the workbook
		 * @param row
		 *            the row to create the cell in
		 * @param column
		 *            the column number to create the cell in
		 * @param halign
		 *            the horizontal alignment for the cell.
		 * @param valign
		 *            the vertical alignment for the cell.
		 */
		private static void createHeaderCell(String cellValue, Workbook wb,
				Row row, short column, short halign, short valign)
		{
			Cell cell = row.createCell(column);
			cell.setCellValue(new HSSFRichTextString(cellValue));
			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setAlignment(halign);
			cellStyle.setVerticalAlignment(valign);
			cell.setCellStyle(cellStyle);
		}

		/**
		 * Creates a cell and aligns it a certain way.
		 * 
		 * @param wb
		 *            the workbook
		 * @param row
		 *            the row to create the cell in
		 * @param column
		 *            the column number to create the cell in
		 * @param halign
		 *            the horizontal alignment for the cell.
		 * @param valign
		 *            the vertical alignment for the cell.
		 */
		private static void createCell(String cellValue, Workbook wb, Row row,
				short column, short halign, short valign)
		{
			Cell cell = row.createCell(column);
			cell.setCellValue(new HSSFRichTextString(cellValue));
			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setAlignment(halign);
			cellStyle.setVerticalAlignment(valign);
			cell.setCellStyle(cellStyle);
		}

		protected Sheet createSheet(String name, Workbook workbook)
		{
			return workbook.createSheet(name);
		}

	}
}
