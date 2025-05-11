package com.scheduler.job.Model;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {

    private String jobName;
    private String jobType;
    private String parameters;
    private int maxRetries;
}
