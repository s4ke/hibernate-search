/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.genericjpa.impl;

import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.genericjpa.events.impl.SynchronizedUpdateSource;
import org.hibernate.search.genericjpa.metadata.impl.ExtendedTypeMetadata;

/**
 * Created by Martin on 27.07.2015.
 */
public interface SynchronizedUpdateSourceProvider {

	SynchronizedUpdateSource getUpdateSource(
			ExtendedSearchIntegrator searchIntegrator,
			Map<Class<?>, ExtendedTypeMetadata> rehashedTypeMetadataPerIndexRoot,
			Map<Class<?>, Set<Class<?>>> containedInIndexOf,
			Properties properties,
			EntityManagerFactory emf,
			TransactionManager transactionManager,
			Set<Class<?>> indexRelevantEntities);

}
