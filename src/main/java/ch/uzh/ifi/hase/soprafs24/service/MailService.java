package ch.uzh.ifi.hase.soprafs24.service;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MailService {
  private final Logger log = LoggerFactory.getLogger(SessionService.class);

  private final MailjetClient mailjetClient;

  // todo how to mock?
  @Value("${MAILJET_SENDER_EMAIL}") private String senderEmail;
  private Integer templateId = 5930543;

  public MailService(MailjetClient mailjetClient) {
    System.out.println("mailjet service");
    this.mailjetClient = mailjetClient;
  }

  public void setSenderEmail(String senderEmail) {
    this.senderEmail = senderEmail;
  }

  public MailjetResponse sendMail(String receiverEmail, Integer templateId, JSONObject variables)
      throws MailjetException {
    MailjetRequest request;
    MailjetResponse response;

    request =
        new MailjetRequest(Emailv31.resource)
            .property(Emailv31.MESSAGES,
                new JSONArray().put(
                    new JSONObject()
                        .put(Emailv31.Message.FROM,
                            new JSONObject()
                                .put("Email", senderEmail)
                                .put("Name", "ProductiviTeam Service"))
                        .put(Emailv31.Message.TO,
                            new JSONArray().put(new JSONObject()
                                                    .put("Email", receiverEmail)
                                                    .put("Name", "New ProductiviTeam Member")))
                        .put(Emailv31.Message.TEMPLATEID, templateId) // expects int64
                        .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                        .put(Emailv31.Message.VARIABLES, variables)
                        .put(Emailv31.Message.SUBJECT, "New ProductiviTeam Invitation!")));

    try {
      response = mailjetClient.post(request);
      log.info(response.getStatus() + " " + response.getData());
    } catch (MailjetException ex) {
      log.error("Mail API Error: ", ex);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Mail API Exception. Did not send mail. Please check if valid email. Otherwise contact Administrator.");
    }

    return response;
  }

  public void sendInvitationEmail(String receiverEmail, String invitationUrl) {
    JSONObject variables = new JSONObject().put("invitationUrl", invitationUrl);

    try {
      sendMail(receiverEmail, templateId, variables);
    } catch (MailjetException e) {
      log.error("Failed to send invitation email; ", e);
    }
  }
}
