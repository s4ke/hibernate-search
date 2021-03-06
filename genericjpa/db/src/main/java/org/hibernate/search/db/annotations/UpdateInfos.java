/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.db.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Contains multiple {@link UpdateInfo}
 * <br> <br>
 * For more information see {@link UpdateInfo}
 *
 * @author Martin Braun
 * @hsearch.experimental
 */
@Target({FIELD, METHOD, TYPE})
@Retention(RUNTIME)
public @interface UpdateInfos {

	UpdateInfo[] value();

}
