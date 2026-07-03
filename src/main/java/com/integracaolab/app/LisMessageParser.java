package com.integracaolab.app;

import java.util.ArrayList;
import java.util.List;
import com.integracaolab.app.api.dto.ResultadoDTO;

public class LisMessageParser {

    private final List<String> dataList;

    public LisMessageParser(List<String> dataList) {
        this.dataList = dataList;
    }

    public LisOrder parse() {
        LisOrder order = new LisOrder();
        for (String linha : dataList) {
            if (linha.startsWith("OBR|")) {
                String[] campos = linha.split("\\|");
                if (campos.length > 2) {
                    order.setSampleId(campos[2].split("\\^")[0].trim());
                }
            }
        }
        return order;
    }

    public List<ResultadoDTO> parseResultados(String sampleId) {
        List<ResultadoDTO> resultados = new ArrayList<>();
        for (String linha : dataList) {
            if (linha.startsWith("OBX|")) {
                String[] campos = linha.split("\\|");
                if (campos.length >= 6) {
                    ResultadoDTO dto = new ResultadoDTO();
                    dto.setSampleId(sampleId);
                    
                    dto.setExame(campos[3].split("\\^")[0]);
                    
                    dto.setValor(campos[4]);
                    
                    dto.setUnidade(campos[5]);
                    
                    resultados.add(dto);
                }
            }
        }
        return resultados;
    }
}