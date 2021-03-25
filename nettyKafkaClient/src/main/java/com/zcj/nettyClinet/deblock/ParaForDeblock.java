package com.zcj.nettyClinet.deblock;

public enum ParaForDeblock {
    INSTANCE;

    /* ************** common ******************** */
    private int windowSize;
    private String fileName;

    /* ************* special ****************** */
    private int pci_windowSize;
    private int pci_threshold;

    /* ************* static code ****************** */
    {
        windowSize = 700;
        pci_windowSize = 20;//15
        pci_threshold = 100; //77
        fileName = "e://test0.txt";
    }

    public int getWindowSize() {
        return windowSize;
    }

    public ParaForDeblock setWindowSize(int windowSize) {
        this.windowSize = windowSize;
        return this;
    }

    public int getPci_windowSize() {
        return pci_windowSize;
    }

    public ParaForDeblock setPci_windowSize(int pci_windowSize) {
        this.pci_windowSize = pci_windowSize;
        return this;
    }

    public int getPci_threshold() {
        return pci_threshold;
    }

    public ParaForDeblock setPci_threshold(int pci_threshold) {
        this.pci_threshold = pci_threshold;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ParaForDeblock setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}
