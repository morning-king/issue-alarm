package com.osp.issue.util;

import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author：lj 链接：https://www.jianshu.com/p/7f26f2703176
 */
@Slf4j
public class ExchangeClient {

    private final String hostname;
    private final ExchangeVersion exchangeVersion;
    private final String domain;
    private final String username;
    private final String password;
    private final String subject;
    private final String recipientTo;
    private final List<String> recipientCc;
    private final List<String> recipientBcc;
    private final List<String> attachments;
    private final String message;

    private ExchangeClient(ExchangeClientBuilder builder) {
        this.hostname = builder.hostname;
        this.exchangeVersion = builder.exchangeVersion;
        this.domain = builder.domain;
        this.username = builder.username;
        this.password = builder.password;
        this.subject = builder.subject;
        this.recipientTo = builder.recipientTo;
        this.recipientCc = builder.recipientCc;
        this.recipientBcc = builder.recipientBcc;
        this.attachments = builder.attachments;
        this.message = builder.message;
    }

    public boolean sendExchange() {
        // The Exchange Server Version.
        ExchangeService exchangeService = new ExchangeService(exchangeVersion);

        // Credentials to sign in the MS Exchange Server.
        ExchangeCredentials exchangeCredentials = new WebCredentials(username, password, domain);
        exchangeService.setCredentials(exchangeCredentials);

        // URL of exchange web service for the mailbox.
        try {
            exchangeService.setUrl(new URI("https://" + hostname + "/ews/Exchange.asmx"));
        } catch (URISyntaxException ex) {
            log.info("An exception occured while creating the uri for exchange service.", ex);
            return false;
        }

        // The email.
        EmailMessage emailMessage;
        try {
            emailMessage = new EmailMessage(exchangeService);
            emailMessage.setSubject(subject);
            emailMessage.setBody(MessageBody.getMessageBodyFromText(message));
        } catch (Exception ex) {
            log.info("An exception occured while setting the email message.", ex);
            return false;
        }

        // TO recipient.
        try {
            emailMessage.getToRecipients().add(recipientTo);
        } catch (ServiceLocalException ex) {
            log.info("An exception occured while sstting the TO recipient(" + recipientTo + ").", ex);
            return false;
        }

        // CC recipient.
        for (String recipient : recipientCc) {
            try {
                emailMessage.getCcRecipients().add(recipient);
            } catch (ServiceLocalException ex) {
                log.info("An exception occured while sstting the CC recipient(" + recipient + ").", ex);
                return false;
            }
        }

        // BCC recipient
        for (String recipient : recipientBcc) {
            try {
                emailMessage.getBccRecipients().add(recipient);
            } catch (ServiceLocalException ex) {
                log.info("An exception occured while sstting the BCC recipient(" + recipient + ").", ex);
                return false;
            }
        }

        // Attachements.
        for (String attachmentPath : attachments) {
            try {
                emailMessage.getAttachments().addFileAttachment(attachmentPath);
            } catch (ServiceLocalException ex) {
                log.info("An exception occured while setting the attachment.", ex);
                return false;
            }
        }

        try {
            emailMessage.send();
            log.info("An email is send.");
        } catch (Exception ex) {
            log.info("An exception occured while sending an email.", ex);
            return false;
        }

        return true;
    }

    public static class ExchangeClientBuilder {

        private String hostname;
        private ExchangeVersion exchangeVersion;
        private String domain;
        private String username;
        private String password;
        private String subject;
        private String recipientTo;
        private List<String> recipientCc;
        private List<String> recipientBcc;
        private List<String> attachments;
        private String message;

        public ExchangeClientBuilder() {
            this.exchangeVersion = ExchangeVersion.Exchange2010_SP1;
            this.hostname = "";
            this.username = "";
            this.password = "";
            this.subject = "";
            this.recipientTo = "";
            this.recipientCc = new ArrayList<>(0);
            this.recipientBcc = new ArrayList<>(0);
            this.attachments = new ArrayList<>(0);
            this.message = "";
        }

        /**
         * The hostname of the Exchange Web Service. It will be used for connecting with URI
         * https://hostname/ews/exchange.asmx
         *
         * @param hostname the hostname of the MS Exchange Smtp Server.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        /**
         * The Exchange Web Server version.
         *
         * @param exchangeVersion the Exchange Web Server version.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder exchangeVersion(ExchangeVersion exchangeVersion) {
            this.exchangeVersion = exchangeVersion;
            return this;
        }

        /**
         * The domain of the MS Exchange Smtp Server.
         *
         * @param domain the domain of the Active Directory. The first part of the username. For example:
         *               MYDOMAIN\\username, set the MYDOMAIN.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder domain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * The username of the MS Exchange Smtp Server. The second part of the username. For example:
         * MYDOMAIN\\username, set the username.
         *
         * @param username the username of the MS Exchange Smtp Server.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * The password of the MS Exchange Smtp Server.
         *
         * @param password the password of the MS Exchange Smtp Server.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * The subject for this send.
         *
         * @param subject the subject for this send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * The recipient for this send.
         *
         * @param recipientTo the recipient for this send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder recipientTo(String recipientTo) {
            this.recipientTo = recipientTo;
            return this;
        }

        /**
         * You can specify one or more email address that will be used as cc recipients.
         *
         * @param recipientCc  the first cc email address.
         * @param recipientsCc the other cc email address for this send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder recipientCc(String recipientCc, String... recipientsCc) {
            // Prepare the list.
            List<String> recipients = new ArrayList<>(1 + recipientsCc.length);
            recipients.add(recipientCc);
            recipients.addAll(Arrays.asList(recipientsCc));
            // Set the list.
            this.recipientCc = recipients;
            return this;
        }

        /**
         * You can specify a list with email addresses that will be used as cc for this email send.
         *
         * @param recipientCc the list with email addresses that will be used as cc for this email send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder recipientCc(List<String> recipientCc) {
            this.recipientCc = recipientCc;
            return this;
        }

        /**
         * You can specify one or more email address that will be used as bcc recipients.
         *
         * @param recipientBcc  the first bcc email address.
         * @param recipientsBcc the other bcc email address for this send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder recipientBcc(String recipientBcc, String... recipientsBcc) {
            // Prepare the list.
            List<String> recipients = new ArrayList<>(1 + recipientsBcc.length);
            recipients.add(recipientBcc);
            recipients.addAll(Arrays.asList(recipientsBcc));
            // Set the list.
            this.recipientBcc = recipients;
            return this;
        }

        /**
         * You can specify a list with email addresses that will be used as bcc for this email send.
         *
         * @param recipientBcc the list with email addresses that will be used as bcc for this email send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder recipientBcc(List<String> recipientBcc) {
            this.recipientBcc = recipientBcc;
            return this;
        }

        /**
         * You can specify one or more email address that will be used as cc recipients.
         *
         * @param attachment  the first attachment.
         * @param attachments the other attachments for this send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder attachments(String attachment, String... attachments) {
            // Prepare the list.
            List<String> attachmentsToUse = new ArrayList<>(1 + attachments.length);
            attachmentsToUse.add(attachment);
            attachmentsToUse.addAll(Arrays.asList(attachments));
            // Set the list.
            this.attachments = attachmentsToUse;
            return this;
        }

        /**
         * You can specify a list with email attachments that will be used for this email send.
         *
         * @param attachments the list with email attachments that will be used for this email send.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder attachments(List<String> attachments) {
            this.attachments = attachments;
            return this;
        }

        /**
         * The body of the email message.
         *
         * @param message the body of the email message.
         * @return the builder for chain usage.
         */
        public ExchangeClientBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Build a mail.
         *
         * @return an EmailApacheUtils object.
         */
        public ExchangeClient build() {
            return new ExchangeClient(this);
        }
    }

}
