package com.vinod.aws.secret.store.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Log4j2
public class SecretStoreSource {
    private AWSSecretsManager ssmClient;

    public SecretStoreSource(AWSSecretsManager ssmClient) {
        this.ssmClient = ssmClient;
    }

    public Object getSecret(String secretName) {
        String secret = null;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = ssmClient.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            log.error("We  can't find the resource that you asked for. Deal with the exception here, and/or rethrow at your discretion.", e);
        }
        if (getSecretValueResult != null && getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
        }
        return secret;
    }
}
