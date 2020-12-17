package com.coremedia.blueprint.feedbackhub.imagga;

import com.coremedia.cache.Cache;
import com.coremedia.cache.config.CacheConfiguration;
import com.coremedia.feedbackhub.adapter.FeedbackHubAdapterFactory;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        CacheConfiguration.class,
})
@DefaultAnnotation(NonNull.class)
public class ImaggaFeedbackHubConfiguration {
  @Bean
  public FeedbackHubAdapterFactory imaggaFeedbackHubAdapterFactory(Cache cache) {
    return new ImaggaFeedbackHubAdapterFactory(cache);
  }
}
