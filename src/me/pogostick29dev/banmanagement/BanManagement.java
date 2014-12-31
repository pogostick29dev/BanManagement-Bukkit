package me.pogostick29dev.banmanagement;

import com.evilmidget38.NameFetcher;
import com.evilmidget38.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

public class BanManagement extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please enter the name or UUID of the player you would like to manage.");
            return true;
        }

        String name, uuid;

        if (args[0].matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")) {
            // They are typing in a UUID.
            try {
                name = new NameFetcher(Arrays.asList(UUID.fromString(args[0]))).call().get(UUID.fromString(args[0]));
            }

            catch (Exception e) {
                e.printStackTrace();
                return true;
            }

            uuid = args[0];
        }

        else {
            // They are typing in a name.
            name = args[0];

            try {
                uuid = new UUIDFetcher(Arrays.asList(args[0])).call().get(args[0]).toString();
            }

            catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }

        // This is a debug line and will be removed or changed eventually.
        sender.sendMessage("Name: " + name + " UUID: " + uuid);

        if (cmd.getName().equalsIgnoreCase("ban")) {
            StringBuilder message = new StringBuilder();

            for (int i = 1; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }

            String msg = message.toString().trim();

            if (msg.equals("")) {
                msg = "You have been banned!";
            }

            // TODO: Push the UUID, ban reason (msg), and time to the Web API.

            sender.sendMessage(ChatColor.GREEN + "You have banned " + name + " for " + msg + "!");

            Player target = Bukkit.getServer().getPlayer(name);

            if (target != null) {
                target.kickPlayer(msg);
            }
        }

        else if (cmd.getName().equalsIgnoreCase("unban")) {
            // TODO: Push the UUID to the Web API.

            sender.sendMessage(ChatColor.GREEN + "You have unbanned " + name + "!");
        }

        else if (cmd.getName().equalsIgnoreCase("checkban")) {
            // TODO: Send a request to the Web API and read the data.
        }

        return true;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        // TODO: Send a request to the Web API to see if the player is banned and why.
    }
}