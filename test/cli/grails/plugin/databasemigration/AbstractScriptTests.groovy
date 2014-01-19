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

import grails.test.AbstractCliTestCase

import groovy.sql.Sql

import java.sql.DriverManager

import org.h2.tools.Server

import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Abstract base class for script tests.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
abstract class AbstractScriptTests extends AbstractCliTestCase {

	private static final String TEST_CHANGELOG = 'changelog.cli.test.groovy'
	protected static final String CHANGELOG_DIR = 'target/changelogs'

	protected static final String URL = 'jdbc:h2:tcp://localhost/./target/testdb/testdb'

	private Server server

	protected File file

	protected void setUp() {
		super.setUp()

		new File('target/testdb').deleteDir()
		new File(CHANGELOG_DIR).deleteDir()
		new File(CHANGELOG_DIR).mkdirs()

		server = Server.createTcpServer().start()
		assertEquals 9092, server.port

		executeUpdate '''
			create table thing (
				id bigint generated by default as identity (start with 1),
				version bigint not null,
				name varchar(255) not null,
				primary key (id)
			)'''
	}

	protected void tearDown() {
		super.tearDown()
		server?.stop()
	}

	protected void executeAndCheck(List<String> command, boolean shouldSucceed = true) {
		execute command
		int exitCode = waitForProcess()
		if (shouldSucceed) {
			if (exitCode != 0) {
				println output
			}
			assertEquals 0, exitCode
		}
		else {
			if (exitCode == 0) {
				println output
			}
			assertFalse 0 == exitCode
		}
		verifyHeader()
	}
	
	protected void assertOutputContains(expected) {
		Pattern p = Pattern.compile(expected)
		Matcher m = p.matcher(output)

		assertTrue 'output not as expected: "' + output + '"', m.find()
	}
	
	protected void checkRunningDB() {
		assertOutputContains('(Starting dbm-changelog-sync-sql for database sa @ |Connected to SA@)jdbc:h2:tcp://localhost/./target/testdb/testdb')
	}

	protected void executeAndCheck(String command) {
		executeAndCheck([command])
	}

	protected void initFile(boolean groovy) {
		String name = 'changelog_' + System.currentTimeMillis() + (groovy ? '.groovy' : '.xml')
		file = new File(CHANGELOG_DIR, name)
		file.deleteOnExit()
		assertFalse file.exists()
	}

	protected Sql newSql() { Sql.newInstance(URL, 'sa', '', 'org.h2.Driver') }

	protected void executeUpdate(String sql, List values = null) {
		if (values) {
			newSql().executeUpdate sql, values
		}
		else {
			newSql().executeUpdate sql
		}
	}

	protected void executeInsert(String sql, List values) {
		newSql().executeInsert sql, values
	}

	protected List<String> findAllTableNames() {
		def tableNames = []
		newSql().eachRow('show tables') { tableNames << it.TABLE_NAME.toLowerCase() }
		tableNames
	}

	protected void assertTableCount(int count) {
		assertEquals 'checking table count', count, findAllTableNames().size()
	}

	protected void generateChangelog() {
		executeAndCheck(['dbm-generate-changelog', TEST_CHANGELOG])
	}

	protected void copyTestChangelog(String name = 'test.changelog') {
		def file = new File(CHANGELOG_DIR, TEST_CHANGELOG)
		file.deleteOnExit()
		file.withWriter {
			it.write getClass().getResourceAsStream(name).text
		}
	}
}
