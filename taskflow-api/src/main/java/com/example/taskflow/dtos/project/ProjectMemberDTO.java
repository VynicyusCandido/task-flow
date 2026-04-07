package com.example.taskflow.dtos.project;

import com.example.taskflow.model.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private ProjectRole role;
}
