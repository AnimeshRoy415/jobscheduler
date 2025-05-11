package com.scheduler.job.repository;

import com.scheduler.job.entity.Job;
import com.scheduler.job.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findAllByStatus(JobStatus status, Pageable pageable);
    Page<Job> findByJobType(String jobType, Pageable pageable);
    Page<Job> findByStatusAndJobType(JobStatus status, String jobType, Pageable pageable);
}
