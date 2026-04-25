package com.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Bot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String personaDescription;
}
