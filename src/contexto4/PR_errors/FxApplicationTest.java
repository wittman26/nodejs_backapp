package com.acelera.fx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootTest(properties = { "spring.profiles.active=native", "external.sscc.base-url=http://mock-url.com" })
class FxApplicationTest {

  @Autowired
  private ConfigurableEnvironment environment;

  @Test
  void contextLoads() {
    assertThat(environment).isNotNull();
  }
}
