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
package se.streamsource.infrastructure.index.elasticsearch.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchConfiguration;
import se.streamsource.infrastructure.index.elasticsearch.internal.AbstractElasticSearchAssembler;
import se.streamsource.infrastructure.index.elasticsearch.memory.ESMemoryIndexQueryService;

public class ESMemoryIndexQueryAssembler
        extends AbstractElasticSearchAssembler<ESMemoryIndexQueryAssembler>
{

    @Override
    protected void doAssemble( String identity,
                               ModuleAssembly module, Visibility visibility,
                               ModuleAssembly configModule, Visibility configVisibility )
            throws AssemblyException
    {
        module.services( ESMemoryIndexQueryService.class ).
                identifiedBy( identity ).
                visibleIn( visibility ).
                instantiateOnStartup();

        configModule.entities( ElasticSearchConfiguration.class ).
                visibleIn( configVisibility );
    }

}
