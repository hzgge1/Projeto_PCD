package game;

public class CountDownLatch {
	
	private int count;
	
	public CountDownLatch(int count) {
		this.count = count;
	}
	
	public synchronized void countDown() {
		if(count == 0) return;
		count --;
		if(count == 0)
			notifyAll();
	}
	
	public synchronized void await() throws InterruptedException {
		while(count > 0)
			wait();
	}

}
