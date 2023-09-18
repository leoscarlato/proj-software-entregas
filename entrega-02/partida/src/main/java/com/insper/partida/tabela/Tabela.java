package com.insper.partida.tabela;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("tabela")
public class Tabela {

    public String nome;
    public Integer pontos;
    public Integer golsPro;
    public Integer golsContra;
    public Integer vitorias;
    public Integer derrotas;
    public Integer empates;
    public Integer jogos;
    
}
