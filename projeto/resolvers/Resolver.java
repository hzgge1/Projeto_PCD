package resolvers;

import game.AutoPlayer;
import game.Game;
import game.Player;

public class Resolver extends Thread{
	
private AutoPlayer player;
	
	public Resolver(AutoPlayer player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}

	@Override
	public void run() {
		try {
			while(true) {
				Thread.sleep(Game.MAX_WAITING_TIME_FOR_MOVE);
				if(!player.isAlive()) return;
				if(player.isStuck()) {
					player.changeIsStuck();
					player.interrupt();
					return;
				}
			}
		} catch (InterruptedException e) {
			return;
		}
	}
	

}
