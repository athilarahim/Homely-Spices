package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepo extends JpaRepository<Status, Integer> {
}
