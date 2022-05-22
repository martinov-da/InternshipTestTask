package com.game.service;

import com.game.entity.Player;
import com.game.repository.PlayerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PlayerServiceImpl implements PlayerService {

    PlayerRepo playerRepo;

    @Autowired
    public void setPlayerRepo(PlayerRepo playerRepo) {
        this.playerRepo = playerRepo;
    }

    @Override
    public Player createPlayer(Player player) {
        return playerRepo.save(player);
    }

    @Override
    public Player updatePlayer(Player player) {
        return playerRepo.save(player);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepo.deleteById(id);
    }

    @Override
    public Player getPlayerById(Long id) throws NoSuchElementException {
        return playerRepo.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<Player> getPlayers(Specification<Player> spec, Pageable pageable) {
        return playerRepo.findAll(spec, pageable).getContent();
    }

    @Override
    public Integer getPlayersCount(Specification<Player> spec) {
        return (int) playerRepo.count(spec);
    }
}
