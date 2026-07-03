package com.integracaolab.app;

import java.util.ArrayList;
import java.util.List;

public class LisMessageBuffer {
    
    private List<String> linhas = new ArrayList<>(); 
    
    public void addLine(String linha) {
        if (linha != null && !linha.isEmpty()) {
            linhas.add(linha.replaceAll("[\\u000b\\u001c\\r\\n]", "").trim());
        }
    }
    
    public boolean isComplete() {
        return !linhas.isEmpty();
    }
    
    public List<String> getMessage() {
        return new ArrayList<>(linhas);
    }
        
    public void clear() {
        linhas.clear();
    } 
}