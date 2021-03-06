/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.async;

import java.util.Map;

import org.hibernate.search.db.events.jpa.impl.AsyncUpdateConstants;
import org.hibernate.search.db.events.triggers.HSQLDBTriggerSQLStringSource;

/**
 * @author Martin Braun
 */
public class HSQLDBAutomaticAsyncIndexUpdateTest extends AutomaticAsyncIndexUpdateTest {

	public HSQLDBAutomaticAsyncIndexUpdateTest() {
		super( false );
	}

	@Override
	public void configure(Map<String, Object> cfg) {
		super.configure( cfg );
		cfg.put( AsyncUpdateConstants.TRIGGER_SOURCE_KEY, HSQLDBTriggerSQLStringSource.class.getName() );
		cfg.put(
				"hibernate.dialect",
				"org.hibernate.dialect.HSQLDialect"
		);
		cfg.put(
				"hibernate.connection.driver_class",
				"org.hsqldb.jdbcDriver"
		);
		cfg.put(
				"hibernate.connection.url",
				"jdbc:hsqldb:mem:test"
		);
		cfg.put(
				"hibernate.connection.username",
				"hibernate_user"
		);
		cfg.put(
				"hibernate.connection.password",
				"hibernate_password"
		);
	}

}
