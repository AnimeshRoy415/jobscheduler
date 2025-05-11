package com.scheduler.job.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobLog {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private Long jobId;

        private String message;

        private LocalDateTime timestamp;
}

