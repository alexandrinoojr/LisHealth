package com.integracaolab.app.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordens")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ent_cod", nullable = false)
    private Entidade entidade;

    @Column(name = "sample_id", nullable = false, length = 50)
    private String sampleId;

    @Column(name = "patient_name", length = 150)
    private String patientName;

    @Column(name = "patient_birth_date", length = 10)
    private String patientBirthDate;

    @Column(name = "patient_gender", length = 1)
    private String patientGender;

    @Column(name = "patient_cpf", length = 20)
    private String patientCpf;

    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Column(name = "exams_raw", columnDefinition = "TEXT")
    private String examsRaw;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /* =======================
       GETTERS AND SETTERS
       ======================= */

    public Long getId() {
        return id;
    }

    public Entidade getEntidade() {
        return entidade;
    }

    public void setEntidade(Entidade entidade) {
        this.entidade = entidade;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientCpf() {
        return patientCpf;
    }

    public void setPatientCpf(String patientCpf) {
        this.patientCpf = patientCpf;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getExamsRaw() {
        return examsRaw;
    }

    public void setExamsRaw(String examsRaw) {
        this.examsRaw = examsRaw;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
