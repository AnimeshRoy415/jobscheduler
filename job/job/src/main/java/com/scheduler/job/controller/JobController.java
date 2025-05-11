package com.scheduler.job.controller;

import com.scheduler.job.Model.JobRequest;
import com.scheduler.job.entity.Job;
import com.scheduler.job.entity.JobLog;
import com.scheduler.job.entity.JobStatus;
import com.scheduler.job.repository.JobRepository;
import com.scheduler.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    @Autowired
    private JobService jobService;


    @PostMapping("/create")
    public ResponseEntity<Job> createJob(@RequestBody JobRequest request) {
        Job job = jobService.createAndRunJob(
                request.getJobName(),
                request.getJobType(),
                request.getParameters(),
                request.getMaxRetries()
        );
        return ResponseEntity.ok(job);
    }

    @PostMapping("/trigger/bulk")
    public List<Job> triggerMultipleJobs(@RequestBody List<JobRequest> jobRequests) {

        return jobService.createAndRunJobs(jobRequests);
    }

    @PostMapping("/trigger")
    public Job triggerJob(@RequestBody Map<String, String> request) {
        String jobName = request.get("jobName");
        String jobType = request.get("jobType");
        String parameters = request.get("parameters");
        int maxRetries = Integer.parseInt(request.getOrDefault("maxRetries", "3"));

        return jobService.createAndRunJob(jobName, jobType, parameters, maxRetries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Job>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/search")
    public Page<Job> searchJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return jobService.searchJobs(status, jobType, page, size);
    }

    @GetMapping("/{id}/logs")
    public List<JobLog> getJobLogs(@PathVariable Long id) {
        return jobService.getLogsForJob(id);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelJob(@PathVariable Long id) {
        boolean cancelled = jobService.cancelJob(id);
        if (cancelled) {
            return ResponseEntity.ok("Job cancelled successfully.");
        } else {
            return ResponseEntity.badRequest().body("Unable to cancel job. It may have already completed or failed.");
        }
    }

}
