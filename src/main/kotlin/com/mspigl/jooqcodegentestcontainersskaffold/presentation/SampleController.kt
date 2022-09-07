package com.mspigl.jooqcodegentestcontainersskaffold.presentation

import com.mspigl.jooqcodegentestcontainersskaffold.presentation.model.SampleDto
import com.mspigl.jooqcodegentestcontainersskaffold.service.SampleService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for resource Sample
 */
@RestController
@RequestMapping("/samples")
class SampleController(private val service: SampleService) {

    @PostMapping
    fun create(@RequestBody dto: SampleDto): SampleDto {
        return service.create(dto)
    }

    @GetMapping("/{id}")
    fun read(@PathVariable id: Int): SampleDto {
        return service.read(id)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody dto: SampleDto): SampleDto {
        return service.update(id, dto)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int) {
        service.delete(id)
    }
}
