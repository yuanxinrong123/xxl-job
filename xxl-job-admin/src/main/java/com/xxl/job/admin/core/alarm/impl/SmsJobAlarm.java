package com.xxl.job.admin.core.alarm.impl;

import com.submail.config.AppConfig;
import com.submail.lib.MESSAGEXsend;
import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class SmsJobAlarm implements JobAlarm {

    private static Logger logger = LoggerFactory.getLogger(SmsJobAlarm.class);

    @Value("${msg_appid}")
    private String APP_ID;
    @Value("${msg_appkey}")
    private String APP_KEY;
    @Value("${msg_signtype}")
    private String APP_SIGNTYPE;
    @Value("${msg_project}")
    private String APP_PROJECT;

    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        boolean alarmResult = true;
        if(StringUtils.isNotBlank(info.getAlarmSms())){
            Set<String> smsSet = new HashSet<String>(Arrays.asList(info.getAlarmSms().trim().split(",")));
            for (String phone : smsSet) {
                AppConfig config = new AppConfig();
                config.setAppId(APP_ID);
                config.setAppKey(APP_KEY);
                config.setSignType(APP_SIGNTYPE);
                MESSAGEXsend submail = new MESSAGEXsend(config);
                submail.addTo(phone);
                submail.setProject(APP_PROJECT);
                submail.addVar("id",String.valueOf(info.getId()));
                submail.addVar("jobDesc",info.getJobDesc());
                try{
                    String xsend = submail.xsend();
                    logger.info(">>>>>>>>>>> xxl-job, job sms alarm, JobLogId:{},JobCallBack:{}", jobLog.getId(), xsend);
                }catch (Exception e){
                    logger.error(">>>>>>>>>>> xxl-job, job fail alarm sms send error, JobLogId:{},error:{}", jobLog.getId(), e);
                    alarmResult = false;
                }

            }
        }
        
        return alarmResult;
    }
}
