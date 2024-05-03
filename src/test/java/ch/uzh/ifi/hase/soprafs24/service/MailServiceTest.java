package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MailServiceTest {
  @Mock private MailjetClient mailjetClient;
  @Mock private MailjetResponse mailjetResponse;

  private MailService mailService;

  private String senderEmail;
  private String receiverEmail;
  private Integer templateId;
  private JSONObject variables;
  private String invitationUrl;
  @Captor ArgumentCaptor<MailjetRequest> requestCaptor;

  @BeforeEach
  private void setup() throws MailjetException {
    MockitoAnnotations.openMocks(this);

    senderEmail = "sender@productiviteam.co";
    receiverEmail = "receiver@productiviteam.co";
    templateId = 1;
    invitationUrl = "https://productiviteam.co";
    variables = new JSONObject().put("invitationUrl", "https://productiviteam.co");

    mailService = new MailService(mailjetClient);
    mailService.setSenderEmail(senderEmail);

    Mockito.when(mailjetClient.post(Mockito.any())).thenReturn(mailjetResponse);
  }

  @Test
  public void sendMail_validInput_success() throws ResponseStatusException, MailjetException {
    // called with
    JSONObject mailjetRequestProperty =
        new JSONObject()
            .put(Emailv31.Message.FROM,
                new JSONObject().put("Email", senderEmail).put("Name", "ProductiviTeam Service"))
            .put(Emailv31.Message.TO,
                new JSONArray().put(new JSONObject()
                                        .put("Email", receiverEmail)
                                        .put("Name", "New ProductiviTeam Member")))
            .put(Emailv31.Message.TEMPLATEID, templateId) // expects int64
            .put(Emailv31.Message.TEMPLATELANGUAGE, true)
            .put(Emailv31.Message.VARIABLES, variables)
            .put(Emailv31.Message.SUBJECT, "New ProductiviTeam Invitation!");

    MailjetResponse response = mailService.sendMail(receiverEmail, templateId, variables);

    // Assert
    Mockito.verify(mailjetClient, Mockito.times(1)).post(requestCaptor.capture());

    JSONArray bodyMessages = (JSONArray) requestCaptor.getValue().getBodyJSON().get("Messages");
    assertTrue(mailjetRequestProperty.similar(bodyMessages.getJSONObject(0)));
  }

  /* test if mail client post request error is converted correctly. This might happen if some fields
   * are not set correctly (e.g. mail address not valid, or auth error).*/
  @Test
  public void sendMail_somethingWrong_ExpectsException()
      throws ResponseStatusException, MailjetException {
    // called with (irrelevant)
    JSONObject mailjetRequestProperty = new JSONObject();

    Mockito.when(mailjetClient.post(Mockito.any()))
        .thenThrow(new MailjetException("Connection Error"));

    // Assert
    assertThrows(ResponseStatusException.class,
        () -> mailService.sendMail(receiverEmail, templateId, variables));
    Mockito.verify(mailjetClient, Mockito.times(1)).post(Mockito.any());
  }

  /* test if incorrect receiver email */
  @Test
  public void sendMail_invalidEmail_ExpectsException()
      throws ResponseStatusException, MailjetException {
    // called with (irrelevant)
    JSONObject mailjetRequestProperty = new JSONObject();

    // Assert
    assertThrows(ResponseStatusException.class,
        () -> mailService.sendMail("1nv#lid-ma.l@mydomin.e", templateId, variables));
    Mockito.verify(mailjetClient, Mockito.never()).post(Mockito.any());
  }

  @Test
  public void sendInvitationEmail_validInput_success()
      throws ResponseStatusException, MailjetException {
    // when other class method is called -> mock success
    MailService spyMailService = Mockito.spy(mailService);
    Mockito.doReturn(mailjetResponse)
        .when(spyMailService)
        .sendMail(Mockito.anyString(), Mockito.anyInt(), Mockito.any());

    // when call -> ok
    spyMailService.sendInvitationEmail(receiverEmail, invitationUrl);

    // assert if called with expected params
    Mockito.verify(spyMailService, Mockito.times(1))
        .sendMail(Mockito.eq(receiverEmail), Mockito.anyInt(), Mockito.any());
  }

  @Test
  public void sendInvitationEmail_sendMailException_ExpectsException()
      throws ResponseStatusException, MailjetException {
    // when other class method is called -> mock exception
    MailService spyMailService = Mockito.spy(mailService);
    Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
        .when(spyMailService)
        .sendMail(Mockito.anyString(), Mockito.anyInt(), Mockito.any());

    // assert if called with expected params
    assertThrows(ResponseStatusException.class,
        () -> spyMailService.sendInvitationEmail(receiverEmail, invitationUrl));
  }
}
