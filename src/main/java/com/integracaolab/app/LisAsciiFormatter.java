package com.integracaolab.app;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LisAsciiFormatter {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public List<String> format(LisOrder order) {
        List<String> linhas = new ArrayList<>();
        String ts = LocalDateTime.now().format(TS_FORMAT);

        linhas.add("\u000bMSH|^~\\&|LIS|SYSTEM|T240Plus||" + ts + "||ORU^R01|1|P|2.3.1||||0||UNICODE");
        
        linhas.add("PID|1||||" + (order.getPatientName() != null ? order.getPatientName() : "") + "||||||||||||||||||||||||||^Y");
        
        String exams = order.getExams() != null ? String.join("^", order.getExams()) : "";
        linhas.add("OBR|1|" + order.getSampleId() + "||" + order.getSampleId() + "^T240Plus|N||" + ts + "||1|1^1||N||" + ts);
        
        int last = linhas.size() - 1;
        linhas.set(last, linhas.get(last) + "\u001c");
        
        return linhas;
    }
}