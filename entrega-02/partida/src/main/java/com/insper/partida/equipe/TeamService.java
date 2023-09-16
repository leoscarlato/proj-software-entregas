package com.insper.partida.equipe;

import com.insper.partida.equipe.dto.SaveTeamDTO;
import com.insper.partida.equipe.dto.TeamReturnDTO;

import com.insper.partida.tabela.Tabela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class  TeamService {

    @Autowired
    private TeamRepository teamRepository;


    public List<TeamReturnDTO> listTeams() {
        return teamRepository.findAll().stream().map(team -> TeamReturnDTO.covert(team)).collect(Collectors.toList());
    }

    public TeamReturnDTO saveTeam(SaveTeamDTO saveTeam) {
        if (teamRepository.findByIdentifier(saveTeam.getIdentifier()) == null) {
            Team team = new Team();
            team.setIdentifier(saveTeam.getIdentifier());
            team.setName(saveTeam.getName());
            teamRepository.save(team);

            Tabela tabela = new Tabela();
            tabela.setNome(saveTeam.getName());
            tabela.setPontos(0);
            tabela.setGolsPro(0);
            tabela.setGolsContra(0);
            tabela.setVitorias(0);
            tabela.setDerrotas(0);
            tabela.setEmpates(0);
            tabela.setJogos(0);
            
            return TeamReturnDTO.covert(team);
        }
        return null;
    }


    public void deleteTeam(String identifier) {

        Team team = teamRepository.findByIdentifier(identifier);
        if (team != null) {
            teamRepository.delete(team);
        }

    }

    public Team getTeam(String identifier) {
        return teamRepository.findByIdentifier(identifier);
    }
}
