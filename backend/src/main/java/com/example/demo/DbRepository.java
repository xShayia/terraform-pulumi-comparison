package com.example.demo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface DbRepository extends CrudRepository<DbEntity,Long> {

	List<DbEntity> findTop5ByOrderById();
}
