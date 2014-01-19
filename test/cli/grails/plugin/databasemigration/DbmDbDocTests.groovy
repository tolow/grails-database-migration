/* Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.databasemigration

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class DbmDbDocTests extends AbstractScriptTests {

	void testDbDoc() {

		generateChangelog()

		executeUpdate 'drop table thing'

		executeAndCheck(['dbm-update-count', '1'])

		executeAndCheck 'dbm-db-doc'

		['authors', 'changelogs', 'columns', 'pending', 'recent', 'tables'].each {
			assertTrue new File('target/dbdoc', it).exists()
			assertTrue new File('target/dbdoc', it).isDirectory()
		}

		assertTrue new File('target/dbdoc/changelogs/changelog.cli.test.groovy.xml').exists()

		assertTrue new File('target/dbdoc/tables/thing.html').exists()

		assertTrue new File('target/dbdoc/columns/thing.id.html').exists()
		assertTrue new File('target/dbdoc/columns/thing.name.html').exists()
		assertTrue new File('target/dbdoc/columns/thing.version.html').exists()

		checkRunningDB()
	}

	protected void tearDown() {
		super.tearDown()
		new File('target/dbdoc').deleteDir()
	}
}
