/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.genericjpa.test.jpa.postgres;

import org.hibernate.search.db.events.triggers.PostgreSQLTriggerSQLStringSource;
import org.hibernate.search.genericjpa.test.jpa.ManualUpdatesIntegrationTest;

import org.junit.Before;

/**
 * Created by Martin on 02.07.2015.
 */
public class PostgreSQLEclipseLinkManualIntegrationTest extends ManualUpdatesIntegrationTest {

	@Before
	public void setup() {
		this.setup( "EclipseLink_PostgreSQL", PostgreSQLTriggerSQLStringSource.class );
	}

}
