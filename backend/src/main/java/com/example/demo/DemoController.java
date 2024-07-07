package com.example.demo;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

	private final DbRepository repository;

	public DemoController(DbRepository repository) {
		this.repository = repository;
	}

	@GetMapping(value = "/")
	public DemoDTO greeting(){
		return new DemoDTO("Hello World");
	}

	@PostMapping(value = "/visit")
	public void addVisit(){
		DbEntity dbEntity = new DbEntity();
		dbEntity.setAccess(ZonedDateTime.now());
		repository.save(dbEntity);
	}

	@GetMapping(value = "/visit")
	public List<DbEntity> getVisits(){
		return repository.findTop5ByOrderByIdDesc();
	}

}
