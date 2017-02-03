
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by ganxiaojian on 16/12/13.
 */
@Document(collection = "scheduleLeaderEntity")
public class ScheduleLeaderEntity extends BaseEntity {

    /**
     * The last ping.
     */
    private long lastPing;

    /**
     * is leader or not.
     */
    private boolean isLeader = false;

    public long getLastPing() {
        return lastPing;
    }

    public void setLastPing(long lastPing) {
        this.lastPing = lastPing;
    }


    public boolean getIsLeader() {
        return isLeader;
    }

    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }

    @Override
    public String toString() {
        return "ScheduleLeaderEntity{" + ", " +
                "id='" + id + '\'' +
                ", lastPing=" + lastPing +
                ", isLeader=" + isLeader +
                '}';
    }
}