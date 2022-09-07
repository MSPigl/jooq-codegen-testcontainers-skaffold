package com.mspigl.jooqcodegentestcontainersskaffold.service

import com.mspigl.jooqcodegentestcontainersskaffold.persistence.SampleRepository
import com.mspigl.jooqcodegentestcontainersskaffold.presentation.model.SampleDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service for resource Sample
 */
@Service
class SampleService(private val repository: SampleRepository) {

    fun create(dto: SampleDto): SampleDto {
        return repository.create(dto).into(SampleDto::class.java)
    }

    fun read(id: Int): SampleDto {
        return repository.read(id)?.into(SampleDto::class.java) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    fun update(id: Int, dto: SampleDto): SampleDto {
        read(id)

        return repository.update(dto.copy(id = id)).into(SampleDto::class.java)
    }

    fun delete(id: Int) {
        repository.delete(id)
    }
}