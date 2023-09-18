package com.insper.partida.game;

import com.insper.partida.equipe.Team;
import com.insper.partida.equipe.TeamService;
import com.insper.partida.equipe.dto.SaveTeamDTO;
import com.insper.partida.equipe.dto.TeamReturnDTO;
import com.insper.partida.game.dto.EditGameDTO;
import com.insper.partida.game.dto.GameReturnDTO;
import com.insper.partida.game.dto.SaveGameDTO;
import com.insper.partida.tabela.Tabela;
import com.insper.partida.tabela.TabelaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TabelaRepository tabelaRepository;

    public Page<GameReturnDTO> listGames(String home, String away, Integer attendance, Pageable pageable) {
        if (home != null && away != null) {

            Team tHome = teamService.getTeam(home);
            Team tAway = teamService.getTeam(away);

            Page<Game> games = gameRepository.findByHomeAndAway(tHome.getIdentifier(), tAway.getIdentifier(), pageable);
            return games.map(game -> GameReturnDTO.covert(game));

        } else if (attendance != null) {
            Page<Game> games =  gameRepository.findByAttendanceGreaterThan(attendance, pageable);
            return games.map(game -> GameReturnDTO.covert(game));
        }
        Page<Game> games =  gameRepository.findAll(pageable);
        return games.map(game -> GameReturnDTO.covert(game));
    }

    public GameReturnDTO saveGame(SaveGameDTO saveGameDTO) {

        Team teamM = teamService.getTeam(saveGameDTO.getHome());
        Team teamV = teamService.getTeam(saveGameDTO.getAway());

        if (teamM == null || teamV == null) {
            return null;
        }

        Game game = new Game();
        game.setIdentifier(UUID.randomUUID().toString());
        game.setHome(teamM.getIdentifier());
        game.setAway(teamV.getIdentifier());
        game.setAttendance(0);
        game.setScoreHome(0);
        game.setScoreAway(0);
        game.setGameDate(LocalDateTime.now());
        game.setStatus("SCHEDULED");

        gameRepository.save(game);
        return GameReturnDTO.covert(game);

    }

    public GameReturnDTO editGame(String identifier, EditGameDTO editGameDTO) {
        Game gameBD = gameRepository.findByIdentifier(identifier);

        gameBD.setScoreAway(editGameDTO.getScoreAway());
        gameBD.setScoreHome(editGameDTO.getScoreHome());
        gameBD.setAttendance(editGameDTO.getAttendance());
        gameBD.setStatus("FINISHED");

        Tabela tabelaHome = tabelaRepository.findByNome(gameBD.getHome());
        if (tabelaHome == null) {
            tabelaHome = new Tabela();
            tabelaHome.setNome(gameBD.getHome());
        }

        Tabela tabelaAway = tabelaRepository.findByNome(gameBD.getAway());
        if (tabelaAway == null) {
            tabelaAway = new Tabela();
            tabelaAway.setNome(gameBD.getAway());
        }

        tabelaHome.setGolsPro(tabelaHome.getGolsPro() + editGameDTO.getScoreHome());
        tabelaHome.setGolsContra(tabelaHome.getGolsContra() + editGameDTO.getScoreAway());
        tabelaHome.setJogos(tabelaHome.getJogos() + 1);

        tabelaAway.setGolsPro(tabelaAway.getGolsPro() + editGameDTO.getScoreAway());
        tabelaAway.setGolsContra(tabelaAway.getGolsContra() + editGameDTO.getScoreHome());
        tabelaAway.setJogos(tabelaAway.getJogos() + 1);

        if (editGameDTO.getScoreAway() == editGameDTO.getScoreHome()) {
            tabelaHome.setEmpates(tabelaHome.getEmpates() + 1);
            tabelaAway.setEmpates(tabelaAway.getEmpates() + 1);
            tabelaHome.setPontos(tabelaHome.getPontos() + 1);
            tabelaAway.setPontos(tabelaAway.getPontos() + 1);
        } else if (editGameDTO.getScoreAway() > editGameDTO.getScoreHome()) {
            tabelaAway.setVitorias(tabelaAway.getVitorias() + 1);
            tabelaHome.setDerrotas(tabelaHome.getDerrotas() + 1);
            tabelaAway.setPontos(tabelaAway.getPontos() + 3);
        } else {
            tabelaHome.setVitorias(tabelaHome.getVitorias() + 1);
            tabelaAway.setDerrotas(tabelaAway.getDerrotas() + 1);
            tabelaHome.setPontos(tabelaHome.getPontos() + 3);
        }

        tabelaRepository.save(tabelaHome);
        tabelaRepository.save(tabelaAway);

        Game game = gameRepository.save(gameBD);
        return GameReturnDTO.covert(game);
    }

    public void deleteGame(String identifier) {
        Game gameBD = gameRepository.findByIdentifier(identifier);
        if (gameBD != null) {
            gameRepository.delete(gameBD);
        }
    }

    public Integer getScoreTeam(String identifier) {
        Team team = teamService.getTeam(identifier);

        return 0;
    }

    public GameReturnDTO getGame(String identifier) {
        return GameReturnDTO.covert(gameRepository.findByIdentifier(identifier));
    }

    public void generateData() {

        String [] teams = {"botafogo", "palmeiras", "gremio", "flamengo", "fluminense", "bragantino", "atletico-mg", "athletico-pr", "fortaleza", "cuiaba", "sao-paulo",
                        "internacional", "cruzeiro", "corinthians", "goias", "bahia", "santos", "vasco", "coritiba", "america-mg"};

        for (String team : teams) {
            SaveTeamDTO saveTeamDTO = new SaveTeamDTO();
            saveTeamDTO.setName(team);
            saveTeamDTO.setStadium(team);
            saveTeamDTO.setIdentifier(team);

            Team teamDB = teamService.getTeam(team);
            if (teamDB == null) {
                teamService.saveTeam(saveTeamDTO);
            }
        }

        List<Game> games = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {

            Integer team1 = new Random().nextInt(20);
            Integer team2 = new Random().nextInt(20);
            while  (team1 == team2) {
                team2 = new Random().nextInt(20);
            }

            Game game = new Game();
            game.setIdentifier(UUID.randomUUID().toString());
            game.setHome(teams[team1]);
            game.setAway(teams[team2]);
            game.setScoreHome(new Random().nextInt(4));
            game.setScoreAway(new Random().nextInt(4));
            game.setStadium(teams[team1]);
            game.setAttendance(new Random().nextInt(4) * 1000);

            gameRepository.save(game);
            
            EditGameDTO editGameDTO = new EditGameDTO();
            editGameDTO.setScoreAway(game.getScoreAway());
            editGameDTO.setScoreHome(game.getScoreHome());
            editGameDTO.setAttendance(game.getAttendance());
            this.editGame(game.getIdentifier(), editGameDTO);
            // games.add(game);
            
            
        }

        // gameRepository.saveAll(games);


    }

    public List<Game> getGameByTeam(String identifier) {
        return gameRepository.findByHomeOrAway(identifier, identifier);
    }
}
