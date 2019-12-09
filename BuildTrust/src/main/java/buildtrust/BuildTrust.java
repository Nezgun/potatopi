package buildtrust;

import io.anuke.arc.Events;
import io.anuke.arc.util.CommandHandler;
import io.anuke.arc.util.Log;
import io.anuke.arc.util.Strings;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.Blocks;
import io.anuke.mindustry.entities.type.Player;
import io.anuke.mindustry.game.EventType;
import io.anuke.mindustry.game.EventType.BlockBuildBeginEvent;
import io.anuke.mindustry.game.EventType.WorldLoadEvent;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.plugin.Plugin;
import io.anuke.mindustry.world.Tile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class BuildTrust extends Plugin{
    private int baselocx;
    private int baselocy;
    private static HashMap<Player, Trust> pt = new HashMap<>();

    private static void ls_players() {
        for (Player list :Vars.playerGroup.all()) {
            Log.info(list.name + " UUID: " + list.uuid);
        }
    }
    private static int getguid(Player p) {
        int guid;
        StringBuilder tmp = new StringBuilder();
        int uuidn = p.uuid.hashCode();
        for (String s : p.con.address.split("\\.")) {
            tmp.append(s);
        }
        guid = Integer.parseInt(tmp.toString()) * uuidn;
        return guid < 0 ? guid*-1 : guid;
    }
    private static void loadPlayer(Player p) throws SQLException {
        DerbyDB db = new DerbyDB();
        int guid = getguid(p);
        StringBuilder guidstring = new StringBuilder();
        PreparedStatement guidquery = db.c.prepareStatement("SELECT GUID from TRUSTDB");
        ResultSet guidresult = guidquery.executeQuery();
        /*
        Take address
        Convert to number
        Convert uuid to hashcode
        Multiply hashcode by address converted
        Result should be unique. can do lookup if the guid gotten is different for dealing with VPNs
         */
        while (guidresult.next()) {
            guidstring.append(guidresult.getObject(1).toString()).append(",");
        }
        if(guidstring.toString().contains(String.valueOf(guid))) {
            //Here we test to see if a guid is in the database already
            PreparedStatement trustquery = db.c.prepareStatement("select * from TRUSTDB where GUID = ?"); //select everything in that one
            trustquery.setInt(1, guid);
            ResultSet trustresult = trustquery.executeQuery(); //this is our player's data
            trustresult.next();
            Trust t = new Trust((int)trustresult.getObject(7),
                    (String) trustresult.getObject(2),
                    (int) trustresult.getObject(5),
                    (int) trustresult.getObject(6),
                    (String) trustresult.getObject(4),
                    (int) trustresult.getObject(9),
                    (int) trustresult.getObject(10) == 1,
                    (int) trustresult.getObject(8) > 0,
                    (int) trustresult.getObject(8),
                    (int) trustresult.getObject(3));
            pt.put(p,t);
        }

        else {
            //todo Add unknown guids to the database IF the uuid isn't already in there
            StringBuilder uuidstring = new StringBuilder(); //our uuid search builder
            PreparedStatement uuidquery = db.c.prepareStatement("SELECT UUID from TRUSTDB"); //get all uuids in the database
            ResultSet uuidresult = uuidquery.executeQuery(); //this is our uuid search
            while (uuidresult.next()) {
                for (int i = 1; i <= uuidresult.getMetaData().getColumnCount(); i++) {
                    uuidstring.append(uuidresult.getObject(1).toString()).append(",");
                }
            }
            //Vpn checker, not going to fire ever, but good edge case
            //Made into regular load just off of uuid because for testing it won't matter
            if(uuidstring.toString().contains(p.uuid)) { //Query the search builder for the uuid
                p.sendMessage("You seem to be using a VPN. Some actions may not be available to you.");
                p.sendMessage("To set this IP as your primary ip, run the command /oneip");
                p.sendMessage("You can only use this command once. Contact server admins if there are any errors.");
                PreparedStatement trustquery = db.c.prepareStatement("select * from TRUSTDB where UUID = ?"); //select everything in that one
                trustquery.setString(1, p.uuid);
                ResultSet trustresult = trustquery.executeQuery(); //this is our player's data
                trustresult.next();
                Trust t = new Trust((int)trustresult.getObject(7),
                        (String) trustresult.getObject(2),
                        (int) trustresult.getObject(5),
                        (int) trustresult.getObject(6),
                        (String) trustresult.getObject(4),
                        (int) trustresult.getObject(9),
                        (int) trustresult.getObject(10) == 1,
                        (int) trustresult.getObject(8) > 0,
                        (int) trustresult.getObject(8),
                        (int) trustresult.getObject(3));
                pt.put(p,t);
            }
            else {
                //make new entry for the player
                Log.info("New player inserted into table");
                Trust t =  new Trust(p.uuid, 500, p.name);
                PreparedStatement playerinsert = db.c.prepareStatement("INSERT into TRUSTDB values (?,?,?,?,?,?,?,?,?,?)");

                playerinsert.setInt(1, guid);
                playerinsert.setString(2, t.getUuid());
                playerinsert.setInt(3, t.getJoincount());
                StringBuilder tmp = new StringBuilder();
                for (String s : t.getNames().split("\\.")) {
                    tmp.append(",").append(s);
                }
                playerinsert.setString(4, tmp.toString().substring(1));
                playerinsert.setInt(5, t.getWins());
                playerinsert.setInt(6, t.getLosses());
                playerinsert.setInt(7, t.getTrust());
                playerinsert.setInt(8, t.getMutecount());
                playerinsert.setInt(9, t.getKickcount());
                playerinsert.setInt(10, t.getBannedPast() ? 1 : 0);
                playerinsert.executeUpdate();

                pt.put(p, t);


            }
        }

        db.closeCon();

    }
    private static void savePlayer(Player p) throws SQLException {
        DerbyDB db = new DerbyDB();
        int guid = getguid(p);
        PreparedStatement trustupdate = db.c.prepareStatement("update TRUSTDB set joincount = ?, names = ?, wins = ?, losses = ?, trust = ?, mutes = ?, kicks = ?, bannedprev = ? where GUID = ?"); //select everything in that one
        trustupdate.setInt(1, pt.get(p).getJoincount());
        trustupdate.setString(2, pt.get(p).getNames());
        trustupdate.setInt(3, pt.get(p).getWins());
        trustupdate.setInt(4, pt.get(p).getLosses());
        trustupdate.setInt(5, pt.get(p).getTrust());
        trustupdate.setInt(6, pt.get(p).getMutecount());
        trustupdate.setInt(7, pt.get(p).getKickcount());
        trustupdate.setInt(8, pt.get(p).getBannedPast() ? 1 : 0);
        trustupdate.setInt(9, guid);
        trustupdate.executeUpdate();
        Log.info("Saved a player's data");
        pt.remove(p);
    }

    //register event handlers and create variables in the constructor
    public BuildTrust(){

        Events.on(EventType.PlayerJoin.class, event -> {
            try {
                loadPlayer(event.player);
            } catch (SQLException e) {
                Log.info("Failure to load data.");
                e.printStackTrace();

            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            try {
                savePlayer(event.player);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        //MARKER
        Events.on(EventType.GameOverEvent.class, e -> {

        });

        Events.on(WorldLoadEvent.class, event -> {
            for (Tile[] t: Vars.world.getTiles()) {
                for (Tile tt : t) {
                    if (tt.block().equals(Blocks.coreFoundation) || tt.block().equals(Blocks.coreNucleus) || tt.block().equals(Blocks.coreShard)) {
                        baselocx = tt.x;
                        baselocy = tt.y;
                    }
                }
            }
        });


        Events.on(BlockBuildBeginEvent.class, event -> {
            if (event.tile.entity.block.equals(Blocks.thoriumReactor)) {
                double tx = Math.pow(baselocx - event.tile.x, 2);
                double ty = Math.pow(baselocy - event.tile.y, 2);
                int l = (int) Math.sqrt(tx + ty);
                Call.sendMessage("[scarlet]![orange]![yellow]![] Someone has begun building a reactor " + l + " blocks away from the core!");
                if (l < 40) {
                    Call.sendMessage("[red]The reactor is within explosion radius of the core, someone should stop them!");
                }

            }
        });
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("export", "[String]", "Export all data into a file, optional file name", (args) -> {
            String filename = "trust.txt";
            String tmp;
            if (args.length != 0) {
                filename = args[0];
            }

            DerbyDB db = new DerbyDB();
            PreparedStatement p;
            try {
                p = db.c.prepareStatement("SELECT * from TRUSTDB");
                ResultSet r = p.executeQuery();
                BufferedWriter out = new BufferedWriter(new FileWriter(filename));
                while (r.next()) {
                    tmp = "";
                    for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) {
                        Log.info(r.getObject(i).toString());
                        tmp = tmp.concat(",").concat(r.getObject(i).toString());

                    }
                    out.write(tmp.substring(1).concat("\n"));
                }

                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            db.closeCon();

        });

        handler.register("query", "Gets stuff", args -> {
            DerbyDB db = new DerbyDB();
            try {
                PreparedStatement p = db.c.prepareStatement("SELECT * from TRUSTDB");
                ResultSet r = p.executeQuery();
                while (r.next()) {
                    for (int i = 1; i <= r.getMetaData().getColumnCount() ; i++) {
                        Log.info(r.getMetaData().getCatalogName(i)+ " " + r.getObject(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            db.closeCon();
        });
        handler.register("trustall", "Returns trust rating and IDs for all online players.", args -> {
            for (Player p : Vars.playerGroup.all()) {
                Log.info( "ID: " + p.id + " Name: " + p.name);
            }
        });
        handler.register("addtrust", "[player] [int]",  "Manually add trust to a player. Cannot go above 1000. Can use UUIDs", (args) -> {

            if (args.length == 0) {
                ls_players();
            }
            else {
                Player p = null;
                for (Player ls : Vars.playerGroup.all()) {
                    if (ls.name.equalsIgnoreCase(args[0]) || ls.uuid.equalsIgnoreCase(args[0])) {
                        p = ls;

                    }
                }
                if (p != null) {
                    pt.get(p).addTrust(Integer.parseInt(args[1]));
                    Log.info("Gave" + p.name + " "  + Integer.parseInt(args[1]) + " trust.");
                }
                else {
                    Log.info("Player not found.");
                }

            }
        });
        handler.register("subtrust", "[player] [int]",  "Manually take trust from a player. Cannot go below 0. Can use UUIDs", (args) -> {

            if (args.length == 0) {
                ls_players();
            }
            else {
                Player p = null;
                for (Player ls : Vars.playerGroup.all()) {
                    if (ls.name.equalsIgnoreCase(args[0]) || ls.uuid.equalsIgnoreCase(args[0])) {
                        p = ls;

                    }
                }
                if (p != null) {
                    pt.get(p).subTrust(Integer.parseInt(args[1]));
                    Log.info("Took" + Integer.parseInt(args[1]) + " trust from " + p.name  + ".");
                }
                else {
                    Log.info("Player not found.");
                }

            }
        });
        handler.register("restrust", "[player]", "Recalculate trust for player. Value depends on past punishments. Can use UUIDs", (args) -> {
           if (args.length == 0) {
               ls_players();
           }
           else {
               Player p = null;
               for (Player ls : Vars.playerGroup.all()) {
                   if (ls.name.equalsIgnoreCase(args[0]) || ls.uuid.equalsIgnoreCase(args[0]) || ls.id == Integer.parseInt(args[0])) {
                       p = ls;
                   }
               }
               if (p != null) {
                   pt.get(p).reCalcTrust();
                   Log.info("Trust recalculated. Trust is now: " + pt.get(p).getTrust());
               }
           }
        });
    }



    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("trust", "[player]", "Displays general trust for a player.", (args, player) -> {
            if (args.length == 0) {
                StringBuilder builder = new StringBuilder();
                builder.append("[orange]Online player list: \n");
                for (Player p : Vars.playerGroup.all()) {
                    if (!p.isAdmin && p.con != null) {
                        builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id).append("\n");
                    }
                }
                player.sendMessage(builder.toString());
            }
            else {
                Player target = null;
                if (args[0].length() > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))) {
                    int id = Strings.parseInt(args[0].substring(1));
                    target = Vars.playerGroup.getByID(id);
                }
                else {
                    for (Player p : Vars.playerGroup.all()) {
                        if (args[0].equals(p.name)) {
                            target = p;
                        }
                    }
                }
                if (target != null && !target.isAdmin) {
                    player.sendMessage(pt.get(target).getTrust() < 500 ? "This player has a lower than average trust value..." : "This player has a higher than average trust value!");
                }
                else {
                    player.sendMessage("Player not found.");
                }
            }
        });
        /*
        //register a simple reply command
        handler.<Player>register("reply", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
            player.sendMessage("You said: [accent] " + args[0]);
        });

        //register a whisper command which can be used to send other players messages
        handler.<Player>register("whisper", "<player> <text...>", "Whisper text to another player.", (args, player) -> {
            //find player by name
            Player other = Vars.playerGroup.find(p -> p.name.equalsIgnoreCase(args[0]));

            //give error message with scarlet-colored text if player isn't found
            if(other == null){
                player.sendMessage("[scarlet]No player by that name found!");
                return;
            }

            //send the other player a message, using [lightgray] for gray text color and [] to reset color
            other.sendMessage("[lightgray](whisper) " + player.name + ":[] " + args[1]);
        });
         */

    }
}
