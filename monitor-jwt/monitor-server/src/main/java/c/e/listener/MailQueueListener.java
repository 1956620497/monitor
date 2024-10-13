package c.e.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

//监听器    用来监听邮件发送
@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    //发送邮件
    @RabbitHandler
    public void sendMailMessage(Map<String,Object> data){
        //将邮件地址取出
        String email = (String) data.get("email");
        //取出验证码
        Integer code = (Integer) data.get("code");
        //取出邮件类型
        String type = (String)data.get("type");
        //根据不同的类型返回不同的邮件参数
        SimpleMailMessage message = switch (type){
            case "reset" -> createMessage(
                    "您的密码重置邮件",
                    "您好，您正在进行重置密码操作，验证码：" + code + ",有效时间三分钟，如非本人操作，请检查您的账号是否泄露。",
                    email);
            default -> null;
        };
        //判断邮件是否正确
        if (message == null) return;
        //发送邮件
        sender.send(message);
    }

    //发送邮件的方法
    private SimpleMailMessage createMessage(String title,String content,String email){
        //邮件发送框架对象
        SimpleMailMessage message = new SimpleMailMessage();
        //设置邮件主题
        message.setSubject(title);
        //设置邮件内容
        message.setText(content);
        //设置邮件发送目标
        message.setTo(email);
        //自己的邮件发送地址
        message.setFrom(username);
        //返回邮件对象
        return message;
    }


}
