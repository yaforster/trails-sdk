package io.github.yaforster.trails.core.test.action.value;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public record RandomValueInstruction(String prefix, String suffix, String charPool,
        int randomStringLength) implements ValueComputationInstruction {

    @Override
    public String computeValue() {
        return generateValue();
    }

    private String generateValue() {
        String safePrefix = Optional.ofNullable(prefix)
                                    .orElse("");
        String safeSuffix = Optional.ofNullable(suffix)
                                    .orElse("");
        String safeCharPool = Optional.ofNullable(charPool)
                                      .orElse("");
        if (safeCharPool.isEmpty() || randomStringLength <= 0) {
            return safePrefix + safeSuffix;
        }
        String randomPart = generateRandomPart(randomStringLength, safeCharPool);
        return safePrefix + randomPart + safeSuffix;
    }

    private String generateRandomPart(int randomStringLength, String characterPool) {
        StringBuilder randomPart = new StringBuilder(randomStringLength);
        int poolLength = characterPool.length();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int position = 0; position < randomStringLength; position++) {
            int index = random.nextInt(poolLength);
            randomPart.append(characterPool.charAt(index));
        }
        return randomPart.toString();
    }
}
