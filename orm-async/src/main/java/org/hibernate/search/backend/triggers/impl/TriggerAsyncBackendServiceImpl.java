/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.triggers.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.db.events.impl.AsyncUpdateSource;
import org.hibernate.search.db.events.impl.EventModelInfo;
import org.hibernate.search.db.events.impl.EventModelParser;
import org.hibernate.search.db.events.index.impl.IndexUpdater;
import org.hibernate.search.db.events.jpa.impl.SQLJPAAsyncUpdateSourceProvider;
import org.hibernate.search.db.events.jpa.impl.TriggerCreationStrategy;
import org.hibernate.search.db.events.triggers.TriggerSQLStringSource;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.engine.metadata.impl.MetadataProvider;
import org.hibernate.search.engine.metadata.impl.TypeMetadata;
import org.hibernate.search.engine.service.classloading.spi.ClassLoaderService;
import org.hibernate.search.exception.AssertionFailure;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.genericjpa.factory.StandaloneSearchConfiguration;
import org.hibernate.search.genericjpa.metadata.impl.ExtendedTypeMetadata;
import org.hibernate.search.genericjpa.metadata.impl.MetadataExtender;
import org.hibernate.search.genericjpa.metadata.impl.MetadataUtil;
import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

import static org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants.BATCH_SIZE_FOR_UPDATES_DEFAULT_VALUE;
import static org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants.BATCH_SIZE_FOR_UPDATES_KEY;
import static org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants.TRIGGER_CREATION_STRATEGY_DEFAULT_VALUE;
import static org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants.TRIGGER_CREATION_STRATEGY_KEY;
import static org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants.TRIGGER_SOURCE_KEY;
import static org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants.UPDATE_DELAY_DEFAULT_VALUE;
import static org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants.UPDATE_DELAY_KEY;

/**
 * Basic Implementation of {@link TriggerAsyncBackendService}
 *
 * @author Martin Braun
 */
public class TriggerAsyncBackendServiceImpl implements TriggerAsyncBackendService {

	private static final Log log = LoggerFactory.make();

	private AsyncUpdateSource asyncUpdateSource;
	private SQLJPAAsyncUpdateSourceProvider asyncUpdateSourceProvider;
	private ORMEntityManagerFactoryWrapper entityManagerFactoryWrapper;
	private TriggerCreationStrategy createTriggerStrategy;
	private List<EventModelInfo> eventModelInfos;

	@Override
	public void start(
			SessionFactory sessionFactory,
			ExtendedSearchIntegrator extendedSearchIntegrator,
			ClassLoaderService cls,
			Properties properties) {
		Boolean enabled = Boolean.parseBoolean(
				(String) properties.getOrDefault(
						TriggerServiceConstants.TRIGGER_BASED_BACKEND_KEY,
						TriggerServiceConstants.TRIGGER_BASED_BACKEND_DEFAULT_VALUE
				)
		);
		if ( !enabled ) {
			return;
		}
		if ( this.asyncUpdateSource != null ) {
			throw new AssertionFailure( "TriggerAsyncBackendServiceImpl was started twice" );
		}

		this.entityManagerFactoryWrapper = new ORMEntityManagerFactoryWrapper( sessionFactory );

		List<Class<?>> entities = sessionFactory.getAllClassMetadata().entrySet().stream().map(
				e -> (Class<?>) e.getValue().getMappedClass()
		).collect( Collectors.toList() );

		List<ExtendedTypeMetadata> extendedTypeMetadatas = getExtendedTypeMetadata( entities );

		this.setupAsyncUpdateSource( extendedTypeMetadatas, entities, sessionFactory, cls, properties );
		this.setupIndexUpdater( extendedTypeMetadatas, entities, sessionFactory, extendedSearchIntegrator );

		this.asyncUpdateSource.start();
	}

	private static List<ExtendedTypeMetadata> getExtendedTypeMetadata(List<Class<?>> entities) {
		List<Class<?>> indexRootTypes = entities.stream()
				.filter( cl -> cl.isAnnotationPresent( Indexed.class ) )
				.collect( Collectors.toList() );

		MetadataProvider metadataProvider = MetadataUtil.getDummyMetadataProvider( new StandaloneSearchConfiguration() );

		List<TypeMetadata> typeMetadatas = new ArrayList<>();
		for ( Class<?> indexRootType : indexRootTypes ) {
			typeMetadatas.add( metadataProvider.getTypeMetadataFor( indexRootType ) );
		}

		MetadataExtender metadataExtender = new MetadataExtender();

		return metadataExtender.rehash(
				typeMetadatas,
				entities
		);
	}

	private void setupAsyncUpdateSource(
			List<ExtendedTypeMetadata> extendedTypeMetadatas,
			List<Class<?>> entities,
			SessionFactory sessionFactory,
			ClassLoaderService cls, Properties properties) {
		{
			String triggerSource = (String) properties.get( TRIGGER_SOURCE_KEY );
			Class<?> triggerSourceClass;
			if ( triggerSource == null || (triggerSourceClass = cls.classForName( triggerSource )) == null ) {
				throw new SearchException(
						"class specified in " + TRIGGER_SOURCE_KEY + " could not be found: " + triggerSource
				);
			}
			this.createTriggerStrategy = TriggerCreationStrategy.fromString(
					(String) properties.getOrDefault(
							TRIGGER_CREATION_STRATEGY_KEY,
							TRIGGER_CREATION_STRATEGY_DEFAULT_VALUE
					)
			);
			try {
				this.asyncUpdateSourceProvider = new SQLJPAAsyncUpdateSourceProvider(
						(TriggerSQLStringSource) triggerSourceClass.newInstance(),
						entities, this.createTriggerStrategy
				);
			}
			catch (InstantiationException | IllegalAccessException e) {
				throw new SearchException( e );
			}
		}

		Integer batchSizeForUpdates = Integer
				.parseInt(
						(String) properties.getOrDefault(
								BATCH_SIZE_FOR_UPDATES_KEY,
								BATCH_SIZE_FOR_UPDATES_DEFAULT_VALUE
						)
				);
		Integer updateDelay = Integer.parseInt(
				(String) properties.getOrDefault(
						UPDATE_DELAY_KEY,
						UPDATE_DELAY_DEFAULT_VALUE
				)
		);

		Set<Class<?>> indexRelevantEntities = MetadataUtil.calculateIndexRelevantEntities(
				extendedTypeMetadatas,
				entities
		);
		EventModelParser eventModelParser = new ORMEventModelParser( sessionFactory, indexRelevantEntities );
		this.eventModelInfos = eventModelParser.parse( new ArrayList<>( indexRelevantEntities ) );

		this.asyncUpdateSource = this.asyncUpdateSourceProvider.getUpdateSource(
				updateDelay,
				TimeUnit.MILLISECONDS,
				batchSizeForUpdates,
				properties,
				this.entityManagerFactoryWrapper,
				this.eventModelInfos
		);
	}

	private void setupIndexUpdater(
			List<ExtendedTypeMetadata> extendedTypeMetadatas,
			List<Class<?>> entities,
			SessionFactory sessionFactory,
			ExtendedSearchIntegrator extendedSearchIntegrator) {
		{
			Map<Class<?>, Set<Class<?>>> containedInIndexOf = MetadataUtil.calculateInIndexOf(
					extendedTypeMetadatas,
					entities
			);
			Map<Class<?>, String> idProperties = MetadataUtil.calculateIdProperties( extendedTypeMetadatas );

			Map<Class<?>, ExtendedTypeMetadata> metadataPerIndexRoot = new HashMap<>();
			for ( ExtendedTypeMetadata extendedTypeMetadata : extendedTypeMetadatas ) {
				metadataPerIndexRoot.put(
						extendedTypeMetadata.getOriginalTypeMetadata().getType(),
						extendedTypeMetadata
				);
			}

			IndexUpdater indexUpdater = new IndexUpdater(
					metadataPerIndexRoot,
					containedInIndexOf,
					new ORMReusableEntityProvider( sessionFactory, idProperties ),
					extendedSearchIntegrator
			);

			this.asyncUpdateSource.setUpdateConsumers( Collections.singletonList( indexUpdater::updateEvent ) );
		}
	}


	@Override
	public void stop() {
		if ( this.asyncUpdateSource != null ) {
			this.asyncUpdateSource.stop();
		}

		/* FIXME: we need a better Hibernate ORM callback so this works:
		if ( this.asyncUpdateSourceProvider != null
				&& this.createTriggerStrategy == TriggerCreationStrategy.CREATE_DROP ) {
			this.asyncUpdateSourceProvider.dropDDL(
					this.entityManagerFactoryWrapper,
					this.eventModelInfos
			);
		}*/

		this.createTriggerStrategy = null;
		this.asyncUpdateSource = null;
		this.asyncUpdateSourceProvider = null;
		this.entityManagerFactoryWrapper = null;
		this.eventModelInfos = null;
	}

}
