package com.vinod.aws.secret.store.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.vinod.aws.secret.store.config.SecretStorePropertySourceConfigurationProperties.*;


@Component
@Log4j2
public class SecretStorePropertySourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static boolean initialized;
	private ObjectMapper mapper;

    private String accessKey="accessKey";
    private String secretKey="secretKey";


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application)
    {
        if (!initialized && isParameterStorePropertySourceEnabled(environment)) {
        	mapper = new ObjectMapper();

        	//AWSSecretsManager simpleSystemsManagementClient = AWSSecretsManagerClientBuilder.defaultClient();
            AWSSecretsManager awsSecretsManager = AWSSecretsManagerClientBuilder.standard().withCredentials(new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))).withRegion("ap-south-1").build();
            SecretStoreSource secretStoreSource = new SecretStoreSource(awsSecretsManager);

            Map<String, Object> map = new HashMap<>();
            setSecretProperties(secretStoreSource, environment.getProperty("database.secret.key"), map);
            MutablePropertySources propertySources = environment.getPropertySources();
            propertySources.addFirst(new MapPropertySource(SECRET_STORE_PROPERTY_SOURCE_NAME, map));

            initialized = true;
        }
    }
    
    private void setSecretProperties(SecretStoreSource secretStoreSource, String propertyName, Map<String, Object> map) {
	    log.info("Retrieving secret from secret store for property : {}", propertyName);
	    Object secret2 = secretStoreSource.getSecret(propertyName);
        String secret = secret2!=null?secret2.toString():"{}";
        log.info("Retrieved secret from secret store for property : {}", propertyName);
	    Map<String, Object> mapPgp = null;
	    try {
	      mapPgp = mapper.readValue(secret, new TypeReference<Map<String, Object>>() {});
	    } catch (IOException e) {
	      log.error("Exception thrown while retrieving secret from secret store for property : {}", propertyName);
	    }
	    map.putAll(mapPgp);
	  }

    private boolean isParameterStorePropertySourceEnabled(ConfigurableEnvironment environment)
    {
        String[] userDefinedEnabledProfiles = environment.getProperty(SECRET_STORE_ACCEPTED_PROFILES_CONFIGURATION_PROPERTY, String[].class);
        return environment.getProperty(SECRET_STORE_ENABLED_CONFIGURATION_PROPERTY, Boolean.class, Boolean.FALSE) || environment.acceptsProfiles(SECRET_STORE_ACCEPTED_PROFILE) || (!ObjectUtils.isEmpty(userDefinedEnabledProfiles) && environment.acceptsProfiles(userDefinedEnabledProfiles));
    }
}
