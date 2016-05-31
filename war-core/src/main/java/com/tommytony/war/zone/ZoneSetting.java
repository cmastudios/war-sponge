package com.tommytony.war.zone;

/**
 * Settings that can be changed on a per-zone basis.
 * Some can be overridden per-team.
 */
public enum ZoneSetting {
    /**
     * Maximum players in a zone or team. Zone max is still observed even if team settings allow for more.
     */
    MAXPLAYERS(Integer.class, 10, true),
    /**
     * Disables starting new games in the zone, and allows zone makers to build.
     */
    EDITING(Boolean.class, false, false),
    /**
     * How many rounds the zone will run before kicking all players out. Use to encourage playing other zones.
     */
    MAXROUNDS(Integer.class, 5, false),
    /**
     * First team to this many points wins the round.
     */
    MAXPOINTS(Integer.class, 10, true);
    private final Class<?> dataType;
    private final Object defaultValue;
    private final boolean perTeam;

    ZoneSetting(Class<?> dataType, Object defaultValue, boolean perTeam) {
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.perTeam = perTeam;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Class<?> getDataType() {
        return dataType;
    }
}
