package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import com.game.repository.PlayerSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    PlayerService playerService;

    Date minDateValue;
    Date maxDateValue;

    {
        Calendar date = Calendar.getInstance();
        date.set(2000, Calendar.JANUARY, 1);
        minDateValue = date.getTime();

        date = Calendar.getInstance();
        date.set(3000, Calendar.DECEMBER, 31, 23, 59, 59);
        maxDateValue = date.getTime();
    }

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!isAllFieldsFilledAndValid(player)) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        if (player.getBanned() == null) {
            player.setBanned(false);
        }

        player.setLevel(getCurrentLvl(player.getExperience()));
        player.setUntilNextLevel(getExpToNextLvl(player.getExperience()));

        player = playerService.createPlayer(player);

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player update) {
        if (id == null || id <= 0) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        if (!isAllFieldsValid(update)) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        Player player;
        try {
            player = playerService.getPlayerById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        if (update.getName() != null) player.setName(update.getName());
        if (update.getTitle() != null) player.setTitle(update.getTitle());
        if (update.getRace() != null) player.setRace(update.getRace());
        if (update.getProfession() != null) player.setProfession(update.getProfession());
        if (update.getBirthday() != null) player.setBirthday(update.getBirthday());
        if (update.getExperience() != null) {
            int exp = update.getExperience();
            player.setExperience(exp);
            player.setLevel(getCurrentLvl(exp));
            player.setUntilNextLevel(getExpToNextLvl(exp));
        }
        if (update.getBanned() != null) player.setBanned(update.getBanned());

        return new ResponseEntity<>(playerService.updatePlayer(player), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable Long id) {
        if (id == null || id <= 0) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        try {
            playerService.getPlayerById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        playerService.deletePlayer(id);

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable Long id) {
        if (id == null || id <= 0) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        Player player;
        try {
            player = playerService.getPlayerById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Player>> getPlayers(@RequestParam Map<String, String> params) {
        Specification<Player> specification = createSpecification(params);
        PageRequest pageRequest = createPageRequest(params);

        List<Player> players = playerService.getPlayers(specification, pageRequest);

        return new ResponseEntity<>(players, HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getPlayersCount(@RequestParam Map<String, String> params) {
        Specification<Player> specification = createSpecification(params);

        Integer playersCount = playerService.getPlayersCount(specification);

        return new ResponseEntity<>(playersCount, HttpStatus.OK);
    }


    private boolean isAllFieldsFilledAndValid(Player player) {
        return player.getName() != null && player.getTitle() != null
                && player.getRace() != null && player.getProfession() != null
                && player.getBirthday() != null && player.getExperience() != null
                && isAllFieldsValid(player);
    }

    private boolean isAllFieldsValid(Player player) {
        return (player.getName() == null
                || (!player.getName().equals("") && player.getName().length() <= 12)) && (player.getTitle() == null
                || player.getTitle().length() <= 30) && (player.getExperience() == null
                || (player.getExperience() >= 0 && player.getExperience() <= 10_000_000)) && (player.getBirthday() == null
                || (player.getBirthday().getTime() >= 0
                && !player.getBirthday().before(minDateValue) && !player.getBirthday().after(maxDateValue)));

    }

    private int getCurrentLvl(int exp) {
        return (int) (Math.sqrt(2500 + 200 * exp) - 50) / 100;
    }

    private int getExpToNextLvl(int exp) {
        int lvl = getCurrentLvl(exp);
        return 50 * (lvl + 1) * (lvl + 2) - exp;
    }

    private Specification<Player> createSpecification(Map<String, String> params) {
        Specification<Player> specification = Specification.where(null);

        String name = params.get("name");
        String title = params.get("title");
        Race race = params.get("race") == null ? null : Race.valueOf(params.get("race"));
        Profession profession = params.get("profession") == null ? null : Profession.valueOf(params.get("profession"));
        long after = Long.parseLong(params.getOrDefault("after", "0"));
        long before = Long.parseLong(params.getOrDefault("before", Long.toString(maxDateValue.getTime() + 1)));
        int minExperience = Integer.parseInt(params.getOrDefault("minExperience", "0"));
        int maxExperience = Integer.parseInt(params.getOrDefault("maxExperience", Integer.toString(Integer.MAX_VALUE)));
        int minLevel = Integer.parseInt(params.getOrDefault("minLevel", "0"));
        int maxLevel = Integer.parseInt(params.getOrDefault("maxLevel", Integer.toString(Integer.MAX_VALUE)));
        Boolean banned = params.get("banned") == null ? null : Boolean.valueOf(params.get("banned"));

        if (name != null) specification = specification.and(PlayerSpec.nameMatch(name));
        if (title != null) specification = specification.and(PlayerSpec.titleMatch(title));
        if (race != null) specification = specification.and(PlayerSpec.raceIs(race));
        if (profession != null) specification = specification.and(PlayerSpec.professionIs(profession));
        if (banned != null) specification = specification.and(PlayerSpec.isBanned(banned));
        if (after != 0L || before != Long.MAX_VALUE)
            specification = specification.and(PlayerSpec.birthdayBetween(after, before));
        if (minExperience != 0 || maxExperience != Integer.MAX_VALUE)
            specification = specification.and(PlayerSpec.expBetween(minExperience, maxExperience));
        if (minLevel != 0 || maxLevel != Integer.MAX_VALUE)
            specification = specification.and(PlayerSpec.lvlBetween(minLevel, maxLevel));

        return specification;
    }

    private PageRequest createPageRequest(Map<String, String> params) {
        PlayerOrder order = PlayerOrder.valueOf(params.getOrDefault("order", PlayerOrder.ID.toString()));
        int pageNumber = Integer.parseInt(params.getOrDefault("pageNumber", "0"));
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "3"));

        return PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
    }
}
