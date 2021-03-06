/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.genericjpa.test.jpa.hsqldb;

import org.hibernate.search.db.events.triggers.HSQLDBTriggerSQLStringSource;
import org.hibernate.search.genericjpa.test.jpa.AutomaticUpdatesIntegrationTest;

import org.junit.Before;

/**
 * Created by Martin on 22.07.2015.
 */
public class HSQLDBEclipseLinkAutomaticUpdatesIntegrationTest extends AutomaticUpdatesIntegrationTest {

	@Before
	public void setup() {
		this.setup( "sql", "EclipseLink_HSQLDB", HSQLDBTriggerSQLStringSource.class );
	}

}
