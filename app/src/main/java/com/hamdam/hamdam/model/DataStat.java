package com.hamdam.hamdam.model;

import com.hamdam.hamdam.enums.StatusEnum;

/**
 * Class to hold information pulled from database on different topics.
 * DataStat is built by constructing a {@link DataStatsBuilder} object.
 */
public class DataStat {
    private String title, description, formattedValue;
    private int value;
    private StatusEnum.StatusMain type;
    private int icon;

    private DataStat(DataStatsBuilder builder) {
        this.title = builder.mTitle;
        this.type = builder.mType;
        this.description = builder.mDescription;
        this.formattedValue = builder.mFormattedValue;
        this.value = builder.mValue;
        this.icon = builder.mIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedValue() {
        return formattedValue;
    }

    public int getValue() {
        return value;
    }

    public StatusEnum.StatusMain getType() {
        return type;
    }

    public int getIcon() { return icon; }

    /*
     * Begin constructing the builder, requiring mandatory title and formatted description
     * pattern.
     */
    public static BuildInterface builder() {
        return new DataStatsBuilder();
    }

    /*
     * Builder for DataStat objects.
     *
     */
    public static class DataStatsBuilder implements BuildInterface {
        private String mTitle, mFormattedValue, mDescription;
        private StatusEnum.StatusMain mType;
        private int mValue;
        private int mIcon;

        @Override
        public DataStat build() {
            return new DataStat(this);
        }

        @Override
        public BuildInterface title(String title) {
            this.mTitle = title;
            return this;
        }

        @Override
        public BuildInterface formattedValue(String stringValue) {
            this.mFormattedValue = stringValue;
            return this;
        }

        @Override
        public BuildInterface value(int value) {
            this.mValue = value;
            return this;
        }

        @Override
        public BuildInterface description(String description) {
            this.mDescription = description;
            return this;
        }

        @Override
        public BuildInterface type(StatusEnum.StatusMain type) {
            this.mType = type;
            return this;
        }

        @Override
        public BuildInterface icon(int iconId) {
            this.mIcon = iconId;
            return this;
        }
    }

    public void setValue(int value) {
        this.value = value;
    }

    public interface BuildInterface {
        DataStat build();
        BuildInterface type(StatusEnum.StatusMain type);
        BuildInterface value(int value);
        BuildInterface formattedValue(String formattedValue);
        BuildInterface title(String title);
        BuildInterface description(String description);
        BuildInterface icon(int iconId);
    }
}
