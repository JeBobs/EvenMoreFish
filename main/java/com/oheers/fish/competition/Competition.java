package com.oheers.fish.competition;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Competition {

    // fisher, fish length
    HashMap<Player, Fish> leaderboard = new HashMap<>();

    Bar bar;

    // (seconds) the length of the competition
    int duration;

    public Competition(int duration) {
        this.duration = duration;
    }

    public void start() {
        announce();
        bar = new Bar(this.duration);
        EvenMoreFish.active = this;
    }

    public void end() {
        bar.removeAllPlayers();
        EvenMoreFish.active = null;
        announceWinners();
    }

    public Bar getBar() {
        return bar;
    }

    private void announce() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.competitionStart));
        }
    }

    public void runLeaderboardScan(Player fisher, Fish fish) {
        // stuff
        for (Player holder : leaderboard.keySet()) {
            if (holder == fisher) {
                if (fish.getLength() > leaderboard.get(holder).getLength()) {
                    leaderboard.remove(holder);
                    leaderboard.put(holder, fish);
                    leaderboard = sortByValue(leaderboard);
                    return;
                }
                return;
            }
        }
        leaderboard.put(fisher, fish);
        leaderboard = sortByValue(leaderboard);
    }

    private static HashMap<Player, Fish> sortByValue(Map<Player, Fish> unsortMap) {

        // Convert Map to List of Map
        List<Map.Entry<Player, Fish>> list =
                new LinkedList<>(unsortMap.entrySet());

        // Sort list with Collections.sort(), provide a custom Comparator
        // Try switch the o1 o2 position for a different order
        list.sort((o2, o1) -> (o1.getValue().getLength()).compareTo(o2.getValue().getLength()));

        // Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        HashMap<Player, Fish> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Player, Fish> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private void announceWinners() {
        StringBuilder message = new StringBuilder();
        int position = 1;

        for (Player player : leaderboard.keySet()) {

            giveRewards(position, player);

            // creates a limit for the number of players to be shown
            if (position > Messages.leaderboard_count) {
                break;
            }

            Fish f = leaderboard.get(player);

            message.append(new Message()
                    .setMSG(Messages.leaderboard)
                    .setPlayer(player.getName())
                    .setColour(f.getRarity().getColour())
                    .setLength(f.getLength().toString())
                    .setFishCaught(f.getName())
                    .setRarity(f.getRarity().getValue())
                    .setPosition(Integer.toString(position))
                    .toString());
            message.append("\n");
            position++;
        }
        if (message.toString().equals("")) {
            // nobody fished a fish
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.competitionEnd));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.noWinners));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.noFish));
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.competitionEnd));
                player.sendMessage(message.toString());
                // checks if the specific player didn't fish a fish
                if (!leaderboard.containsKey(player)) player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.noFish));
            }
        }
    }

    private void giveRewards(Integer position, Player player) {
        // is there a reward set for the position player came?#
        if (EvenMoreFish.rewards.containsKey(position)) {
            // acts on each reward set for the player's position
            for (Reward r : EvenMoreFish.rewards.get(position)) {
                r.run(player);
            }
        }
    }
}