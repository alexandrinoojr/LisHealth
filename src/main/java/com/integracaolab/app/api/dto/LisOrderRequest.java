package com.integracaolab.app.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class LisOrderRequest {

    @JsonProperty("code_bar")
    private String codeBar;

    @JsonProperty("patient_name")
    private String patientName;

    @JsonProperty("patient_birth_date")
    private String patientBirthDate;

    @JsonProperty("patient_gender")
    private String patientGender;

    @JsonProperty("patient_cpf")
    private String patientCpf;

    @JsonProperty("sample-type")
    private String sampleType;

    @JsonProperty("tests")
    private List<String> tests;

    // getters e setters
    public String getCodeBar() {
        return codeBar;
    }

    public void setCodeBar(String codeBar) {
        this.codeBar = codeBar;
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

    public List<String> getTests() {
        return tests;
    }

    public void setTests(List<String> tests) {
        this.tests = tests;
    }
}
