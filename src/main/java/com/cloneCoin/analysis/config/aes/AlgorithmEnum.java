package com.cloneCoin.analysis.config.aes;

public enum AlgorithmEnum {

    MD5("MD5"),
    SHA3256("SHA3-256"),
    SHA256("SHA-256");

    private String algorithm;

    public String getAlgorithm() {
        return this.algorithm;
    }
    AlgorithmEnum(String algorithm) {
        this.algorithm = algorithm;
    }
}///~