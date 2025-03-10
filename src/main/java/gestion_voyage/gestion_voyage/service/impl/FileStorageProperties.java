package gestion_voyage.gestion_voyage.service.impl;

import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageProperties {

  private String uploadDir = "uploads";

  public String getUploadDir() {
    return uploadDir;
  }

  public void setUploadDir(String uploadDir) {
    this.uploadDir = uploadDir;
  }
}
