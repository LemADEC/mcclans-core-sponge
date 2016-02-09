package nl.riebie.mcclans.clan;

import nl.riebie.mcclans.ClansImpl;
import nl.riebie.mcclans.api.Clan;
import nl.riebie.mcclans.api.ClanPlayer;
import nl.riebie.mcclans.api.Rank;
import nl.riebie.mcclans.api.enums.Permission;
import nl.riebie.mcclans.api.exceptions.NotDefaultImplementationException;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.database.TaskForwarder;
import nl.riebie.mcclans.events.EventDispatcher;
import nl.riebie.mcclans.player.ClanPlayerImpl;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by K.Volkers on 19-1-2016.
 */
public class ClanImpl implements Clan, Cloneable {

    private int clanID;
    private String name;
    private ClanPlayerImpl owner;
    private Location home;
    private int homeSetTimes = 0;
    private long homeLastSetTimeStamp = -1;
    // TODO SPONGE: TextColor
    // private String tagColor = ChatColor.DARK_PURPLE.toString();
    private String tag;
    private HashMap<String, RankImpl> ranks = new HashMap<String, RankImpl>();
    private List<ClanPlayerImpl> members = new ArrayList<ClanPlayerImpl>();
    private List<ClanPlayerImpl> invitedMembers = new ArrayList<ClanPlayerImpl>();
    private List<ClanImpl> allies = new ArrayList<ClanImpl>();
    private List<ClanImpl> invitedAllies = new ArrayList<ClanImpl>();
    private ClanImpl invitingAlly;
    private boolean allowAllyInvites = true;
    private Date creationDate;
    private boolean ffProtection;

    private ClanImpl(Builder builder) {
        this.clanID = builder.clanID;
        this.tag = builder.tag;
        this.name = builder.name;
        this.owner = builder.owner;
        // TODO SPONGE:
        // this.tagColor = builder.tagColor;
        this.allowAllyInvites = builder.acceptAllyInvites;
        this.home = builder.home;
        this.homeLastSetTimeStamp = builder.homeLastSetTimeStamp;
        this.homeSetTimes = builder.homeSetTimes;

        this.creationDate = builder.creationDate;
        this.ffProtection = builder.ffProtection;

    }

    public int getID() {
        return clanID;
    }

    public ClanPlayerImpl getMember(String name) {
        for (ClanPlayerImpl clanPlayer : getMembersImpl()) {
            if (clanPlayer.getName().toLowerCase().equals(name.toLowerCase())) {
                return clanPlayer;
            }
        }
        return null;
    }

    @Override
    public ClanPlayerImpl getMember(UUID uuid) {
        for (ClanPlayerImpl clanPlayer : getMembersImpl()) {
            if (clanPlayer.getUUID().equals(uuid)) {
                return clanPlayer;
            }
        }
        return null;
    }

    public ClanPlayerImpl getInvited(String name) {
        for (ClanPlayerImpl invitedPlayer : getInvitedPlayersImpl()) {
            if (invitedPlayer.getName().toLowerCase().equals(name.toLowerCase())) {
                return invitedPlayer;
            }
        }
        return null;
    }

    public List<ClanPlayerImpl> getInvitedPlayersImpl() {
        return new ArrayList<ClanPlayerImpl>(invitedMembers);
    }

    @Override
    public List<ClanPlayer> getMembers() {
        return new ArrayList<ClanPlayer>(members);
    }

    public List<ClanPlayerImpl> getMembersImpl() {
        return new ArrayList<ClanPlayerImpl>(members);
    }

    @Override
    public boolean isPlayerMember(UUID uuid) {
        for (ClanPlayer clanPlayer : getMembers()) {
            if (clanPlayer.getUUID().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerMember(String playerName) {
        for (ClanPlayer clanPlayer : getMembers()) {
            if (clanPlayer.getName().toLowerCase().equals(playerName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerInvited(String playerName) {
        for (ClanPlayer clanPlayer : getInvitedPlayersImpl()) {
            if (clanPlayer.getName().toLowerCase().equals(playerName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPlayerFriendlyToThisClan(ClanPlayer clanPlayer) {
        if (clanPlayer instanceof ClanPlayerImpl) {
            ClanPlayerImpl clanPlayerImpl = (ClanPlayerImpl) clanPlayer;
            ClanImpl clan = clanPlayerImpl.getClan();
            if (clan != null) {
                if (clan.getTag().equals(tag)) {
                    return true;
                }
                if (isClanAllyOfThisClan(clan.getTag())) {
                    return true;
                }
            }
            return false;
        } else {
            throw new NotDefaultImplementationException(clanPlayer.getClass());
        }
    }

    @Override
    public List<Rank> getRanks() {
        List<Rank> rankList = new ArrayList<Rank>(ranks.values());
        return rankList;
    }

    public List<RankImpl> getRankImpls() {
        List<RankImpl> rankList = new ArrayList<RankImpl>(ranks.values());
        return rankList;
    }

    @Override
    public void setHome(Location<World> location) {
        // TODO SPONGE: clone has protected access in Object..
//        home = location.clone();
        TaskForwarder.sendUpdateClan(this);
    }

    @Override
    public Location<World> getHome() {
        if (home != null) {
            // TODO SPONGE: clone has protected access in Object..
//            return home.clone();
            return null;
        } else {
            return null;
        }
    }

    public int getHomeSetTimes() {
        return homeSetTimes;
    }

    public long getHomeSetTimeStamp() {
        return homeLastSetTimeStamp;
    }

    public int increaseHomeSetTimes() {
        homeSetTimes++;
        TaskForwarder.sendUpdateClan(this);
        return homeSetTimes;
    }

    public void setHomeSetTimes(int homeSetTimes) {
        this.homeSetTimes = homeSetTimes;
        TaskForwarder.sendUpdateClan(this);
    }

    public void setHomeSetTimeStamp(long homeLastSetTimeStamp) {
        this.homeLastSetTimeStamp = homeLastSetTimeStamp;
        TaskForwarder.sendUpdateClan(this);
    }

    @Override
    public int getKills() {
        int kills = 0;
        for (ClanPlayerImpl member : members) {
            kills += member.getKills();
        }
        return kills;
    }

    @Override
    public int getKillsHigh() {
        int kills = 0;
        for (ClanPlayerImpl member : members) {
            kills += member.getKillsHigh();
        }
        return kills;
    }

    @Override
    public int getKillsMedium() {
        int kills = 0;
        for (ClanPlayerImpl member : members) {
            kills += member.getKillsMedium();
        }
        return kills;
    }

    @Override
    public int getKillsLow() {
        int kills = 0;
        for (ClanPlayerImpl member : members) {
            kills += member.getKillsLow();
        }
        return kills;
    }

    @Override
    public int getDeaths() {
        int deaths = 0;
        for (ClanPlayerImpl member : members) {
            deaths += member.getDeaths();
        }
        return deaths;
    }

    @Override
    public int getDeathsHigh() {
        int deaths = 0;
        for (ClanPlayerImpl member : members) {
            deaths += member.getDeathsHigh();
        }
        return deaths;
    }

    @Override
    public int getDeathsMedium() {
        int deaths = 0;
        for (ClanPlayerImpl member : members) {
            deaths += member.getDeathsMedium();
        }
        return deaths;
    }

    @Override
    public int getDeathsLow() {
        int deaths = 0;
        for (ClanPlayerImpl member : members) {
            deaths += member.getDeathsLow();
        }
        return deaths;
    }

    @Override
    public double getKDR() {
        double kdr = 0;
        double killsWeighted = 0;
        double deathsWeighted = 0;
        for (ClanPlayerImpl member : members) {
            killsWeighted += member.getKillsWeighted();
            deathsWeighted += member.getDeathsWeighted();
        }
        if (members.size() > 0) {
            if (deathsWeighted < 1) {
                deathsWeighted = 1;
            }
            kdr = killsWeighted / deathsWeighted;
            int ix = (int) (kdr * 10.0); // scale it
            double dbl2 = ((double) ix) / 10.0;
            return dbl2;
        } else {
            return 0;
        }
    }

    @Override
    public void setOwner(ClanPlayer clanPlayer) {
        if (clanPlayer instanceof ClanPlayerImpl) {
            ClanPlayerImpl clanPlayerImpl = (ClanPlayerImpl) clanPlayer;
            EventDispatcher.getInstance().dispatchClanOwnerChangeEvent(this, owner, clanPlayerImpl);
            this.owner = clanPlayerImpl;
            TaskForwarder.sendUpdateClan(this);
        } else {
            throw new NotDefaultImplementationException(clanPlayer.getClass());
        }
    }

    // Use for restoring clan object from database/xml
    public void setLoadedOwner(ClanPlayerImpl clanPlayer) {
        this.owner = clanPlayer;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        TaskForwarder.sendUpdateClan(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getTagColored() {
        // TODO SPONGE: TextColor
//        ChatColor colonColor = ChatColor.GRAY;
//        if (Configuration.useColoredTagsBasedOnClanKDR) {
//            ClanImpl firstClan = ClansImpl.getInstance().getFirstClan();
//            ClanImpl secondClan = ClansImpl.getInstance().getSecondClan();
//            ClanImpl thirdClan = ClansImpl.getInstance().getThirdClan();
//            if (firstClan != null && firstClan.getTag().equalsIgnoreCase(getTag())) {
//                colonColor = ChatColor.DARK_RED;
//            } else if (secondClan != null && secondClan.getTag().equalsIgnoreCase(getTag())) {
//                colonColor = ChatColor.GOLD;
//            } else if (thirdClan != null && thirdClan.getTag().equalsIgnoreCase(getTag())) {
//                colonColor = ChatColor.DARK_BLUE;
//            }
//        }
//        return colonColor + "[" + getTagColor() + getTag() + colonColor + "]" + ChatColor.RESET;
        return null;
    }

    @Override
    public RankImpl getRank(String rank) {
        return ranks.get(rank.toLowerCase());
    }

    public void addRank(String name) {
        RankImpl rank = new RankImpl.Builder(ClansImpl.getInstance().getNextAvailableRankID(), name).build();
        ranks.put(name.toLowerCase(), rank);
        TaskForwarder.sendInsertRank(getID(), rank);
        TaskForwarder.sendUpdateClan(this);
    }

    public void addRank(Rank rank) {
        if (rank instanceof RankImpl) {
            RankImpl rankImpl = (RankImpl) rank;
            ranks.put(rank.getName().toLowerCase(), rankImpl);
            TaskForwarder.sendInsertRank(getID(), rankImpl);
            TaskForwarder.sendUpdateClan(this);
        } else {
            throw new NotDefaultImplementationException(rank.getClass());
        }
    }

    public void removeRank(String name) {
        RankImpl rank = getRank(name);
        if (rank != null) {
            ranks.remove(name.toLowerCase());
            TaskForwarder.sendDeleteRank(rank.getID());
        }
    }

    @Override
    public void renameRank(String oldName, String newName) {
        RankImpl rank = getRank(oldName);
        ranks.remove(oldName.toLowerCase());
        rank.setName(newName);
        this.ranks.put(newName.toLowerCase(), rank);
        TaskForwarder.sendUpdateRank(rank);
    }

    @Override
    public boolean containsRank(String rankName) {
        return this.ranks.containsKey(rankName.toLowerCase());
    }

    public void addMember(ClanPlayer player) {
        if (player instanceof ClanPlayerImpl) {
            ClanPlayerImpl clanPlayerImpl = (ClanPlayerImpl) player;
            members.add(clanPlayerImpl);
            TaskForwarder.sendUpdateClanPlayer(clanPlayerImpl);
            EventDispatcher.getInstance().dispatchClanMemberJoinEvent(this, clanPlayerImpl);
        } else {
            throw new NotDefaultImplementationException(player.getClass());
        }
    }

    public void removeMember(String playerName) {
        ClanPlayerImpl member = getMember(playerName);
        if (member != null) {
            member.setClan(null);

            member.setRank(null);

            members.remove(member);
            // TODO SPONGE:
//            EventDispatcher.getInstance().dispatchClanMemberLeaveEvent(this, member);
            TaskForwarder.sendUpdateClanPlayer(member);
        }
    }

    public void addInvitedPlayer(ClanPlayer player) {
        if (player instanceof ClanPlayerImpl) {
            invitedMembers.add((ClanPlayerImpl) player);
        } else {
            throw new NotDefaultImplementationException(player.getClass());
        }
    }

    public void removeInvitedPlayer(String playerName) {
        ClanPlayerImpl invitedPlayer = getInvited(playerName);
        if (invitedPlayer != null) {
            invitedMembers.remove(invitedPlayer);
        }
    }

    @Override
    public ClanPlayerImpl getOwner() {
        return owner;
    }

    @Override
    public String getTagColor() {
        // TODO SPONGE: TextColor
        // return this.tagColor;
        return null;
    }

    @Override
    public boolean setTagColor(String chatColor) {
        // TODO SPONGE:
//        this.tagColor = chatColor;
        TaskForwarder.sendUpdateClan(this);
        return true;
    }

    public boolean setTag(String newTag) {
        // TODO Maybe not allow changing of tags as discussed
        TaskForwarder.sendUpdateClan(this);
        return false;
    }

    @Override
    public void sendMessage(Text... message) {
        for (ClanPlayerImpl clanPlayer : getMembersImpl()) {
            clanPlayer.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(Permission permission, Text... message) {
        for (ClanPlayerImpl clanPlayer : getMembersImpl()) {
            if (clanPlayer.getRank().hasPermission(permission)) {
                clanPlayer.sendMessage(message);
            }
        }
    }

    public void sendClanMessage(String sendingPlayerName, String sendingPlayerRank, String message) {

        Text textMessage = Text.join(
                Text.builder("[").color(TextColors.GRAY).build(),
                Text.builder("CC").color(TextColors.DARK_GREEN).build(),
                Text.builder("] [").color(TextColors.GRAY).build(),
                Text.builder(sendingPlayerRank).color(TextColors.BLUE).build(),
                Text.builder("] ").color(TextColors.GRAY).build(),
                Text.builder(sendingPlayerName + ": ").color(TextColors.DARK_GREEN).build(),
                Text.builder(message).color(TextColors.YELLOW).build()
        );

        sendMessage(textMessage);
    }

    public void sendAllyMessage(String sendingPlayerName, Text sendingPlayerClanTagColored, String message) {

        Text textMessage = Text.join(
                Text.builder("[").color(TextColors.GRAY).build(),
                Text.builder("AC").color(TextColors.DARK_GREEN).build(),
                Text.builder("] [").color(TextColors.GRAY).build(),
                sendingPlayerClanTagColored,
                Text.builder("] ").color(TextColors.GRAY).build(),
                Text.builder(sendingPlayerName + ": ").color(TextColors.DARK_GREEN).build(),
                Text.builder(message).color(TextColors.GOLD).build()
        );

        sendMessage(textMessage);
    }

    @Override
    public List<Clan> getAllies() {
        return new ArrayList<Clan>(allies);
    }

    public List<ClanImpl> getAlliesImpl() {
        return new ArrayList<ClanImpl>(allies);
    }

    public void addAlly(Clan clan) {
        if (clan instanceof ClanImpl) {
            ClanImpl clanImpl = (ClanImpl) clan;
            if (!allies.contains(clanImpl)) {
                allies.add(clanImpl);
                TaskForwarder.sendInsertClanAlly(getID(), clanImpl.getID());
            }
        } else {
            throw new NotDefaultImplementationException(clan.getClass());
        }
    }

    @Override
    public ClanImpl getAlly(String clanTag) {
        for (ClanImpl ally : getAlliesImpl()) {
            if (ally.getTag().toLowerCase().equals(clanTag.toLowerCase())) {
                return ally;
            }
        }
        return null;
    }

    public void removeAlly(String clanTag) {
        ClanImpl ally = getAlly(clanTag);
        if (ally != null) {
            allies.remove(ally);
            TaskForwarder.sendDeleteClanAlly(getID(), ally.getID());
        }
    }

    public List<ClanImpl> getInvitedAlliesImpl() {
        return new ArrayList<ClanImpl>(invitedAllies);
    }

    public void addInvitedAlly(ClanImpl clan) {
        if (!invitedAllies.contains(clan)) {
            invitedAllies.add(clan);
        }
    }

    public ClanImpl getInvitedAlly(String clanTag) {
        for (ClanImpl ally : getInvitedAlliesImpl()) {
            if (ally.getTag().toLowerCase().equals(clanTag.toLowerCase())) {
                return ally;
            }
        }
        return null;
    }

    public void removeInvitedAlly(String clanTag) {
        ClanImpl ally = getAlly(clanTag);
        if (ally != null) {
            invitedAllies.remove(ally);
        }
    }

    public void setInvitingAlly(ClanImpl clan) {
        invitingAlly = clan;
    }

    public void resetInvitingAlly() {
        invitingAlly = null;
    }

    public ClanImpl getInvitingAlly() {
        return invitingAlly;
    }

    @Override
    public void setAllowingAllyInvites(boolean inviteable) {
        this.allowAllyInvites = inviteable;
        TaskForwarder.sendUpdateClan(this);
    }

    @Override
    public boolean isAllowingAllyInvites() {
        return allowAllyInvites;
    }

    @Override
    public boolean isClanAllyOfThisClan(String clanTag) {
        for (ClanImpl ally : getAlliesImpl()) {
            if (ally.getTag().toLowerCase().equals(clanTag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isClanAllyOfThisClan(Clan clan) {
        for (ClanImpl ally : getAlliesImpl()) {
            if (ally.equals(clan)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public String getCreationDateUserFriendly() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        return dateFormat.format(creationDate);
    }

    @Override
    public boolean isFfProtected() {
        return ffProtection;
    }

    @Override
    public void setFfProtection(boolean ffProtection) {
        this.ffProtection = ffProtection;
        TaskForwarder.sendUpdateClan(this);
    }

    public void setupDefaultRanks() {
        RankImpl owner = RankFactory.getInstance().createOwner();
        RankImpl member = RankFactory.getInstance().createMember();
        RankImpl recruit = RankFactory.getInstance().createRecruit();
        ranks.put(recruit.getName().toLowerCase(), recruit);
        ranks.put(owner.getName().toLowerCase(), owner);
        addRank(member);
    }

    @Override
    public int getMemberCount() {
        return members.size();
    }

    @Override
    public ClanImpl clone() {
        ClanImpl clone = null;
        try {
            Object object = super.clone();
            if (object instanceof ClanImpl) {
                clone = (ClanImpl) object;
            }
        } catch (CloneNotSupportedException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }

        return clone;
    }

    public static class Builder {
        private int clanID;
        private String name;
        private ClanPlayerImpl owner;
        private Location home;
        private int homeSetTimes = 0;
        private long homeLastSetTimeStamp = -1;
        // TODO SPONGE: TextColor
        // private String tagColor = ChatColor.DARK_PURPLE.toString();
        private String tag;
        private boolean acceptAllyInvites = true;
        private Date creationDate = new Date();
        private boolean ffProtection = true;

        public Builder(int clanID, String tag, String name) {
            this.tag = tag;
            this.name = name;
            this.clanID = clanID;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder owner(ClanPlayerImpl owner) {
            this.owner = owner;
            return this;
        }

        public Builder home(Location home) {
            this.home = home;
            return this;
        }

        public Builder homeSetTimes(int times) {
            this.homeSetTimes = times;
            return this;
        }

        public Builder homeLastSetTimeStamp(long timeStamp) {
            this.homeLastSetTimeStamp = timeStamp;
            return this;
        }

        public Builder tagColor(String tagColor) {
            // TODO SPONGE: TextColor
//            this.tagColor = tagColor;
//            return this;
            return this;
        }

        public Builder acceptAllyInvites(boolean acceptAllyInvites) {
            this.acceptAllyInvites = acceptAllyInvites;
            return this;
        }

        public Builder creationTime(long creationTime) {
            this.creationDate = new Date(creationTime);
            return this;
        }

        public Builder ffProtection(boolean ffProtection) {
            this.ffProtection = ffProtection;
            return this;
        }

        public ClanImpl build() {
            return new ClanImpl(this);
        }
    }
}