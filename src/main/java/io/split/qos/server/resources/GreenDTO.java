package io.split.qos.server.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

public class GreenDTO {

    @JsonProperty
    public final Status status;

    @JsonProperty
    public final Long when;

    @JsonProperty
    public final List<String> failed;

    public static GreenDTO green(Long when) {
        return new GreenDTO(Status.GREEN, Preconditions.checkNotNull(when), Lists.newArrayList());
    }

    public static GreenDTO waiting() {
        return new GreenDTO(Status.WAITING_CYCLE, null, Lists.newArrayList());
    }

    public static GreenDTO failed(List<String> failed) {
        return new GreenDTO(Status.RED, null, Preconditions.checkNotNull(failed));
    }

    private GreenDTO(Status status, Long when, List<String> failed) {
        this.status = Preconditions.checkNotNull(status);
        this.failed = Preconditions.checkNotNull(failed);
        this.when = when;
    }

    public enum Status {
        GREEN,
        WAITING_CYCLE,
        RED,
        ;
    }
}
