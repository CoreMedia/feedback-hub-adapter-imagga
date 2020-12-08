package com.coremedia.blueprint.feedbackhub.imagga;

import com.coremedia.cache.Cache;
import com.coremedia.feedbackhub.adapter.FeedbackHubAdapterFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration(proxyBeanMethods = false)
@ImportResource(value = {
        "classpath:/com/coremedia/cache/cache-services.xml",
}, reader = ResourceAwareXmlBeanDefinitionReader.class)@DefaultAnnotation(NonNull.class)
public class ImaggaFeedbackHubConfiguration {
  @Bean
  public FeedbackHubAdapterFactory imaggaFeedbackHubAdapterFactory(Cache cache) {
    return new ImaggaFeedbackHubAdapterFactory(cache);
  }
}
