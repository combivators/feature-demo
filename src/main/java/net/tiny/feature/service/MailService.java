package net.tiny.feature.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import net.tiny.dao.BaseService;
import net.tiny.el.ELParser;
import net.tiny.feature.entity.Setting;
import net.tiny.service.ServiceContext;
import net.tiny.service.communication.MailProvider;
import net.tiny.ws.mvc.TemplateParser;

public class MailService extends SettingService {

    private ExecutorService executor;
    private MailProvider.Builder builder;
    private TemplateParser templateParser;

    public MailService(ServiceContext context) {
        super(context);
        Setting setting = super.get();
        executor = context.lookup(ExecutorService.class);
        templateParser = context.lookup(TemplateParser.class);
        builder = mailProviderBuilder(setting);
    }

    public MailService(BaseService<?> base) {
        super(base);
        Setting setting = super.get();
        executor = base.service(ExecutorService.class);
        templateParser = base.service(TemplateParser.class);
        builder = mailProviderBuilder(setting);
    }

    private MailProvider.Builder mailProviderBuilder(Setting setting) {
        return new MailProvider.Builder()
                .smtp(setting.getSmtpHost(), setting.getSmtpPort(), setting.getSmtpUser(), setting.getSmtpPassword())
                .mail(setting.getEmail())
                .type(setting.getMailType())
                .executor(executor);
    }

    /**
     * 发送邮件
     */
    public void send(String to, String subject, String content) {
        MailProvider.Mail mail = builder.build()
                .to(to)
                .subject(subject)
                .content(content);
        mail.send();
     }

    /**
     * 发送邮件
     */
    public void send(String to, String subject, String template, Map<String, Object> param) {
        try {
            String content = templateParser.parse(template, param);
            send(to, subject, content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
     }

/*
    public void sendFindPasswordMail(String toMail, String username, SafeKey safeKey) {
        Setting setting = super.get();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("username", username);
        model.put("safeKey", safeKey);
        String subject = Message.getMessage("shop.password.mailSubject", setting.getSiteName());
        Template findPasswordMailTemplate = templateService.get("findPasswordMail");
        send(toMail, subject, findPasswordMailTemplate.getTemplatePath(), model);
    }

    public void sendProductNotifyMail(ProductNotify productNotify) {
        Setting setting = super.get();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("productNotify", productNotify);
        String subject = Message.getMessage("admin.productNotify.mailSubject", setting.getSiteName());
        Template productNotifyMailTemplate = templateService.get("productNotifyMail");
        send(productNotify.getEmail(), subject, productNotifyMailTemplate.getTemplatePath(), model);
    }
*/
}
