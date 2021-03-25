package com.zcj.nettyClinet.tools;

public enum SingletonStatistics {
    INSTANCE;
    private long timerForDeblock;
    private long timerForDiff;
    private long timerForGene;
    private long timerForTotal;
    private long timerForTest;

    private long timerForDeblock_t = 0L;
    private long timerForDiff_t = 0L;
    private long timerForGene_t = 0L;
    private long timerForTotal_t = 0L;
    private long timerForTest_t = 0L;

    private int blockCount = 0;
    private long diffCount = 0L;
    private long testCount = 0L;

    public void init(){
        timerForDeblock = 0L;
        timerForDiff = 0L;
        timerForGene = 0L;
        timerForTotal = 0L;
        timerForTest = 0L;

        timerForDeblock_t = 0L;
        timerForDiff_t = 0L;
        timerForGene_t = 0L;
        timerForTotal_t = 0L;
        timerForTest_t = 0L;

        blockCount = 0;
        diffCount = 0L;
        testCount = 0L;

    }

    public void blockCountAdd(){
        this.blockCount++;
    }
    public void blockCountClear(){
        this.blockCount = 0;
    }
    public void blockCountPrint(){
        printCounter("blockCount", this.blockCount, "个");
    }

    public void diffCountAdd(long length){
        this.diffCount += length;
    }
    public void diffCountPrint(){
        printCounter("diffCount", this.diffCount, "bytes");
    }

    public void testCoutnAdd(long num){
        this.testCount += num;
    }
    public void testCoutnPrint(){
        printCounter("testCount", this.testCount, "bytes");
    }

    /* ***************Timer Method********************* */
    public void timerForDeblock_Start(){
        this.timerForDeblock = System.currentTimeMillis();
    }
    public void timerForDeblock_OverAndPrint(){
        printTimer("timerForDeblock",System.currentTimeMillis()-this.timerForDeblock);
    }
    //支持累加
    public void timerForDeblock_OverOne(){
        this.timerForDeblock_t += System.currentTimeMillis()-this.timerForDeblock;
    }
    public void timerForDeblock_OverFinal(){
        printTimer("timerForDeblock",this.timerForDeblock_t);
        this.timerForDeblock_t = 0L;
    }

    public void timerForDiff_Start(){
        this.timerForDiff = System.currentTimeMillis();
    }
    public void timerForDiff_OverAndPrint(){
        printTimer("timerForDiff",System.currentTimeMillis()-this.timerForDiff);
    }
    //支持累加
    public void timerForDiff_OverOne(){
        this.timerForDiff_t += System.currentTimeMillis()-this.timerForDiff;
    }
    public void timerForDiff_OverFinal(){
        printTimer("timerForDiff",this.timerForDiff_t);
        this.timerForDiff_t = 0L;
    }

    public void timerForGene_Start(){
        this.timerForGene = System.currentTimeMillis();
    }
    public void timerForGene_OverAndPrint(){
        printTimer("timerForGene",System.currentTimeMillis()-this.timerForGene);
    }
    //支持累加
    public void timerForGene_OverOne(){
        this.timerForGene_t += System.currentTimeMillis()-this.timerForGene;
    }
    public void timerForGene_OverFinal(){
        printTimer("timerForGene",this.timerForGene_t);
        this.timerForGene_t = 0L;
    }

    public void timerForTotal_Start(){
        this.timerForTotal = System.currentTimeMillis();
    }
    public void timerForTotal_OverAndPrint(){
        printTimer("timerForTotal",System.currentTimeMillis()-this.timerForTotal);
    }
    //支持累加
    public void timerForTotal_OverOne(){
        this.timerForTotal_t += System.currentTimeMillis()-this.timerForTotal;
    }
    public void timerForTotal_OverFinal(){
        printTimer("timerForTotal",this.timerForTotal_t);
        this.timerForTotal_t = 0L;
    }

    public void timerForTest_Start(){
        this.timerForTest = System.currentTimeMillis();
    }
    public void timerForTest_OverAndPrint(){
        printTimer("timerForTest",System.currentTimeMillis()-this.timerForTest);
    }
    //支持累加
    public void timerForTest_OverOne(){
        this.timerForTest_t += System.currentTimeMillis()-this.timerForTest;
    }
    public void timerForTest_OverFinal(){
        printTimer("timerForTest",this.timerForTest_t);
        this.timerForTest_t = 0L;
    }
    public void timerForTest_Now(String info){
        //截止现在用时多久
        printTimer("timerForTest--"+info,System.currentTimeMillis()-this.timerForTest);
        this.timerForTest = System.currentTimeMillis();
    }


    private void printTimer(String timerName, long time){
        System.out.println("[Statistics-timer]-["+timerName+"]"+String.valueOf(time)+"ms");
    }

    private void printCounter(String counterName, long count, String type){
        System.out.println("[Statistics-counter]-["+counterName+"]"+String.valueOf(count)+"["+type+"]");
    }

    /* ***************getter and setter******************** */
    public long getTimerForDeblock() {
        return timerForDeblock;
    }

    public void setTimerForDeblock(long timerForDeblock) {
        this.timerForDeblock = timerForDeblock;
    }

    public long getTimerForDiff() {
        return timerForDiff;
    }

    public void setTimerForDiff(long timerForDiff) {
        this.timerForDiff = timerForDiff;
    }

    public long getTimerForGene() {
        return timerForGene;
    }

    public void setTimerForGene(long timerForGene) {
        this.timerForGene = timerForGene;
    }

    public long getTimerForTotal() {
        return timerForTotal;
    }

    public void setTimerForTotal(long timerForTotal) {
        this.timerForTotal = timerForTotal;
    }

    public long getTimerForTest() {
        return timerForTest;
    }

    public void setTimerForTest(long timerForTest) {
        this.timerForTest = timerForTest;
    }


}
