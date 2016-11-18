package pokerBase;

import static org.junit.Assert.*;

import org.junit.Test;

import exceptions.DeckException;
import exceptions.HandException;
import pokerEnums.eCardDestination;
import pokerEnums.eDrawCount;
import pokerEnums.eGame;

public class GamePlay_Test {

	@Test
	public void GamePlayFiveStudTest1() {
		
		Rule rle = new Rule(eGame.FiveStud);
		Player pDealer = new Player("Bert",0);		
		GamePlay gme = new GamePlay(rle,pDealer.getPlayerID());
		gme.addPlayerToGame(pDealer);
		gme.addPlayerHandToGame(pDealer);
		
		try {
			gme.drawCard(pDealer, eCardDestination.Player);
			gme.drawCard(pDealer, eCardDestination.Player);
			gme.drawCard(pDealer, eCardDestination.Player);
			gme.drawCard(pDealer, eCardDestination.Player);
			gme.drawCard(pDealer, eCardDestination.Player);
			
			gme.seteDrawCountLast(eDrawCount.FOURTH);
		} catch (DeckException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		Hand h = gme.getPlayerHand(pDealer);
		try {
			h = Hand.EvaluateHand(h);
		} catch (HandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gme.isGameOver();
		
		try {
			gme.ScoreGame();
		} catch (HandException e) {
			e.printStackTrace();
		}
		System.out.println(h.getHandScore().getHandStrength());
		
		Hand hWinner = gme.GetWinningHand();
		
		System.out.println(hWinner.getHandScore().getHandStrength());
	}

}
