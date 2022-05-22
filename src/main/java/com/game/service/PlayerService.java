package com.game.service;

import com.game.entity.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.NoSuchElementException;

public interface PlayerService {

    Player createPlayer(Player player);

    Player updatePlayer(Player player);

    void deletePlayer(Long id);

    Player getPlayerById(Long id) throws NoSuchElementException;

    List<Player> getPlayers(Specification<Player> spec, Pageable pageable);

    Integer getPlayersCount(Specification<Player> spec);
}
