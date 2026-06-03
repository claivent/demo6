package com.claivent.demo6.repository;

import com.claivent.demo6.model.DbUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbUserRepository extends JpaRepository<DbUser, Long> {}
