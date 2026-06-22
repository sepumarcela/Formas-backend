package com.formas.cms.orders;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "formas.payments.wompi")
public class WompiProperties {
  private String publicKey = "";
  private String privateKey = "";
  private String integritySecret = "";
  private String eventsSecret = "";
  private String checkoutUrl = "https://checkout.wompi.co/p/";
  private String redirectUrl = "";

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getIntegritySecret() {
    return integritySecret;
  }

  public void setIntegritySecret(String integritySecret) {
    this.integritySecret = integritySecret;
  }

  public String getEventsSecret() {
    return eventsSecret;
  }

  public void setEventsSecret(String eventsSecret) {
    this.eventsSecret = eventsSecret;
  }

  public String getCheckoutUrl() {
    return checkoutUrl;
  }

  public void setCheckoutUrl(String checkoutUrl) {
    this.checkoutUrl = checkoutUrl;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public boolean isCheckoutConfigured() {
    return hasText(publicKey) && hasText(integritySecret);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
