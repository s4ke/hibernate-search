/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.async;

import java.util.Properties;

import org.hibernate.search.backend.triggers.impl.TriggerAsyncBackendService;
import org.hibernate.search.backend.triggers.impl.TriggerAsyncBackendServiceImpl;
import org.hibernate.search.engine.service.classloading.impl.DefaultClassLoaderService;

/**
 * @author Martin Braun
 *
 * this starts the service by hand
 */
public class ManualAsyncIndexUpdateStartTest extends BaseAsyncIndexUpdateTest {

	public ManualAsyncIndexUpdateStartTest() {
		super( true );
	}

	protected ManualAsyncIndexUpdateStartTest(boolean isProfileTest) {
		super( isProfileTest );
	}

	private TriggerAsyncBackendService triggerAsyncBackendService;

	@Override
	protected void setup() {
		this.triggerAsyncBackendService = new TriggerAsyncBackendServiceImpl();
		Properties properties = new Properties();
		this.triggerAsyncBackendService.start(
				this.getSessionFactory(),
				this.getExtendedSearchIntegrator(),
				new DefaultClassLoaderService(),
				properties
		);
	}

	@Override
	protected void shutdown() {
		this.triggerAsyncBackendService.stop();
	}

}
