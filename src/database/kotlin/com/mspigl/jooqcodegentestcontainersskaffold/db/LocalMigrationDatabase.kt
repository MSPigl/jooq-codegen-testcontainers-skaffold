package com.mspigl.jooqcodegentestcontainersskaffold.db

import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.meta.postgres.PostgresDatabase
import org.jooq.tools.jdbc.JDBCUtils
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection

/**
 * Custom jOOQ database that creates a Postgres database via Testcontainers, executes Flyway migrations against that
 * database container, and then performs jOOQ code generation targeting the container.
 *
 * See build.gradle.kts for usage
 */
@Suppress("unused")
class LocalMigrationDatabase : PostgresDatabase() {
    private var internalConnection: Connection? = null
        get() {
            if (field == null) {
                try {
                    createInternalConnection()
                } catch (e: Exception) {
                    throw RuntimeException("Failed to launch Postgres container", e)
                }
            }

            return field
        }

    override fun create0(): DSLContext {
        return DSL.using(internalConnection, SQLDialect.POSTGRES)
    }

    override fun close() {
        JDBCUtils.safeClose(internalConnection)
        internalConnection = null
        super.close()
    }

    private fun createInternalConnection() {
        try {
            val container = PostgreSQLContainer("postgres:14")
                .withDatabaseName("jooq_activity")
                .withUsername("jooq_generator")
                .withPassword("jooq_generator")
            container.start()

            val flyway = Flyway.configure()
                .dataSource(container.jdbcUrl, container.username, container.password)
                .schemas("sample_schema")
                .baselineOnMigrate(true)
                .baselineVersion("1.0.0")
                .baselineDescription("Baseline")
                .locations("classpath:com/mspigl/jooqcodegentestcontainersskaffold/db/migrations")
                .load()
            flyway.migrate()

            internalConnection = flyway.configuration.dataSource.connection
            connection = internalConnection
        } catch (e: Exception) {
            // printing stack trace here since any error thrown by this class won't be properly displayed by Gradle
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }
}