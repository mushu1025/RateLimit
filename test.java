public class RateLimiter {
    private double maxPermits;
    private double rate;
    private double storedPermits;
    private long nextFreeTicketMicros;

    public RateLimiter(double rate) {
        this.rate = rate;
        this.maxPermits = rate;
        this.storedPermits = 0.0;
        this.nextFreeTicketMicros = System.nanoTime();
    }

    public synchronized void acquire(int permits) throws InterruptedException {
        if (permits <= 0) {
            return;
        }
        long nowMicros = System.nanoTime();
        resync(nowMicros);
        double storedPermitsToSpend = Math.min(permits, this.storedPermits);
        double freshPermits = permits - storedPermitsToSpend;
        long waitMicros = (long) (freshPermits * (1000000L / this.rate));
        Thread.sleep(waitMicros / 1000, (int) (waitMicros % 1000) * 1000);
        this.storedPermits -= storedPermitsToSpend;
        this.nextFreeTicketMicros += waitMicros;
    }

    private void resync(long nowMicros) {
        if (nowMicros > this.nextFreeTicketMicros) {
            double newPermits = (nowMicros - this.nextFreeTicketMicros) / 1000000.0 * this.rate;
            this.storedPermits = Math.min(this.maxPermits, this.storedPermits + newPermits);
            this.nextFreeTicketMicros = nowMicros;
        }
    }
}
public static void main(String[] args) throws InterruptedException {
    RateLimiter rateLimiter = new RateLimiter(10); // 10 permits per second
    for (int i = 0; i < 20; i++) {
        rateLimiter.acquire(1);
        System.out.println("Acquired permit " + (i + 1));
    }
}
