package com.cloneCoin.analysis.config;

public enum AlgorithmEnum {

    MD5("MD5"),
    SHA3256("SHA3-256"),
    SHA256("SHA-256");

    /** 알고리즘 이름 */
    private String algorithm;
    /** 알고리즘 이름을 반환한다. */
    public String getAlgorithm() {
        return this.algorithm;
    }
    AlgorithmEnum(String algorithm) {
        this.algorithm = algorithm;
    }
}///~