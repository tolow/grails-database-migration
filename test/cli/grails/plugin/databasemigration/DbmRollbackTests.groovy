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

import java.sql.SQLException

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class DbmRollbackTests extends AbstractScriptTests {

	void testRollback() {

		// run all the updates
		copyTestChangelog()
		executeAndCheck 'dbm-update'

		// test that person table exists
		executeUpdate '''
			insert into person(version, name, street1, street2, zipcode)
			values (0, 'test.name', 'test.street1', 'test.street2', '12345')
		'''

		// manually tag the first two updates
		String tagName = 'THE_TAG'
		executeUpdate "update databasechangelog set tag=? where id='test-1' or id='test-2'", [tagName]

		// test parameter check
		executeAndCheck(['dbm-rollback'], false)
		assertOutputContains('ERROR: The dbm-rollback script requires a tag')

		executeAndCheck(['dbm-rollback', tagName])

		// can't do full insert since the 3rd change was rolled back
		String message = shouldFail(SQLException) {
			executeUpdate '''
				insert into person(version, name, street1, street2, zipcode)
				values (0, 'test.name2', 'test.street1.2', 'test.street2.2', '123456')
			'''
		}
		assertTrue message.contains('Column "ZIPCODE" not found')

		// can do partial insert
		executeUpdate '''
			insert into person(version, name, street1, street2)
			values (1, 'test.name2', 'test.street1.2', 'test.street2.2')
		'''
	}
}
