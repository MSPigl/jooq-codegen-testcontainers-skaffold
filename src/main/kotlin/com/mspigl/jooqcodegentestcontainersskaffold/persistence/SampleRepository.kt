package com.mspigl.jooqcodegentestcontainersskaffold.persistence

import com.mspigl.jooqcodegentestcontainersskaffold.presentation.model.SampleDto
import com.mspigl.jooqcodegentestcontainersskaffold.tables.records.SampleTableRecord
import com.mspigl.jooqcodegentestcontainersskaffold.tables.references.SAMPLE_TABLE
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

/**
 * Repository for the sample_table table
 */
@Repository
class SampleRepository(private val dslContext: DSLContext) {

    fun create(dto: SampleDto): SampleTableRecord {
        val record = dslContext.newRecord(SAMPLE_TABLE, dto)
        record.insert()

        return record
    }

    fun read(id: Int): SampleTableRecord? {
        return dslContext.selectFrom(SAMPLE_TABLE)
            .where(SAMPLE_TABLE.ID.eq(id))
            .fetchOne()
    }

    fun update(dto: SampleDto): SampleTableRecord {
        val record = dslContext.newRecord(SAMPLE_TABLE, dto)
        record.update()

        return record
    }

    fun delete(id: Int) {
        dslContext.deleteFrom(SAMPLE_TABLE)
            .where(SAMPLE_TABLE.ID.eq(id))
            .execute()
    }
}
