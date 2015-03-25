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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

public class BanManagement extends JavaPlugin implements Listener {

    private String ip, key;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.ip = getConfig().getString("ip");
        this.key = getConfig().getString("key");

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please enter the name or UUID of the player you would like to manage.");
            return true;
        }

        final String name, uuid;

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

            try {
                HttpURLConnection connection = connect("add", "uuid=" + uuid + "&reason=" + msg.replaceAll(" ", "%20"));
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String data = in.readLine();

                if (data == null || data.equals("key")) {
                    sender.sendMessage(ChatColor.RED + "BanManagement's config file is not configured properly.");
                }

                else {
                    sender.sendMessage(ChatColor.GREEN + "You have banned " + name + " for " + msg + "!");

                    Player target = Bukkit.getServer().getPlayer(name);

                    if (target != null) {
                        target.kickPlayer(msg);
                    }
                }
            }

            catch (FileNotFoundException e) {
                sender.sendMessage(ChatColor.RED + "BanManagement's config file is not configured properly.");
                return true;
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if (cmd.getName().equalsIgnoreCase("unban")) {
            try {
                HttpURLConnection connection = connect("remove", "uuid=" + uuid);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String data = in.readLine();

                if (data == null || data.equals("key")) {
                    sender.sendMessage(ChatColor.RED + "BanManagement's config file is not configured properly.");
                }

                else {
                    sender.sendMessage(ChatColor.GREEN + "You have unbanned " + name + "!");
                }
            }

            catch (FileNotFoundException e) {
                sender.sendMessage(ChatColor.RED + "BanManagement's config file is not configured properly.");
                return true;
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if (cmd.getName().equalsIgnoreCase("checkban")) {
            try {
                HttpURLConnection connection = connect("checkban", "uuid=" + uuid);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String data = in.readLine();

                if (data == null || data.equals("key")) {
                    sender.sendMessage(ChatColor.RED + "BanManagement's config file is not configured properly.");
                }

                else {
                    sender.sendMessage(ChatColor.GREEN + data);
                }
            }

            catch (FileNotFoundException e) {
                sender.sendMessage(ChatColor.RED + "BanManagement's config file is not configured properly.");
                return true;
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        try {
            HttpURLConnection connection = connect("get", "uuid=" + e.getPlayer().getUniqueId());
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String data = in.readLine();

            if (data == null || data.equals("key")) {
                e.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + "BanManagement's config file is not configured properly.");
            }
        }

        catch (FileNotFoundException ex) {
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + "BanManagement's config file is not configured properly.");
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HttpURLConnection connect(String api, String args) {
        try {
            args = "key=" + key + "&" + args;
            byte[] data = args.getBytes(Charset.forName("UTF-8"));
            int dataLength = args.length();
            String request = "http://" + ip + "/api/" + api + ".php";
            URL url = new URL(request);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(dataLength));

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(data);
            out.flush();
            out.close();

            return connection;
        }

        catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}