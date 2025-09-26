package com.searchDev.SearchDev.Repository;

import com.searchDev.SearchDev.Model.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepo extends JpaRepository<Projects, UUID> {
}
