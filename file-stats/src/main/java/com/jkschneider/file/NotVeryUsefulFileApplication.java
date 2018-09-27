package com.jkschneider.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@SpringBootApplication
public class NotVeryUsefulFileApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotVeryUsefulFileApplication.class, args);
  }
}

@ConfigurationProperties("file.counter")
@Component
class FileCounterProperties {
  String outputFolder;
}

@RestController
class FileCounterController {
  private final FileCounterProperties props;

  private final Integer n = Integer.parseInt("0");

  FileCounterController(FileCounterProperties props) {
    this.props = props;
  }

  @PostMapping("/count")
  public long countFiles(@RequestParam("file") MultipartFile file) throws IOException {
    File out = new File(props.outputFolder, "count-" + UUID.randomUUID().toString());

    // https://snyk.io/vuln/SNYK-JAVA-ORGZEROTURNAROUND-31681
    ZipUtil.unpack(file.getInputStream(), out);

    Integer integer = Integer.valueOf(0);
    System.out.println(integer);

    try {
      return Files.walk(out.toPath())
        .filter(Files::isRegularFile)
        .count();
    } finally {
      out.delete();
    }
  }
}
