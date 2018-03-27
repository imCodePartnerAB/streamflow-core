/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.application.entityexport;

import net.sf.ehcache.Element;
import org.qi4j.api.service.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static se.streamsource.streamflow.web.application.entityexport.AbstractExportHelper.LINE_SEPARATOR;
import static se.streamsource.streamflow.web.application.entityexport.SchemaCreatorHelper.IDENTITY_MODIFIED_INFO_TABLE_NAME;

/**
 * Task to check entity existence at SQL.
 * <br/>
 * Used to check current state of entity at SQL db and update/change it
 * <br/>
 */
public class EntityDBCheckJob implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EntityDBCheckJob.class);
    private final DbVendor dbVendor;
    private final ServiceReference<DataSource> dataSource;
    private final Caching caching;
    private final EntityExportService entityExportService;

    private String identity;
    private long modified;
    private String transaction;

    public EntityDBCheckJob(DbVendor dbVendor, ServiceReference<DataSource> dataSource, Caching caching, EntityExportService entityExportService) {
        this.dbVendor = dbVendor;
        this.dataSource = dataSource;
        this.caching = caching;
        this.entityExportService = entityExportService;
    }

    @Override
    public void run() {
        try (final Connection connection = dataSource.get().getConnection()) {
            try (final Statement statement = connection.createStatement()) {
                final String sqlUpdateEntity = entityExportService.updateEntityInfoSql(identity, modified, false);
                statement.executeUpdate(sqlUpdateEntity);

                final String selectProceed = "SELECT proceed FROM " + IDENTITY_MODIFIED_INFO_TABLE_NAME + LINE_SEPARATOR +
                        "WHERE [identity] = '" + identity + "'";
                final ResultSet resultSet = statement.executeQuery(selectProceed);

                if (resultSet.next()) {
                    final boolean proceed = resultSet.getBoolean(1);
                    if (!proceed) {
                        logger.info("Added entity to cache " + identity);
                        caching.put(new Element(identity, transaction));
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
        } catch (SQLException e) {
            logger.error( "Unexpected SQLException: ", e );
        }

    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }
}
