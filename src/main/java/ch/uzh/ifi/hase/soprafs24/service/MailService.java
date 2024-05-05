package ch.uzh.ifi.hase.soprafs24.service;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import java.io.IOException;
import java.util.regex.Pattern;
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

  // default NULL, so it can be overwritten in tests
  @Value("${MAILJET_SENDER_EMAIL:#{null}}") private String senderEmail;
  private static final Integer templateId = 5930543;

  /* OWASP pattern */
  private String emailRegexPattern =
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

  public MailService(MailjetClient mailjetClient) {
    this.mailjetClient = mailjetClient;
  }

  public void setSenderEmail(String senderEmail) {
    this.senderEmail = senderEmail;
  }

  /**
   * @param receiverEmail String of receiver
   * @param templateId int64
   * @param variables JSONObject with variables for the template
   * @return MailjetResponse response object
   * @throws ResponseStatusException if mail API or validate error
   */
  public MailjetResponse sendMail(String receiverEmail, Integer templateId, JSONObject variables)
      throws ResponseStatusException {
    MailjetRequest request;
    MailjetResponse response;

    // syntax validate email
    if (!patternMatches(receiverEmail, emailRegexPattern)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email address.");
    }

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
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "Mail API Exception. Did not send mail. Please check if correct email. Otherwise contact Administrator.");
    }

    return response;
  }

  public void sendInvitationEmail(String receiverEmail, String invitationUrl) {
    JSONObject variables = new JSONObject().put("invitationUrl", invitationUrl);

    sendMail(receiverEmail, templateId, variables);
  }

  /**
   * @param emailAddress email address to validate
   * @param regexPattern regex pattern to match
   * @return boolean
   * @see <a
   *     href="https://www.baeldung.com/java-email-validation-regex">https://www.baeldung.com/java-email-validation-regex</a>
   */
  private static boolean patternMatches(String emailAddress, String regexPattern) {
    return Pattern.compile(regexPattern).matcher(emailAddress).matches();
  }
}
