package com.hamdam.hamdam.model;
import android.text.TextUtils;

/**
 * StaticFact class to store static content. A static content 'object' is defined as having
 * a set of text including a itemHeading, subheading, and body text.
 * A StaticFact object is built with the Builder pattern by constructing a {@link FactBuilder} object.
 */
public class StaticFact {
    private String mHeading, mSubheading, mBody;
    private boolean newTopic;
    private TOPIC_TYPES topicType;

    public enum TOPIC_TYPES {
        HEALTH,
        MARRIAGE_RIGHTS,
        ABOUT,
        FAQ
    }

    private StaticFact(FactBuilder builder) {
        this.mHeading = builder.mHeading;
        this.mSubheading = builder.mSubheading;
        this.mBody = builder.mBody;
        this.newTopic = builder.newTopic;
        this.topicType = builder.topicType;
    }

    public String getHeading() {
        return mHeading;
    }

    public String getSubheading() {
        return mSubheading;
    }

    public String getBody() {
        return mBody;
    }

    public boolean hasHeading() { return (mHeading == null || TextUtils.isEmpty(mHeading)); }

    public boolean isNewTopic() {
        return newTopic;
    }

    public TOPIC_TYPES getTopicType() { return this.topicType; }


    /*
         * Begin constructing the builder, requiring mandatory mHeading and formatted mSubheading
         * pattern.
         */
    public static BuildInterface builder() {
        return new FactBuilder();
    }

    /*
     * Builder for StaticFact objects.
     *
     */
    public static class FactBuilder implements BuildInterface {
        private String mHeading, mSubheading, mBody;
        private boolean newTopic;
        private TOPIC_TYPES topicType;

        @Override
        public StaticFact build() {
            return new StaticFact(this);
        }

        @Override
        public BuildInterface heading(String title) {
            this.mHeading = title;
            return this;
        }

        @Override
        public BuildInterface body(String bodyText) {
            this.mBody = bodyText;
            return this;
        }

        @Override
        public BuildInterface subheading(String subHeadingText) {
            this.mSubheading = subHeadingText;
            return this;
        }

        @Override
        public BuildInterface setNewTopic(boolean isNewTopic) {
            this.newTopic = isNewTopic;
            return this;
        }

        @Override
        public BuildInterface setTopicType(TOPIC_TYPES type) {
            this.topicType = type;
            return this;
        }

    }

    public interface BuildInterface {
        StaticFact build();
        BuildInterface heading(String titleText);
        BuildInterface subheading(String subHeadingText);
        BuildInterface body(String bodyText);
        BuildInterface setNewTopic(boolean isNewTopic);
        BuildInterface setTopicType(TOPIC_TYPES type);
    }
}
