package com.scheduler.job.service;

import com.scheduler.job.Model.JobRequest;
import com.scheduler.job.entity.Job;
import com.scheduler.job.entity.JobLog;
import com.scheduler.job.entity.JobStatus;
import com.scheduler.job.repository.JobLogRepository;
import com.scheduler.job.repository.JobRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobLogRepository jobLogRepository;

    public Job createAndRunJob(String jobName, String jobType, String parameters, int maxRetries) {
        Job job = Job.builder()
                .jobName(jobName)
                .jobType(jobType)
                .parameters(parameters)
                .status(JobStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .maxRetries(maxRetries)
                .progressPercentage(0)
                .build();

        Job savedJob = jobRepository.save(job);
        runAsyncJob(savedJob.getId());
        return savedJob;
    }

    public List<Job> createAndRunJobs(List<JobRequest> requests) {
        List<Job> jobs = new ArrayList<>();

        for (JobRequest req : requests) {
            Job job = new Job();
            job.setJobName(req.getJobName());
            job.setJobType(req.getJobType());
            job.setParameters(req.getParameters());
            job.setMaxRetries(req.getMaxRetries());
            job.setStatus(JobStatus.PENDING);
            job.setCreatedAt(LocalDateTime.now());

            jobs.add(job);
        }

        // Save all jobs in a single DB transaction
        List<Job> savedJobs = jobRepository.saveAll(jobs);

        // Launch each job asynchronously
        for (Job job : savedJobs) {
            runAsyncJob(job.getId());
        }

        return savedJobs;
    }

    /*@Async("taskExecutor")
    public void runAsyncJob(Long jobId) {
        Optional<Job> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            log(jobId, "Job not found");
            return;
        }

        Job job = optionalJob.get();
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            log(jobId, "Job started");

            for (int i = 1; i <= 10; i++) {
                Thread.sleep(500);
                job.setProgressPercentage(i * 10);
                jobRepository.save(job);
                log(jobId, "Progress: " + (i * 10) + "%");
            }

            job.setStatus(JobStatus.SUCCESS);
            job.setCompletedAt(LocalDateTime.now());
            job.setResult("Job completed successfully.");
            jobRepository.save(job);

            log(jobId, "Job completed successfully");
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setResult("Job failed: " + e.getMessage());
            jobRepository.save(job);

            log(jobId, "Job failed: " + e.getMessage());
        }
    }*/

    @Async("taskExecutor")
    public void runAsyncJob(Long jobId) {
        Optional<Job> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            log(jobId, "Job not found");
            return;
        }

        Job job = optionalJob.get();

        // Check if already cancelled before starting
        if (job.isCancelled()) {
            job.setStatus(JobStatus.CANCELLED);
            job.setCompletedAt(LocalDateTime.now());
            job.setResult("Job was cancelled before execution.");
            jobRepository.save(job);
            log(jobId, "Job was cancelled before execution.");
            return;
        }

        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            log(jobId, "Job started");

            for (int i = 1; i <= 10; i++) {
                // Check for cancellation during execution
                if (job.isCancelled()) {
                    job.setStatus(JobStatus.CANCELLED);
                    job.setCompletedAt(LocalDateTime.now());
                    job.setResult("Job was cancelled during execution.");
                    jobRepository.save(job);
                    log(jobId, "Job was cancelled during execution.");
                    return;
                }

                Thread.sleep(500); // Simulate work
                job.setProgressPercentage(i * 10);
                jobRepository.save(job);
                log(jobId, "Progress: " + (i * 10) + "%");
            }

            job.setStatus(JobStatus.SUCCESS);
            job.setCompletedAt(LocalDateTime.now());
            job.setResult("Job completed successfully.");
            jobRepository.save(job);

            log(jobId, "Job completed successfully");
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setResult("Job failed: " + e.getMessage());
            jobRepository.save(job);

            log(jobId, "Job failed: " + e.getMessage());
        }
    }


    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Page<Job> searchJobs(String statusStr, String jobType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        JobStatus status = null;
        if (statusStr != null) {
            try {
                status = JobStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        if (status != null && jobType != null) {
            return jobRepository.findByStatusAndJobType(status, jobType, pageable);
        } else if (status != null) {
            return jobRepository.findAllByStatus(status, pageable);
        } else if (jobType != null) {
            return jobRepository.findByJobType(jobType, pageable);
        } else {
            return jobRepository.findAll(pageable);
        }
    }

    private void log(Long jobId, String message) {
        jobLogRepository.save(JobLog.builder()
                .jobId(jobId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<JobLog> getLogsForJob(Long jobId) {
        return jobLogRepository.findByJobId(jobId);
    }

    public boolean cancelJob(Long jobId) {
        Optional<Job> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isPresent()) {
            Job job = optionalJob.get();
            if (job.getStatus() == JobStatus.PENDING || job.getStatus() == JobStatus.RUNNING) {
                job.setCancelled(true);
                job.setStatus(JobStatus.CANCELLED);
                job.setCompletedAt(LocalDateTime.now());
                job.setResult("Job was cancelled.");
                jobRepository.save(job);
                log(jobId, "Job was cancelled by user.");
                return true;
            }
        }
        return false;
    }
}
