
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ganxiaojian on 16/12/13.
 */
@Component
public class ScheduleLeaderProcesser {

    /**
     * the logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScheduleLeaderProcesser.class);

    /**
     * expired time in milliseconds as a leader.
     */
    private static long expiredTime = Long.parseLong(Utils.getEnv(Constants.CRON_SCHEDULE_LEADER_EXPIRED_MILLISE_TIME, "35000"));

    /**
     * the scheduleLeader repository.
     */
    @Autowired
    private ScheduleLeaderRepository repository;

    @Autowired
    private Environment env;

    /**
     * Gets repository
     * @return BaseRepository<S,T>
     */
    protected BaseRepository<ScheduleLeaderEntity, String> getRepository()  {
        return repository;
    }


    private String getNodeName() throws UnknownHostException {
        String applicationName = env.getProperty("spring.config.name","CloudChef1");
        String ip = InetAddress.getLocalHost().getHostAddress();
        String id = applicationName + ":" +ip;
        return id;
    }


    public void createOrUpdateNode() {
        SecurityContext origContext = CloudChefContext.setAuthentication1(CloudChefContext.createSolutionAuthentication());
        try{
            String id = getNodeName();
            ScheduleLeaderEntity node = repository.findOne(id);
            if (node == null) {
                createNode(id);
            } else {
                updateNode(node);
            }
        }catch(Exception e) {
            logger.error("error happend when execute createOrUpdateNode:{}",e.getLocalizedMessage());
        }finally {
            CloudChefContext.setContext(origContext);
        }
    }

    /**
     * Elects a leader for the cluster to execute the tasks.
     */
    public void electionLeaderShip() {
        SecurityContext origContext = CloudChefContext.setAuthentication1(CloudChefContext.createSolutionAuthentication());
        try{
            // get allNodes and the aliveNodes
            final List<ScheduleLeaderEntity> allNodes = repository.findAll();
            List<ScheduleLeaderEntity> aliveNodes = null;
            if (CollectionUtils.isEmpty(allNodes)) {
                logger.error("allNodes should not be null");
                return;
            } else {
                aliveNodes = getAliveNodes(allNodes);
            }
            if (CollectionUtils.isEmpty(aliveNodes)) {
                logger.error("aliveNodes should not be null");
                return;
            }

            // get leader
            // is there a valid leader
            ScheduleLeaderEntity leader = getLeader(allNodes);
            if (leader != null && aliveNodes.contains(leader)) {
                return;
            }
            // no exist, then get the min-lastPing as the new leader
            leader = getMinNodeAsLeader(aliveNodes);

            // set and save the new leader
            setLeaderFlagAsFalse(allNodes);
            leader.setIsLeader(Boolean.TRUE);
            allNodes.add(leader);
            repository.save(allNodes);
        }catch(Exception e) {
            logger.error("error happend when execute electionLeaderShip:{}",e.getLocalizedMessage());
        }finally {
            CloudChefContext.setContext(origContext);
        }

    }

    /**
     * Returns the leader if exists
     * @param list the list
     * @return the leader
     */
    private ScheduleLeaderEntity getLeader(final List<ScheduleLeaderEntity> list) {
        for (ScheduleLeaderEntity applicationNode : list) {
            if (applicationNode.getIsLeader()) {
                return applicationNode;
            }
        }
        return null;
    }


    /**
     * Creates the node
     */
    private void createNode(String nodeName) {
        final ScheduleLeaderEntity node = new ScheduleLeaderEntity();
        long time = System.currentTimeMillis();
        node.setId(nodeName);
        node.setLastPing(time);
        node.setIsLeader(CollectionUtils.isEmpty(repository.findAll()));
        repository.save(node);
    }

    /**
     * Updates the node
     */
    private void updateNode(final ScheduleLeaderEntity node) {
        long time = System.currentTimeMillis();
        node.setLastPing(time);
        repository.save(node);
    }

    /**
     * Returns the alive nodes.
     * @param list the list
     * @return the alive nodes
     */
    private List<ScheduleLeaderEntity> getAliveNodes(final List<ScheduleLeaderEntity> list) {
        final List<ScheduleLeaderEntity> aliveNodes = new LinkedList<>();
        for (ScheduleLeaderEntity applicationNode : list) {
            if (isAliveNode(applicationNode.getLastPing())) {
                aliveNodes.add(applicationNode);
            }
        }
        return aliveNodes;
    }

    /**
     * node is alive or not
     * @param lastPingTime
     * @return
     */
    private boolean isAliveNode(long lastPingTime){
        long currentTime = System.currentTimeMillis();
        boolean isNotExpired = (currentTime - lastPingTime < expiredTime);
        if(isNotExpired){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the min-lastPing node which is alive as the new leader.
     * @param list the list
     * @return the new leader
     */
    private ScheduleLeaderEntity getMinNodeAsLeader(final List<ScheduleLeaderEntity> list) {
        ScheduleLeaderEntity min = list.get(0);
        for (ScheduleLeaderEntity applicationNode : list) {
            if (applicationNode.getLastPing() < min.getLastPing()) {
                min = applicationNode;
            }
        }
        return min;
    }

    /**
     * Judgments the node is the leader of the cluster or not.
     * @return Boolean
     */
    @RunAsSolutionUser
    public boolean isLeader() {
        boolean isLeader =false;
        try{
            String nodeName = getNodeName();
            ScheduleLeaderEntity node = repository.findOne(nodeName);
            isLeader = (node != null && node.getIsLeader());
        } catch (Exception e){
            logger.error("error happend when execute isLeader:{}",e.getLocalizedMessage());
        }finally {
            return isLeader;
        }
    }

    /**
     * Sets all the node's leader-flag as false.
     * @param list  the list
     */
    private void setLeaderFlagAsFalse(final List<ScheduleLeaderEntity> list) {
        for (ScheduleLeaderEntity scheduleLeaderEntity : list) {
            scheduleLeaderEntity.setIsLeader(Boolean.FALSE);
        }
    }

}