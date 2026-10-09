package com.oplus.wrapper.util;

public class StatsEvent {

    private StatsEvent() {}

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private Builder() {}

        public StatsEvent build() {
            return new StatsEvent();
        }

        public Builder setAtomId(int atomId) {
            return this;
        }

        public Builder usePooledBuffer() {
            return this;
        }

        public Builder writeBoolean(boolean value) {
            return this;
        }

        public Builder writeInt(int value) {
            return this;
        }

        public Builder writeLong(long value) {
            return this;
        }
    }
}
