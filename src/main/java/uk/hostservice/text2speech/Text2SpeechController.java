package uk.hostservice.text2speech;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.Voice;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

@RestController
@CrossOrigin
public class Text2SpeechController {

  private TextToSpeechClient textToSpeechClient;

  public Text2SpeechController(AppConfig appConfig) {

    System.out.println("Current location : " + new File("./config/").getAbsolutePath());
      System.out.println("Text2SpeechController " + appConfig.getCredentialsPath());
      System.out.println("Text2SpeechController " + appConfig.toString());
    try {
      ServiceAccountCredentials credentials = ServiceAccountCredentials
          .fromStream(Files.newInputStream(Paths.get(appConfig.getCredentialsPath())));
      TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
          .setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
      this.textToSpeechClient = TextToSpeechClient.create(settings);
    }
    catch (IOException e) {
      LoggerFactory.getLogger(Text2SpeechController.class)
          .error("init Text2SpeechController", e);
    }
  }

  @PreDestroy
  public void destroy() {
    if (this.textToSpeechClient != null) {
      this.textToSpeechClient.close();
    }
  }

  @GetMapping("voices")
  public List<VoiceDto> getSupportedVoices() {
    ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();
    ListVoicesResponse listreponse = this.textToSpeechClient.listVoices(request);
    return listreponse.getVoicesList().stream()
        .map(voice -> new VoiceDto(getSupportedLanguage(voice), voice.getName(),
            voice.getSsmlGender().name()))
        .collect(Collectors.toList());
  }

  @PostMapping("speakText")
  public byte[] speakText(@RequestParam("language") String language,
      @RequestParam("voice") String voice, @RequestParam("text") String text,
      @RequestParam("pitch") double pitch,
      @RequestParam("speakingRate") double speakingRate) {

    SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
    return convertToBytes(language, voice, pitch, speakingRate, input);
  }

  @PostMapping("speak")
  public byte[] speakSSML(@RequestParam("language") String language,
      @RequestParam("voice") String voice, @RequestParam("text") String text,
      @RequestParam("pitch") double pitch,
      @RequestParam("speakingRate") double speakingRate) {

    String ssmlText = convertToSSMLText(text);
    SynthesisInput input = SynthesisInput.newBuilder().setSsml(ssmlText).build();
    return convertToBytes(language, voice, pitch, speakingRate, input);
  }

  private byte[] convertToBytes(String language, String voice, double pitch, double speakingRate, SynthesisInput input) {
    VoiceSelectionParams voiceSelection = VoiceSelectionParams.newBuilder()
        .setLanguageCode(language).setName(voice).build();

    AudioConfig audioConfig = AudioConfig.newBuilder().setPitch(pitch)
        .setSpeakingRate(speakingRate).setAudioEncoding(AudioEncoding.MP3).build();

    SynthesizeSpeechResponse response = this.textToSpeechClient.synthesizeSpeech(input,
        voiceSelection, audioConfig);
    System.out.println("response " +response.isInitialized());

    return response.getAudioContent().toByteArray();
  }

  private static String getSupportedLanguage(Voice voice) {
    List<ByteString> languageCodes = voice.getLanguageCodesList().asByteStringList();
    for (ByteString languageCode : languageCodes) {
      return languageCode.toStringUtf8();
    }
    return null;
  }

  private String convertToSSMLText(String text) {
    String expandedNewline = text.replaceAll("\\*", "\n<break time='1s'/>");
    String ssml = "<speak>" + expandedNewline + "</speak>";
    System.out.println("Converted string " + ssml);
    return ssml;
  }

}
