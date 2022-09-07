package com.mspigl.jooqcodegentestcontainersskaffold.db.migrations

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

/**
 * @author matthew.pigliavento
 */
class V1_0_1__migration : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val dslContext = DSL.using(context!!.connection)

        dslContext.createTableIfNotExists("another_table")
            .column("id", SQLDataType.INTEGER.identity(true))
            .execute()
    }
}
