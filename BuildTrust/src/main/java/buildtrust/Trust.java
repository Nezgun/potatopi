package buildtrust;



import io.anuke.arc.util.Log;

import java.util.ArrayList;

public class Trust {
    private String uuid;
    private int trust;
    private int wins = 0;
    private int losses = 0;
    private String names ="";
    private int kickcount = 0;
    private boolean bannedPast = false;
    private boolean muted = false;
    private int mutecount = 0;
    private int joincount = 0;

    public Trust() {
    }

    public Trust(String uuid, int trust, String name) {
        this.uuid = uuid;
        this.trust = trust;
        assert false;
        this.names = this.names.concat(name).concat(",") ;
    }


    public Trust(int trust, String uuid, int wins, int losses, String names, int kickcount, boolean bannedPast, boolean muted, int mutecount, int joincount) {
        //Use for loading known member
        this.trust = trust;
        this.uuid = uuid;
        this.wins = wins;
        this.losses = losses;
        assert false;
        this.names = this.names.concat(names).concat(",");
        this.kickcount = kickcount;
        this.bannedPast = bannedPast;
        this.muted = muted;
        this.mutecount = mutecount;
        this.joincount = joincount;
        //Lol this constructor though
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setTrust(int trust) {
        this.trust = trust;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public void setKickcount(int kickcount) {
        this.kickcount = kickcount;
    }

    public void setBannedPast(boolean bannedPast) {
        this.bannedPast = bannedPast;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public int getMutecount() {
        return mutecount;
    }

    public void setMutecount(int mutecount) {
        this.mutecount = mutecount;
    }

    public int getJoincount() {
        return joincount;
    }
    public boolean getBannedPast() {
        return this.bannedPast;
    }
    public void setJoincount(int joincount) {
        this.joincount = joincount;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        this.wins++;
    }

    public int getLosses() {
        return losses;
    }

    @Override
    public String toString() {
        return "Trust{" +
                "uuid='" + uuid + '\'' +
                ", trust=" + trust +
                ", wins=" + wins +
                ", losses=" + losses +
                ", names=" + names +
                ", kickcount=" + kickcount +
                ", bannedPast=" + bannedPast +
                ", muted=" + muted +
                ", mutecount=" + mutecount +
                ", joincount=" + joincount +
                '}';
    }

    public void addLoss() {
        this.losses++;
    }

    public String getNames() {
        return names;
    }

    public void addName(String name) {
        if (this.names.contains(name)) {
            this.names = this.names.concat(name + ",");
        }
        else {
            Log.info("Name already accounted for.");
        }
    }

    public int getKickcount() {
        return kickcount;
    }

    public void addKick() {
        this.kickcount++;
    }

    public boolean isBannedPast() {
        return bannedPast;
    }

    public void flagPast() {
        this.bannedPast = true;
    }

    public boolean isMuted() {
        return muted;
    }

    public void mute() {
        this.muted = true;
    }

    public String getUuid() {
        return uuid;
    }
    public void addJoin() {
        this.joincount++;
    }

    public int getTrust() {
        return trust;
    }

    public void reCalcTrust() {
        //Called on reset trust
        this.trust = 500;
        int newtrust = this.kickcount * 50 + this.mutecount * 20;
        newtrust += this.bannedPast ? 50 : 0;
        this.trust -= newtrust;
    }
    public void addTrust(int amount) {
        this.trust = Math.min(this.trust + amount, 1000);
    }
    public void subTrust(int amount) { this.trust = Math.max(this.trust - amount, 0);}

    public String out() {
        StringBuilder tt = new StringBuilder();
        for (String s : names.split(",")) {
            tt.append(s).append(".");
        }
        return this.uuid + "," +
                this.joincount + "," +
                tt + "," +
                wins + "," +
                losses + "," +
                trust + "," +
                mutecount + "," +
                kickcount + "," +
                bannedPast +
                "\n";
    }
}

