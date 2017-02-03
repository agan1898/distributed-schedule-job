
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by ganxiaojian on 16/12/17.
 */
@Aspect
@Component
public class ScheduleLeaderProvider {

    @Autowired
    private ScheduleLeaderProcesser scheduleLeaderProcesser;

    @Around("@annotation(IsScheduleLeader))")
    public void isScheduleLeader(ProceedingJoinPoint point) throws Throwable {
        try {
            boolean isLeader =  scheduleLeaderProcesser.isLeader();
            if(isLeader){
                point.proceed();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
