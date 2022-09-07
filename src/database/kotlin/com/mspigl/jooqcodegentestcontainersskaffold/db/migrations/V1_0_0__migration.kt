package com.mspigl.jooqcodegentestcontainersskaffold.db.migrations

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

class V1_0_0__migration : BaseJavaMigration() {

    override fun migrate(context: Context?) {
        val dslContext = DSL.using(context!!.connection)

        dslContext.createSchemaIfNotExists("sample_schema").execute()

        dslContext.createTableIfNotExists("sample_table")
            .column("id", SQLDataType.INTEGER.identity(true))
            .column("field", SQLDataType.INTEGER)
            .constraint(DSL.primaryKey("id"))
            .execute()
    }
}
