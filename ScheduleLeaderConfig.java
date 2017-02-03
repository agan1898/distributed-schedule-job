
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

@Configuration
public class ScheduleLeaderConfig {

    @Bean(name = "createOrUpdateNode")
    @Autowired
    public MethodInvokingJobDetailFactoryBean createOrUpdateNode(ScheduleLeaderProcesser scheduleLeaderProcesser) {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(scheduleLeaderProcesser);
        bean.setTargetMethod("createOrUpdateNode");
        bean.setConcurrent(false);
        return bean;
    }

    @Bean(name = "electionLeaderShip")
    @Autowired
    public MethodInvokingJobDetailFactoryBean electionLeaderShip(ScheduleLeaderProcesser scheduleLeaderProcesser) {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(scheduleLeaderProcesser);
        bean.setTargetMethod("electionLeaderShip");
        bean.setConcurrent(false);
        return bean;
    }

    @Bean
    @Autowired
    public CronTriggerFactoryBean createOrUpdateNodeTriggerFactoryBean(@Qualifier("createOrUpdateNode") MethodInvokingJobDetailFactoryBean bean) {
        return SchedulerUtils.buildTriggerFactory(bean, Utils.getEnv(Constants.CRON_SCHEDULE_CREATE_OR_UPDATE_NODE, "0/15 * * * * ? *"));
    }

    @Bean
    @Autowired
    public CronTriggerFactoryBean electionLeaderShipTriggerFactoryBean(@Qualifier("electionLeaderShip") MethodInvokingJobDetailFactoryBean bean) {
        return SchedulerUtils.buildTriggerFactory(bean, Utils.getEnv(Constants.CRON_SCHEDULE_ELECTION_LEADER, "0/20 * * * * ? *"));
    }
}
